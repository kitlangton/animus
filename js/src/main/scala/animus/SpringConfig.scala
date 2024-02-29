package animus

final class SpringConfig[V](
  var value: V,
  var velocity: V,
  var targetValue: V,
  val stiffness: Double = 157.913,
  val damping: Double = 25.132,
  val mass: Double = 1,
  val epsilon: Double = 0.001
)(implicit V: VectorArithmetic[V]) {

  def update(
    deltaTime: Double
  ): Boolean = {
    val displacement = V.subtract(value, targetValue)
    val springForce  = V.scaledBy(displacement, -stiffness)
    val dampingForce = V.scaledBy(velocity, damping)

    val acceleration = V.scaledBy(V.subtract(springForce, dampingForce), 1 / mass)

    velocity = V.add(velocity, V.scaledBy(acceleration, deltaTime))
    value = V.add(value, V.scaledBy(velocity, deltaTime))

    // Return true if the animation is "settled"
    V.magnitudeSquared(displacement) < epsilon &&
    V.magnitudeSquared(velocity) < epsilon
  }

  def setTarget(newTarget: V): Unit =
    this.targetValue = newTarget

}

object SpringConfig {
  def make[V](value: V, target: V)(implicit V: VectorArithmetic[V]) =
    new SpringConfig(value = value, velocity = V.zero, targetValue = target)
}
