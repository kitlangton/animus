package animus

import scala.scalajs.js

final class Spring(
  var position: Double = 0,
  var velocity: Double = 0,
  var to: Double = 0,
  var lastTime: Double = -1,
  stiffness: Double = 170,
  damping: Double = 26,
  precision: Double = 0.005,
  speed: Double = 1
) extends js.Object { self =>

  def tick(t: Double): Unit = {
    val dt = if (lastTime <= 0) 8 else t - lastTime

    lastTime = t

//    val numSteps = (dt.toInt * speed).toInt
    val numSteps = dt.toInt

    var i = 0
    while (i < numSteps) {
      val springForce  = (-stiffness * 0.000001) * (position - to)
      val dampingForce = (-damping * 0.001) * velocity
      val acceleration = springForce + dampingForce

      val newVelocity = velocity + acceleration * speed
      val newPosition = position + (newVelocity * speed)

      if (Math.abs(newVelocity) < precision && Math.abs(newPosition - to) < precision) {
        position = to
        velocity = 0
        i = numSteps // done
        lastTime = -1
      } else {
        position = newPosition
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

  def copy(
    position: Double = self.position,
    velocity: Double = self.velocity,
    to: Double = self.to,
    lastTime: Double = self.lastTime,
    stiffness: Double = self.stiffness,
    damping: Double = self.damping,
    precision: Double = self.precision,
    speed: Double = self.speed
  ): Spring =
    new Spring(position, velocity, to, lastTime, stiffness, damping, precision, speed)

  def default: Spring                  = copy(stiffness = 170, damping = 26)
  def gentle: Spring                   = copy(stiffness = 120, damping = 14)
  def wobbly: Spring                   = copy(stiffness = 180, damping = 12)
  def stiff: Spring                    = copy(stiffness = 210, damping = 20)
  def slow: Spring                     = copy(stiffness = 280, damping = 60)
  def molasses: Spring                 = copy(stiffness = 280, damping = 120)
  def withSpeed(speed: Double): Spring = copy(speed = speed)

}

object Spring {
  def fromValueAndTarget(value: Double, target: Double): Spring =
    new Spring(position = value, to = target)

  def fromValue(value: Double): Spring =
    new Spring(position = value, to = value)

  def minMax(value: Double, min: Double, max: Double): Double =
    Math.min(Math.max(value, min), max)

}
