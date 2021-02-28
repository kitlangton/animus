package animus

import com.raquo.airstream.core.Signal
import com.raquo.airstream.state.Var

import scala.scalajs.js.timers.setTimeout

sealed trait Animation { self =>
  import Animation._

  def to(double: => Double): Animation = {
    lazy val double0 = double
    Sequence(self, Value(() => double0))
  }

  def wait(ms: Int): Animation = Hold(ms, self)

  def flattened: Vector[(() => Double, Double)] =
    self match {
      case Value(value) => Vector((value, 300))
      case Hold(delay, animation) =>
        val flatAnim                            = animation.flattened
        val (lastValue, _)                      = flatAnim.last
        val res: Vector[(() => Double, Double)] = flatAnim.dropRight(1).appended((lastValue, delay))
        if (lastValue() == 50) {
          println(lastValue())
          println(delay)
          println(flatAnim.map(p => p._1() -> p._2))
          println(res.map(p => p._1() -> p._2))
        }
        res
      case Sequence(lhs, rhs) =>
        lhs.flattened ++ rhs.flattened
    }

  def signal: Signal[Double] = {
    val flat     = flattened
    val variable = Var(flat.head._1())

    def go(values: Vector[(() => Double, Double)]): Unit = values.headOption.foreach { case (value, delay) =>
      variable.set(value())
      setTimeout(delay) {
        go(values.tail)
      }
    }

    go(flat)

    variable.signal
  }

  def run: Signal[Double] = signal.spring

}

object Animation {
  def from(double: => Double): Animation = {
    lazy val double0 = double
    Value(() => double0)
  }

  private case class Value(value: () => Double)               extends Animation
  private case class Hold(delay: Int, animation: Animation)   extends Animation
  private case class Sequence(lhs: Animation, rhs: Animation) extends Animation
}
