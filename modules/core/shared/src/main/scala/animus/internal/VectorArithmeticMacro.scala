package animus.internal

import animus.VectorArithmetic

import scala.quoted.*
import quotidian.*

// trait VectorArithmetic[A]:
//  def subtract(lhs: A, rhs: A): A
//  def add(lhs: A, rhs: A): A
//  def scaledBy(lhs: A, rhs: Double): A
//  def magnitudeSquared(value: A): Double
//  def zero: A

object VectorArithmeticMacro:
  inline def derive[A]: VectorArithmetic[A] = ${ deriveImpl[A] }

  def deriveImpl[A: Type](using Quotes): Expr[VectorArithmetic[A]] =
    import quotes.reflect.*
    val mirror = MacroMirror.summon[A].getOrElse {
      report.errorAndAbort(s"VectorArithmetic derivation requires that ${Type.show[A]} is a case class")
    }
    mirror match
      case productMirror: MacroMirror.Product[quotes.type, A] =>
        deriveProduct(productMirror)

      case sumMirror: MacroMirror.Sum[quotes.type, A] =>
        report.errorAndAbort(s"VectorArithmetic derivation for ${Type.show[A]} is not yet supported for sealed traits")

  def deriveProduct[A: Type](using Quotes)(mirror: MacroMirror.Product[quotes.type, A]): Expr[VectorArithmetic[A]] =
    import quotes.reflect.*

    def subtractElems(lhs: Expr[A], rhs: Expr[A]) =
      val results = mirror.elems.map { elem =>
        import elem.given
        val v = elem.summon[VectorArithmetic]
        val l = elem.get(lhs)
        val r = elem.get(rhs)
        '{ $v.subtract($l, $r) }.asTerm
      }
      mirror.construct(results)

    def addElems(lhs: Expr[A], rhs: Expr[A]) =
      val results = mirror.elems.map { elem =>
        import elem.given
        val v = elem.summon[VectorArithmetic]
        val l = elem.get(lhs)
        val r = elem.get(rhs)
        '{ $v.add($l, $r) }.asTerm
      }
      mirror.construct(results)

    def scaledByElem(lhs: Expr[A], rhs: Expr[Double]) =
      val results = mirror.elems.map { elem =>
        import elem.given
        val v = elem.summon[VectorArithmetic]
        val l = elem.get(lhs)
        '{ $v.scaledBy($l, $rhs) }.asTerm
      }
      mirror.construct(results)

    def magnitudeSquaredElem(value: Expr[A]) =
      val results = mirror.elems.map { elem =>
        import elem.given
        val v = elem.summon[VectorArithmetic]
        val l = elem.get(value)
        '{ $v.magnitudeSquared($l) }
      }
      results
        .reduceLeftOption { (l, r) =>
          '{ $l + $r }
        }
        .getOrElse('{ 0.0 })
        .asExprOf[Double]

    def zeroElem() =
      val results = mirror.elems.map { elem =>
        import elem.given
        val v = elem.summon[VectorArithmetic]
        '{ $v.zero }.asTerm
      }
      mirror.construct(results)

    '{
      new VectorArithmetic[A]:
        def subtract(lhs: A, rhs: A): A        = ${ subtractElems('lhs, 'rhs) }
        def add(lhs: A, rhs: A): A             = ${ addElems('lhs, 'rhs) }
        def scaledBy(lhs: A, rhs: Double): A   = ${ scaledByElem('lhs, 'rhs) }
        def magnitudeSquared(value: A): Double = ${ magnitudeSquaredElem('value) }
        def zero: A                            = ${ zeroElem() }
    }
