package example

import animus.{ObservableOps, ResizeObserver, SignalOps}
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveElement.Base
import example.styles.transitionOpacity

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.scalajs.js
import scala.scalajs.js.timers.{SetTimeoutHandle, clearTimeout, setTimeout}

import TransitionStatus._

sealed trait TransitionStatus { self =>
  def isActive: Boolean = self == TransitionStatus.Active

}

object TransitionStatus {
  case object Inserting extends TransitionStatus
  case object Active    extends TransitionStatus
  case object Removing  extends TransitionStatus
}

object Transitions {
  def transitioning[A, Key, Output](
      $as: Signal[A]
  )(key: A => Key)(project: (Key, A, Signal[A], Signal[TransitionStatus]) => Output): Signal[Seq[Output]] = {
    transitionList($as.map(Seq(_)))(key)(project)
  }

  def slideList[A]($items: Signal[Seq[A]])(render: (A, Signal[A]) => HtmlElement): Signal[Seq[Div]] =
    transitionList($items)(identity) { (key, init, $signal, $status) =>
      div(
        render(init, $signal),
        Transitions.transitionHeightAndOpacity($status.map(_ == TransitionStatus.Active))
      )
    }

  def slide($isOpen: Signal[Boolean], dynamicHeight: Boolean = false)(
      content: => HtmlElement
  ): Signal[Option[HtmlElement]] =
    slideOption($isOpen.map { b => Option.when(b)(b) }, dynamicHeight) { _ => content }

  def transitionWidth($isVisible: Signal[Boolean]): Mod[HtmlElement] = Seq(
    overflowX.hidden,
    onMountBind { (el: MountContext[Base]) =>
      maxWidth <-- $isVisible.map { b =>
        if (b) el.thisNode.ref.scrollWidth.toDouble
        else 0.0
      }.spring.px
    }
  )

  def transitionHeight($isVisible: Signal[Boolean]): Mod[HtmlElement] = Seq(
    overflowY.hidden,
    onMountBind { (el: MountContext[Base]) =>
      val events = ResizeObserver
        .resize(el.thisNode)
        .mapTo(el.thisNode.ref.scrollHeight.toDouble)
        .toSignal(el.thisNode.ref.scrollHeight.toDouble)

      height <-- events
        .combineWith($isVisible)
        .map { case (h, b) =>
          if (b) h
          else 0.0
        }
        .spring
        .px
    }
  )

  def transitionHeightAndOpacity($isVisible: Signal[Boolean], dynamicHeight: Boolean = false): Mod[HtmlElement] = Seq(
    overflowY.hidden,
    transitionOpacity($isVisible),
    onMountBind { (el: MountContext[Base]) =>
      lazy val $height = ResizeObserver
        .resize(el.thisNode.ref.firstElementChild)
        .mapTo(el.thisNode.ref.scrollHeight.toDouble)
        .toSignal(0.0)
      maxHeight <-- $isVisible.flatMap { b =>
        if (b) {
          if (dynamicHeight) $height
          else Val(el.thisNode.ref.scrollHeight.toDouble)
        } else Val(0.0)
      }.spring.px
    }
  )

  def slideOption[A]($signal: Signal[Option[A]], dynamicHeight: Boolean = false)(
      render: Signal[A] => HtmlElement
  ): Signal[Option[HtmlElement]] =
    transitionOption(
      $signal,
      { $isVisible =>
        Seq(
          overflow.hidden,
          transitionOpacity($isVisible),
          onMountBind { (el: MountContext[Base]) =>
            lazy val observer = ResizeObserver
              .resize(el.thisNode.ref.firstElementChild)
              .mapTo(el.thisNode.ref.scrollHeight.toDouble)
              .toSignal(0.0)
            maxHeight <-- $isVisible.flatMap { b =>
              if (b) {
                if (dynamicHeight) observer
                else Val(el.thisNode.ref.scrollHeight.toDouble)
              } else Val(0.0)
            }.spring.px
          }
        )
      }
    )(render)

  def transitionOption[A]($signal: Signal[Option[A]], transition: Signal[Boolean] => Modifier[HtmlElement])(
      render: Signal[A] => Base
  ): Signal[Option[HtmlElement]] = {
    import TransitionStatus._

    val status       = Var(Option.empty[TransitionStatus])
    var lastValue: A = null.asInstanceOf[A]

    var timeoutHandle: SetTimeoutHandle = null

    def delay(ms: Int)(action: => Unit) = {
      if (timeoutHandle != null) clearTimeout(timeoutHandle)
      timeoutHandle = setTimeout(ms)(action)
    }

    val $valueAndTransitionStatus = $signal.map {
      case Some(value) =>
        status.now() match {
          case Some(Active) =>
          case Some(_) =>
            if (timeoutHandle != null) clearTimeout(timeoutHandle)
            status.set(Some(Active))
          case None =>
            status.set(Some(Inserting))
            delay(0) { status.set(Some(Active)) }
        }
        lastValue = value
        Some(value)
      case None =>
        status.now() match {
          case Some(Removing) =>
          case Some(_) =>
            status.set(Some(Removing))
            delay(950) { status.set(None) }
          case None =>
        }
        Option(lastValue)
    }
      .combineWith(status.signal)
      .map {
        case (Some(value), Some(status)) =>
          Some(value, status)
        case _ => None
      }

    $valueAndTransitionStatus.split(_._1) { (key, value, $value) =>
      val $isVisible = $value.map(_._2 match {
        case TransitionStatus.Active => true
        case _                       => false
      })

      div(
        render($value.map(_._1)),
        transition($isVisible)
      )
    }
  }

  def transitionList[A, Key, Output](
      $items: Signal[Seq[A]]
  )(getKey: A => Key)(project: (Key, A, Signal[A], Signal[TransitionStatus]) => Output): Signal[Seq[Output]] = {

    type ValueMap = Map[Key, (A, TransitionStatus)]
    val valueMap: Var[ValueMap] = Var(Map.empty[Key, (A, TransitionStatus)])

    val ordering = new Ordering[Key](Vector.empty)

    val allKeys = mutable.Set.empty[Key]

    type StatusList = Seq[(A, TransitionStatus)]
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
          case Some((_, Removing)) =>
            addValue(key, x, Active)
          case Some((_, status)) =>
            addValue(key, x, status)
          case None =>
            addValue(key, x, Inserting)
            addTimer(key) {
              valueMap.update(_.updated(key, x -> Active))
            }
        }
      }

      (allKeys diff newKeys).foreach { key =>
        current.get(key) match {
          case Some((x, _)) =>
            addValue(key, x, Removing)
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
        project(k, init._1, $a.map(_._1), $a.map(_._2))
      }
  }
}
