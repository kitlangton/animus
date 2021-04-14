package animus

import com.raquo.airstream.common.{InternalTryObserver, SingleParentObservable}
import com.raquo.airstream.core.{EventStream, Signal, Transaction}
import com.raquo.airstream.state.Var

import scala.util.{Failure, Try}

class SpringRunner[A](implicit animatable: Animatable[A]) {
  private var anim: animatable.Anim   = _
  private val animating: Var[Boolean] = Var(true)

  private lazy val $time: EventStream[Double] = animating.signal.flatMap {
    case true  => AnimationFrameStream
    case false => EventStream.empty
  }

  def animateTo(value: A): Signal[A] = {
    if (anim == null)
      anim = animatable.toAnim(value)
    else
      animatable.update(anim, value)

    animating.set(true)

    $time.map { t =>
      if (animatable.tick(anim, t)) {
        animating.set(false)
      }
      animatable.fromAnim(anim)
    }
      .startWith(animatable.fromAnim(anim))
  }
}
