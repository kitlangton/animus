package animus

trait Animatable[A] {

  type Anim

  def toAnim(value: A, configure: Spring => Spring): Anim

  def fromAnim(anim: Anim): A

  def tick(anim: Anim, time: Double): Boolean

  def update(anim: Anim, newValue: A): Unit
}

object Animatable extends AnimatableImplicits
