import com.raquo.airstream.core.Observable
import com.raquo.airstream.eventstream.EventStream
import com.raquo.airstream.features.FlattenStrategy
import com.raquo.airstream.signal.{Signal, Var}

import scala.scalajs.js.timers.setTimeout

package object animus {
  sealed trait Animation { self =>
    import Animation._

    def to(double: => Double): Animation = {
      lazy val double0 = double
      Sequence(self, Value(() => double0))
    }

    def wait(ms: Int): Animation = Hold(ms, self)

    def flattened: Vector[(() => Double, Double)] =
      self match {
        case Value(value) => Vector((value, 300))
        case Hold(delay, animation) =>
          val flatAnim                            = animation.flattened
          val (lastValue, _)                      = flatAnim.last
          val res: Vector[(() => Double, Double)] = flatAnim.dropRight(1).appended((lastValue, delay))
          if (lastValue() == 50) {
            println(lastValue())
            println(delay)
            println(flatAnim.map(p => p._1() -> p._2))
            println(res.map(p => p._1() -> p._2))
          }
          res
        case Sequence(lhs, rhs) =>
          lhs.flattened ++ rhs.flattened
      }

    def signal: Signal[Double] = {
      val flat     = flattened
      val variable = Var(flat.head._1())

      def go(values: Vector[(() => Double, Double)]): Unit = values.headOption.foreach { case (value, delay) =>
        variable.set(value())
        setTimeout(delay) {
          go(values.tail)
        }
      }

      go(flat)

      variable.signal
    }

    def run: Signal[Double] = signal.spring

  }

  object Animation {
    def from(double: => Double): Animation = {
      lazy val double0 = double
      Value(() => double0)
    }

    private case class Value(value: () => Double)               extends Animation
    private case class Hold(delay: Int, animation: Animation)   extends Animation
    private case class Sequence(lhs: Animation, rhs: Animation) extends Animation
  }

  def spring[A, Output](
      $value: Signal[A],
      stiffness: Double = 170,
      damping: Double = 26
  )(implicit animatable: Animatable[A]): Signal[A] = {
    val runner = new SpringRunner[A]()
    $value.flatMap(runner.animateTo(_, stiffness = stiffness, damping = damping))
  }

  class SpringRunner[A](implicit animatable: Animatable[A]) {
    private var anim: animatable.Anim   = _
    private val animating: Var[Boolean] = Var(true)

    private lazy val $time: EventStream[Double] = animating.signal.flatMap {
      case true  => RAFStream
      case false => EventStream.empty
    }

    def animateTo(value: A, stiffness: Double = 170, damping: Double = 26): Signal[A] = {
      if (anim == null)
        anim = animatable.toAnim(value)
      else
        animatable.updateAnim(anim, value)

      animating.set(true)

      $time.map { t =>
        if (animatable.tickAnim(anim, t)) {
          animating.set(false)
        }
        animatable.fromAnim(anim)
      }
        .startWith(animatable.fromAnim(anim))
    }
  }

  implicit final class ObservableOps[A](private val value: Observable[A]) extends AnyVal {
    def px(implicit numeric: Numeric[A]): Observable[String]      = value.map(x => s"${x}px")
    def percent(implicit numeric: Numeric[A]): Observable[String] = value.map(x => s"${x}%")
  }

  implicit final class SignalOps[A](private val value: Signal[A]) extends AnyVal {
    def spring(implicit animatable: Animatable[A]): Signal[A] = animus.spring(value)
  }

  implicit final class EventStreamOps[A](private val value: EventStream[A]) extends AnyVal {
    def spring(initial: => A)(implicit animatable: Animatable[A]): Signal[A] = animus.spring(value.startWith(initial))
  }

}
