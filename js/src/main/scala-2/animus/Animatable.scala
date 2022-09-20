package animus

trait Animatable[A] {

  type Anim

  def toAnim(start: A, value: A): Anim

  def fromAnim(anim: Anim): A

  def tick(anim: Anim, time: Double): Boolean

  def update(anim: Anim, newValue: A): Unit
}

object Animatable extends AnimatableImplicits
