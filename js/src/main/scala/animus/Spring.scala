package animus

import scala.scalajs.js

final class Spring(
    var value: Double = 0,
    var velocity: Double = 0,
    var target: Double = 0,
    var lastTime: Double = -1,
    stiffness: Double = 175,
    damping: Double = 28,
    precision: Double = 0.005
) extends js.Object { self =>

  def tick(t: Double): Unit = {
    val delta =
//      if (lastTime == -1.0) {
      1.0 / 50.0
//      } else {
//        (t - lastTime) / 1000.0
//      }

    val fSpring = -stiffness * (value - target);
    val fDamper = -damping * velocity;
    val a       = fSpring + fDamper;

    val newVelocity = velocity + a * delta
    val newValue    = value + newVelocity * delta;

    lastTime = t

    if (Math.abs(newVelocity) < precision && Math.abs(newValue - target) < precision) {
      value = target
      velocity = 0
    } else {
      value = newValue
      velocity = newVelocity
    }
  }

  def isDone: Boolean = value == target && velocity == 0d

  def setTarget(newTarget: Double): Unit =
    this.target = newTarget
}

object Spring {
  def fromValue(value: Double): Spring =
    new Spring(value = value, target = value)

  def minMax(value: Double, min: Double, max: Double): Double =
    Math.min(Math.max(value, min), max)
}
