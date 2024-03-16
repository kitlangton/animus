package animus

import animus.internal.VectorArithmeticMacro

import scala.scalajs.js

trait VectorArithmetic[A]:
  def subtract(lhs: A, rhs: A): A
  def add(lhs: A, rhs: A): A
  def scaledBy(lhs: A, rhs: Double): A
  def magnitudeSquared(value: A): Double
  def zero: A

object VectorArithmetic:

  inline def derived[A: VectorArithmetic]: VectorArithmetic[A] =
    ${ VectorArithmeticMacro.deriveImpl[A] }

  given doubleArithmetic: VectorArithmetic[Double] = new VectorArithmetic[Double]:
    override def zero: Double                               = 0
    override def subtract(lhs: Double, rhs: Double): Double = lhs - rhs
    override def add(lhs: Double, rhs: Double): Double      = lhs + rhs
    override def scaledBy(lhs: Double, rhs: Double): Double = lhs * rhs
    override def magnitudeSquared(value: Double): Double    = value * value

  given floatArithmetic: VectorArithmetic[Float] = new VectorArithmetic[Float]:
    override def zero: Float                              = 0
    override def subtract(lhs: Float, rhs: Float): Float  = lhs - rhs
    override def add(lhs: Float, rhs: Float): Float       = lhs + rhs
    override def scaledBy(lhs: Float, rhs: Double): Float = (lhs * rhs).toFloat
    override def magnitudeSquared(value: Float): Double   = value * value

  given listArithmetic: VectorArithmetic[List[Double]] = new VectorArithmetic[List[Double]]:
    override def zero: List[Double] = List.empty
    override def subtract(lhs: List[Double], rhs: List[Double]): List[Double] =
      lhs.zip(rhs).map { case (l, r) => l - r }
    override def add(lhs: List[Double], rhs: List[Double]): List[Double] =
      lhs.zip(rhs).map { case (l, r) => l + r }
    override def scaledBy(lhs: List[Double], rhs: Double): List[Double] =
      lhs.map(_ * rhs)
    override def magnitudeSquared(value: List[Double]): Double =
      value.map(v => v * v).sum

  given vectorArithmetic[A](using arith: VectorArithmetic[A]): VectorArithmetic[Vector[A]] =
    new VectorArithmetic[Vector[A]]:
      override def zero: Vector[A] = Vector.empty
      override def subtract(lhs: Vector[A], rhs: Vector[A]): Vector[A] =
        lhs.zip(rhs).map { case (l, r) => arith.subtract(l, r) }
      override def add(lhs: Vector[A], rhs: Vector[A]): Vector[A] =
        lhs.zip(rhs).map { case (l, r) => arith.add(l, r) }
      override def scaledBy(lhs: Vector[A], rhs: Double): Vector[A] =
        lhs.map(arith.scaledBy(_, rhs))
      override def magnitudeSquared(value: Vector[A]): Double =
        value.map(arith.magnitudeSquared).sum

  given tuple2Arithmetic: VectorArithmetic[(Double, Double)] = new VectorArithmetic[(Double, Double)]:
    override def zero: (Double, Double) = (0, 0)

    override def subtract(lhs: (Double, Double), rhs: (Double, Double)): (Double, Double) =
      (lhs._1 - rhs._1, lhs._2 - rhs._2)

    override def add(lhs: (Double, Double), rhs: (Double, Double)): (Double, Double) =
      (lhs._1 + rhs._1, lhs._2 + rhs._2)

    override def scaledBy(lhs: (Double, Double), rhs: Double): (Double, Double) =
      (lhs._1 * rhs, lhs._2 * rhs)

    override def magnitudeSquared(value: (Double, Double)): Double =
      value._1 * value._1 + value._2 * value._2

  given tuple3Arithmetic: VectorArithmetic[(Double, Double, Double)] = new VectorArithmetic[(Double, Double, Double)]:
    override def zero: (Double, Double, Double) = (0, 0, 0)

    override def subtract(lhs: (Double, Double, Double), rhs: (Double, Double, Double)): (Double, Double, Double) =
      (lhs._1 - rhs._1, lhs._2 - rhs._2, lhs._3 - rhs._3)

    override def add(lhs: (Double, Double, Double), rhs: (Double, Double, Double)): (Double, Double, Double) =
      (lhs._1 + rhs._1, lhs._2 + rhs._2, lhs._3 + rhs._3)

    override def scaledBy(lhs: (Double, Double, Double), rhs: Double): (Double, Double, Double) =
      (lhs._1 * rhs, lhs._2 * rhs, lhs._3 * rhs)

    override def magnitudeSquared(value: (Double, Double, Double)): Double =
      value._1 * value._1 + value._2 * value._2 + value._3 * value._3

  given tuple4Arithmetic: VectorArithmetic[(Double, Double, Double, Double)] =
    new VectorArithmetic[(Double, Double, Double, Double)]:
      override def zero: (Double, Double, Double, Double) = (0, 0, 0, 0)

      override def subtract(
          lhs: (Double, Double, Double, Double),
          rhs: (Double, Double, Double, Double)
      ): (Double, Double, Double, Double) =
        (lhs._1 - rhs._1, lhs._2 - rhs._2, lhs._3 - rhs._3, lhs._4 - rhs._4)

      override def add(
          lhs: (Double, Double, Double, Double),
          rhs: (Double, Double, Double, Double)
      ): (Double, Double, Double, Double) =
        (lhs._1 + rhs._1, lhs._2 + rhs._2, lhs._3 + rhs._3, lhs._4 + rhs._4)

      override def scaledBy(lhs: (Double, Double, Double, Double), rhs: Double): (Double, Double, Double, Double) =
        (lhs._1 * rhs, lhs._2 * rhs, lhs._3 * rhs, lhs._4 * rhs)

      override def magnitudeSquared(value: (Double, Double, Double, Double)): Double =
        value._1 * value._1 + value._2 * value._2 + value._3 * value._3 + value._4 * value._4
