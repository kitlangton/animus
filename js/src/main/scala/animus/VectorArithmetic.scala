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

}
