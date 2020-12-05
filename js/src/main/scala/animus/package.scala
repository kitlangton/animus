import com.raquo.airstream.eventstream.EventStream
import com.raquo.airstream.signal.{Signal, Var}

package object animus {
  def spring[A](
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

  implicit final class SignalOps[A](private val value: Signal[A]) extends AnyVal {
    def spring(implicit animatable: Animatable[A]): Signal[A] = animus.spring(value)
  }

  implicit final class EventStreamOps[A](private val value: EventStream[A]) extends AnyVal {
    def spring(initial: => A)(implicit animatable: Animatable[A]): Signal[A] = animus.spring(value.startWith(initial))
  }
}
