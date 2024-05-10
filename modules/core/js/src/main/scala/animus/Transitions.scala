package animus

import com.raquo.airstream.core.Signal
import com.raquo.airstream.state.Var
import com.raquo.laminar.api.L.*
import com.raquo.laminar.api.L

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.scalajs.js
import scala.scalajs.js.timers.{SetTimeoutHandle, setTimeout}

case class Transition(signal: Signal[TransitionStatus]):
  lazy val $isActive: Signal[Boolean] = signal.map(_.isActive)

  lazy val opacity: Mod[HtmlElement] =
    Transitions.opacity($isActive)

  lazy val width: Mod[HtmlElement] =
    Transitions.width($isActive)

  def width(speed: Double): Mod[HtmlElement] =
    Transitions.width($isActive, speed)

  lazy val height: Mod[HtmlElement] =
    Transitions.height($isActive)

  def height(speed: Double): Mod[HtmlElement] =
    Transitions.height($isActive, speed)

  lazy val blur: Mod[HtmlElement] =
    Transitions.blur($isActive)

  def blur(closedBlur: Double): Mod[HtmlElement] =
    Transitions.blur($isActive, closedBlur)

  lazy val scale: Mod[HtmlElement] =
    Transitions.scale($isActive)

  def scale(closedScale: Double, speed: Double = 1): Mod[HtmlElement] =
    Transitions.scale($isActive, closedScale, speed)

  def offset(closedX: Double = 0, closedY: Double = 0): Mod[HtmlElement] =
    Transitions.offset($isActive, closedX, closedY)

