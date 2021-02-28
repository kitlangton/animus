package animus

import com.raquo.airstream.core.{EventStream, Signal}
import com.raquo.airstream.state.Var

class SpringRunner[A](implicit animatable: Animatable[A]) {
  private var anim: animatable.Anim   = _
  private val animating: Var[Boolean] = Var(true)

  private lazy val $time: EventStream[Double] = animating.signal.flatMap {
    case true  => RAFStream
    case false => EventStream.empty
  }

  def animateTo(value: A): Signal[A] = {
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
