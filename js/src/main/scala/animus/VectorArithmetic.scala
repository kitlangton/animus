package animus

trait VectorArithmetic[A] {
  def subtract(lhs: A, rhs: A): A
  def add(lhs: A, rhs: A): A
  def scaledBy(lhs: A, rhs: Double): A
  def magnitudeSquared(value: A): Double
  def zero: A
}

object VectorArithmetic {

  implicit val doubleArithmetic: VectorArithmetic[Double] = new VectorArithmetic[Double] {
    override def zero: Double                               = 0
    override def subtract(lhs: Double, rhs: Double): Double = lhs - rhs
    override def add(lhs: Double, rhs: Double): Double      = lhs + rhs
    override def scaledBy(lhs: Double, rhs: Double): Double = lhs * rhs
    override def magnitudeSquared(value: Double): Double    = value * value
  }

  implicit val doubleTupleArithmetic: VectorArithmetic[(Double, Double)] = new VectorArithmetic[(Double, Double)] {
    override def zero: (Double, Double) = (0, 0)
    override def subtract(lhs: (Double, Double), rhs: (Double, Double)): (Double, Double) =
      (lhs._1 - rhs._1, lhs._2 - rhs._2)
    override def add(lhs: (Double, Double), rhs: (Double, Double)): (Double, Double) =
      (lhs._1 + rhs._1, lhs._2 + rhs._2)
    override def scaledBy(lhs: (Double, Double), rhs: Double): (Double, Double) =
      (lhs._1 * rhs, lhs._2 * rhs)
    override def magnitudeSquared(value: (Double, Double)): Double =
      value._1 * value._1 + value._2 * value._2
  }

}
