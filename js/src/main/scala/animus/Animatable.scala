package animus

import magnolia._

trait Animatable[A] {
  type Anim

  def tickAnim(anim: Anim, time: Double): Boolean

  def toAnim(value: A): Anim

  def updateAnim(anim: Anim, newValue: A): Unit

  def fromAnim(anim: Anim): A
}

object Animatable {
  implicit val animatableDouble: Animatable[Double] = new Animatable[Double] {
    override type Anim = Spring

    override def tickAnim(anim: Spring, time: Double): Boolean = {
      anim.tick(time)
      anim.isDone
    }

    override def toAnim(value: Double): Spring = Spring.fromValue(value)

    override def updateAnim(anim: Spring, newValue: Double): Unit = anim.setTarget(newValue)

    override def fromAnim(anim: Spring): Double = anim.value
  }

  implicit def animatableTuple[A, B](implicit a: Animatable[A], b: Animatable[B]): Animatable[(A, B)] =
    new Animatable[(A, B)] {
      override type Anim = (a.Anim, b.Anim)

      override def tickAnim(anim: (a.Anim, b.Anim), time: Double): Boolean = {
        val d1 = a.tickAnim(anim._1, time)
        val d2 = b.tickAnim(anim._2, time)
        d1 && d2
      }

      override def toAnim(value: (A, B)): (a.Anim, b.Anim) =
        (a.toAnim(value._1), b.toAnim(value._2))

      override def updateAnim(anim: (a.Anim, b.Anim), newValue: (A, B)): Unit = {
        a.updateAnim(anim._1, newValue._1)
        b.updateAnim(anim._2, newValue._2)
      }

      override def fromAnim(anim: (a.Anim, b.Anim)): (A, B) = (a.fromAnim(anim._1), b.fromAnim(anim._2))
    }

  implicit def animatableTuple3[A, B, C](implicit
      a: Animatable[A],
      b: Animatable[B],
      c: Animatable[C]
  ): Animatable[(A, B, C)] =
    new Animatable[(A, B, C)] {
      override type Anim = (a.Anim, b.Anim, c.Anim)

      override def tickAnim(anim: (a.Anim, b.Anim, c.Anim), time: Double): Boolean = {
        val d1 = a.tickAnim(anim._1, time)
        val d2 = b.tickAnim(anim._2, time)
        val d3 = c.tickAnim(anim._3, time)
        d1 && d2 && d3
      }

      override def toAnim(value: (A, B, C)): (a.Anim, b.Anim, c.Anim) =
        (a.toAnim(value._1), b.toAnim(value._2), c.toAnim(value._3))

      override def updateAnim(anim: (a.Anim, b.Anim, c.Anim), newValue: (A, B, C)): Unit = {
        a.updateAnim(anim._1, newValue._1)
        b.updateAnim(anim._2, newValue._2)
        c.updateAnim(anim._3, newValue._3)
      }

      override def fromAnim(anim: (a.Anim, b.Anim, c.Anim)): (A, B, C) =
        (a.fromAnim(anim._1), b.fromAnim(anim._2), c.fromAnim(anim._3))
    }

}

object DeriveAnimatable {
  type Typeclass[T] = Animatable[T]

  def combine[T](ctx: CaseClass[Animatable, T]): Animatable[T] =
    new Animatable[T] {
      override type Anim = Array[Any]

      override def toAnim(value: T): Array[Any] = {
        var i     = -1
        val anims = Array.ofDim[Any](ctx.parameters.length)
        ctx.parameters.foreach { param =>
          i += 1
          anims(i) = param.typeclass.toAnim(param.dereference(value))
        }
        anims
      }

      override def tickAnim(anim: Array[Any], time: Double): Boolean = {
        var i       = -1
        var allDone = true
        ctx.parameters.foreach { param =>
          i += 1
          val typeclass = param.typeclass
          val paramDone = typeclass.tickAnim(anim(i).asInstanceOf[typeclass.Anim], time)
          allDone = allDone && paramDone
        }
        allDone
      }

      override def updateAnim(anim: Array[Any], newValue: T): Unit = {
        var i = -1
        ctx.parameters.foreach { param =>
          i += 1
          val typeclass = param.typeclass
          typeclass.updateAnim(anim(i).asInstanceOf[typeclass.Anim], param.dereference(newValue))
        }
      }

      override def fromAnim(anim: Array[Any]): T = {
        var i = -1
        ctx.construct { param =>
          i += 1
          val animatable = param.typeclass
          val value      = animatable.fromAnim(anim(i).asInstanceOf[animatable.Anim])
          value
        }
      }

    }

  implicit def gen[T]: Typeclass[T] = macro Magnolia.gen[T]
}
