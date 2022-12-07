package animus

import com.raquo.airstream.core.Signal
import com.raquo.airstream.state.Var
import com.raquo.laminar.api.L._
import com.raquo.laminar.api.L

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.scalajs.js
import scala.scalajs.js.timers.SetTimeoutHandle

case class Transition(signal: Signal[TransitionStatus]) {
  lazy val $isActive: Signal[Boolean] = signal.map(_.isActive)

  lazy val opacity: Mod[HtmlElement] =
    Transitions.opacity($isActive)

  lazy val width: Mod[HtmlElement] =
    Transitions.width($isActive)

  lazy val height: Mod[HtmlElement] =
    Transitions.height($isActive)
}

object Transitions {
  def opacity($visible: Signal[Boolean]): Mod[HtmlElement] =
    L.opacity <-- $visible.map { if (_) 1.0 else 0 }.spring

  def height($open: Signal[Boolean]): Mod[HtmlElement] =
    Seq(
      overflowY.hidden,
      onMountBind { (el: MountContext[HtmlElement]) =>
        L.maxHeight <-- $open.map {
          if (_)
            el.thisNode.ref.scrollHeight.toDouble
          else
            0.0
        }.spring.px
      }
    )

  def width($open: Signal[Boolean]): Mod[HtmlElement] =
    Seq(
      overflowX.hidden,
      onMountBind { (el: MountContext[HtmlElement]) =>
        L.maxWidth <-- $open.map {
          if (_)
            el.thisNode.ref.scrollWidth.toDouble
          else
            0.0
        }.spring.px
      }
    )

  def heightDynamic($isVisible: Signal[Boolean]): Mod[HtmlElement] = Seq(
    overflowY.hidden,
    onMountBind { (el: MountContext[HtmlElement]) =>
      lazy val $height = ResizeObserver
        .resize(el.thisNode.ref.firstElementChild)
        .mapTo(el.thisNode.ref.scrollHeight.toDouble)
        .toSignal(0.0)

      maxHeight <-- $isVisible.flatMap { b =>
        if (b) { $height }
        else Val(0.0)
      }.spring.px
    }
  )

  def widthDynamic($isVisible: Signal[Boolean]): Mod[HtmlElement] = Seq(
    overflowX.hidden,
    onMountBind { (el: MountContext[HtmlElement]) =>
      lazy val $width = ResizeObserver
        .resize(el.thisNode.ref.firstElementChild)
        .mapTo(el.thisNode.ref.scrollWidth.toDouble)
        .toSignal(0.0)

      maxWidth <-- $isVisible.flatMap { b =>
        if (b) { $width }
        else Val(0.0)
      }.spring.px
    }
  )

  def transitionList[A, Key, Output](
      $items: Signal[Seq[A]]
  )(getKey: A => Key)(project: (Key, A, Signal[A], Transition) => Output): Signal[Seq[Output]] = {

    type ValueMap = Map[Key, (A, TransitionStatus)]
    val valueMap: Var[ValueMap] = Var(Map.empty[Key, (A, TransitionStatus)])

    val ordering = new OrderedSet[Key](Vector.empty)

    val allKeys = mutable.Set.empty[Key]

    val timerMap = mutable.Map.empty[Key, SetTimeoutHandle]

    def cancelTimer(key: Key): Unit =
      timerMap.get(key).foreach(handle => js.timers.clearTimeout(handle))

    def addTimer(key: Key, ms: Int = 0)(body: => Unit): Unit = {
      cancelTimer(key)
      timerMap(key) = js.timers.setTimeout(ms)(body)
    }

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

        if (!allKeys.contains(key)) {
          allKeys.add(key)
        }

        cancelTimer(key)

        current.get(key) match {
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
      }

      (allKeys diff newKeys).foreach { key =>
        current.get(key) match {
          case Some((x, status)) if status != TransitionStatus.Removing =>
            addValue(key, x, TransitionStatus.Removing)
            addTimer(key, 950) {
              valueMap.update(_.removed(key))
              allKeys.remove(key)
              ordering.remove(key)
            }
          case _ => ()
        }
      }

      val changes = updates.foldLeft[ValueMap => ValueMap](identity)(_ compose _)
      valueMap.update(changes)

      xs
    }

    $adding.flatMap(_ =>
      $values.map { values =>
        ordering.toList.flatMap { key => values.get(key) }
      }
    )
      .split(v => getKey(v._1)) { (k, init, $a) =>
        project(k, init._1, $a.map(_._1), Transition($a.map(_._2)))
      }
  }

}