object Transitions:
  def opacity($visible: Signal[Boolean]): Mod[HtmlElement] =
    L.opacity <-- $visible.map(if _ then 1.0 else 0).spring

  enum Status:
    case Inserting, Active, Removing

  def height($open: Signal[Boolean], speed: Double = 1): Mod[HtmlElement] =
    val scrollHeightVar = Var(0.0)

    val $scrollHeight = $open.distinct
      .flatMapSwitch {
        if _ then scrollHeightVar.signal
        else Val(0.0)
      }
      .spring(_.withSpeed(speed))
      .px

    val signalVar                          = Var($scrollHeight)
    var setTimeoutHandle: SetTimeoutHandle = null

    Seq(
      overflowY.hidden,
      onMountBind { ctx =>
        EventStream.periodic(10) --> { _ =>
          scrollHeightVar.set(ctx.thisNode.ref.scrollHeight.toDouble)
        }
      },
      onMountBind { ctx =>
        $open.distinct --> { open =>
          if setTimeoutHandle != null then js.timers.clearTimeout(setTimeoutHandle)
          if open then
            signalVar.set($scrollHeight)
            setTimeoutHandle = setTimeout(700) {
              signalVar.set(Val("none"))
            }
          else signalVar.set($scrollHeight)
        }
      },
      $scrollHeight --> { _ => () },
      maxHeight <-- signalVar.signal.flattenSwitch
    )

  def width($open: Signal[Boolean], speed: Double = 1): Mod[HtmlElement] =
    val scrollWidthVar = Var(0.0)

    val $scrollWidth = $open.distinct
      .flatMapSwitch {
        if _ then scrollWidthVar.signal
        else Val(0.0)
      }
      .spring(_.withSpeed(speed))
      .px

    val signalVar                          = Var($scrollWidth)
    var setTimeoutHandle: SetTimeoutHandle = null

    Seq(
      overflowY.hidden,
      onMountBind { ctx =>
        EventStream.periodic(10) --> { _ =>
          scrollWidthVar.set(ctx.thisNode.ref.scrollWidth.toDouble)
        }
      },
      onMountBind { ctx =>
        $open.distinct --> { open =>
          if setTimeoutHandle != null then js.timers.clearTimeout(setTimeoutHandle)
          if open then
            signalVar.set($scrollWidth)
            setTimeoutHandle = setTimeout(700) {
              signalVar.set(Val("none"))
            }
          else signalVar.set($scrollWidth)
        }
      },
      $scrollWidth --> { _ => () },
      maxWidth <-- signalVar.signal.flattenSwitch
    )

  def scale($open: Signal[Boolean], closedScale: Double = 0.0, speed: Double = 1): Mod[HtmlElement] =
    List(
      styleProp("transform") <-- $open.map(if _ then 1.0 else closedScale).spring(_.withSpeed(speed)).map { scale =>
        s"scale($scale)"
      },
      transformOrigin("top left")
    )

  def blur($open: Signal[Boolean], closedBlur: Double = 5.0): Mod[HtmlElement] =
    styleProp("filter") <-- $open.map(if _ then 0.0 else closedBlur).spring(_.withSpeed(0.8)).map { blur =>
      s"blur(${blur}px)"
    }

  def offset($open: Signal[Boolean], closedX: Double = 0.0, closedY: Double = 0.0): Mod[HtmlElement] =
    List(
      position.relative,
      top <-- $open.map(if _ then 0.0 else closedY).spring.px,
      left <-- $open.map(if _ then 0.0 else closedX).spring.px
    )

  def transitionList[A, Key, Output](
      $items: Signal[Seq[A]]
  )(getKey: A => Key)(project: (Key, A, Signal[A], Transition) => Output): Signal[Seq[Output]] =

    type ValueMap = Map[Key, (A, TransitionStatus)]
    val valueMap: Var[ValueMap] = Var(Map.empty[Key, (A, TransitionStatus)])

    val ordering = new OrderedSet[Key](Vector.empty)

    val allKeys = mutable.Set.empty[Key]

    val timerMap = mutable.Map.empty[Key, SetTimeoutHandle]

    def cancelTimer(key: Key): Unit =
      timerMap.get(key).foreach(handle => js.timers.clearTimeout(handle))

    def addTimer(key: Key, ms: Int = 0)(body: => Unit): Unit =
      cancelTimer(key)
      timerMap(key) = js.timers.setTimeout(ms)(body)

    val $values = valueMap.signal

    val $adding = $items.map { xs =>
      ordering.addValues(xs.map(getKey))

      val newKeys                                   = mutable.Set.empty[Key]
      val current                                   = valueMap.now()
      val updates: ListBuffer[ValueMap => ValueMap] = mutable.ListBuffer.empty

      def addValue(key: Key, value: A, status: TransitionStatus) =
        updates.addOne(_.updated(key, (value, status)))

      xs.foreach { x =>
        val key = getKey(x)
        newKeys.add(key)

        if !allKeys.contains(key) then allKeys.add(key)

        cancelTimer(key)

        current.get(key) match
          case Some((_, TransitionStatus.Removing)) =>
            addValue(key, x, TransitionStatus.Active)
          case Some((_, status)) =>
            addValue(key, x, status)
          case None =>
            addValue(key, x, TransitionStatus.Inserting)
            addTimer(key) {
              valueMap.update(_.updated(key, x -> TransitionStatus.Active))
            }
      }

      (allKeys diff newKeys).foreach { key =>
        current.get(key) match
          case Some((x, status)) if status != TransitionStatus.Removing =>
            addValue(key, x, TransitionStatus.Removing)
            addTimer(key, 950) {
              valueMap.update(_.removed(key))
              allKeys.remove(key)
              ordering.remove(key)
            }
          case _ => ()
      }

      val changes = updates.foldLeft[ValueMap => ValueMap](identity)(_ compose _)
      valueMap.update(changes)

      xs
    }

    $adding.flatMapSwitch(_ =>
      $values.map { values =>
        ordering.toList.flatMap(key => values.get(key))
      }
    )
      .split(v => getKey(v._1)) { (k, init, $a) =>
        project(k, init._1, $a.map(_._1), Transition($a.map(_._2)))
      }
