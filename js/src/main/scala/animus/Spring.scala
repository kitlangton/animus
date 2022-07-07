package animus

import scala.scalajs.js

final class Spring(
  var position: Double = 0,
  var velocity: Double = 0,
  var to: Double = 0,
  var lastTime: Double = -1,
  stiffness: Double = 170,
  damping: Double = 26,
  precision: Double = 0.005
) extends js.Object { self =>

  private val FPS = 1.0 / 1000.0

  def tick(t: Double): Unit = {
    val dt = if (lastTime <= 0) 8 else t - lastTime

    lastTime = t

    val numSteps = dt.toInt

    var i    = 0
    var done = false
    while (i < numSteps && !done) {

      val fSpring = -stiffness * (position - to);
      val fDamper = -damping * velocity;
      val a       = fSpring + fDamper;

      val newVelocity = velocity + a * FPS
      val newValue    = position + (newVelocity * FPS)

      if (Math.abs(newVelocity) < precision && Math.abs(newValue - to) < precision) {
        position = to
        velocity = 0
        i = numSteps // done
        lastTime = -1
      } else {
        position = newValue
        velocity = newVelocity
      }

      i += 1
    }

    velocity = velocity
    position = position
  }

  def isDone: Boolean = position == to && velocity == 0d

  def setTarget(newTarget: Double): Unit =
    this.to = newTarget
}

object Spring {
  def fromValue(value: Double): Spring =
    new Spring(position = value, to = value)

  def minMax(value: Double, min: Double, max: Double): Double =
    Math.min(Math.max(value, min), max)

}
