package animus

import animus.Animation.AnimationRunner
import com.raquo.airstream.eventstream.EventStream
import com.raquo.airstream.signal.{Signal, Var}

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

case class Animation(values: Vector[Double]) {
  def to(value: Double): Animation = Animation(values.appended(value))

  def run: Signal[Double] = {
    val runner = new AnimationRunner(this)
    runner.animate
  }
}

object Animation {
  def from(value: Double): Animation = Animation(Vector(value))

  def spring[A](
      $value: Signal[A],
      stiffness: Double = 170,
      damping: Double = 26
  )(implicit animatable: Animatable[A]): Signal[A] = {
    val runner = new SpringRunner[A]()
    $value.flatMap(runner.animateTo(_, stiffness = stiffness, damping = damping))
  }

  class AnimationRunner(val animation: Animation)(implicit animatable: Animatable[Double]) {
    var values: Vector[Double] = animation.values

    private var anim: animatable.Anim   = _
    private val animating: Var[Boolean] = Var(true)

    private lazy val $time: EventStream[Double] = animating.signal.flatMap {
      case true  => RAFStream
      case false => EventStream.empty
    }

    def animate: Signal[Double] = {
      anim = animatable.toAnim(values.head)
      values = values.tail

      animating.set(true)

      $time.map { t =>
        if (animatable.tickAnim(anim, t)) {
          values.headOption match {
            case Some(value) =>
              animatable.updateAnim(anim, value)
              values = values.tail
            case None =>
              animating.set(false)
          }
        }
        animatable.fromAnim(anim)
      }
        .startWith(animatable.fromAnim(anim))
    }
  }

}
