package animus

final class Animator[V](
  var value: V,
  var velocity: V,
  var targetValue: V,
  val stiffness: Double = 157.913,
  val damping: Double = 25.132,
  val mass: Double = 1,
  val epsilon: Double = 0.001,
  val speed: Double = 1
)(implicit V: VectorArithmetic[V]) {

  var isDone: Boolean     = false
  var callback: V => Unit = { _ => () }

  def update(): Unit = {
    val deltaSeconds = (1.0 / 120.0) * speed
    val displacement = V.subtract(value, targetValue)
    val springForce  = V.scaledBy(displacement, -stiffness)
    val dampingForce = V.scaledBy(velocity, damping)

    val acceleration = V.scaledBy(V.subtract(springForce, dampingForce), 1 / mass)

    velocity = V.add(velocity, V.scaledBy(acceleration, deltaSeconds))
    value = V.add(value, V.scaledBy(velocity, deltaSeconds))

    // Return true if the animation is "settled"
    callback(value)
    isDone = V.magnitudeSquared(displacement) < epsilon &&
      V.magnitudeSquared(velocity) < epsilon
  }

  def setTarget(newTarget: V): Unit =
    this.targetValue = newTarget

  def copy(
    value: V = value,
    velocity: V = velocity,
    targetValue: V = targetValue,
    stiffness: Double = stiffness,
    damping: Double = damping,
    mass: Double = mass,
    epsilon: Double = epsilon,
    speed: Double = speed
  ): Animator[V] =
    new Animator(value, velocity, targetValue, stiffness, damping, mass, epsilon, speed)

  def default: Animator[V]                  = copy(stiffness = 170, damping = 26)
  def gentle: Animator[V]                   = copy(stiffness = 120, damping = 14)
  def wobbly: Animator[V]                   = copy(stiffness = 180, damping = 12)
  def stiff: Animator[V]                    = copy(stiffness = 210, damping = 20)
  def slow: Animator[V]                     = copy(stiffness = 280, damping = 60)
  def molasses: Animator[V]                 = copy(stiffness = 280, damping = 120)
  def withSpeed(speed: Double): Animator[V] = copy(speed = speed)
}

object Animator {
  def make[V](value: V)(implicit V: VectorArithmetic[V]) =
    new Animator(value = value, velocity = V.zero, targetValue = value)

  def make[V](value: V, target: V)(implicit V: VectorArithmetic[V]) =
    new Animator(value = value, velocity = V.zero, targetValue = target)
}
