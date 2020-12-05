package animus

import scala.annotation.tailrec
import scala.scalajs.js

final class Spring(
    var value: Double = 0,
    var velocity: Double = 0,
    var target: Double = 0,
    stiffness: Double = 170,
    damping: Double = 26,
    precision: Double = 0.005
) extends js.Object { self =>

  def tick(t: Double): Unit = {
    val delta = 1.0 / 80.0

    val fSpring = -stiffness * (value - target);
    val fDamper = -damping * velocity;
    val a       = fSpring + fDamper;

    val newVelocity = velocity + a * delta
    val newValue    = value + newVelocity * delta;

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
