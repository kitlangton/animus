package animus

import magnolia1._

object DeriveAnimatable {
  type Typeclass[T] = Animatable[T]

  def join[T](ctx: CaseClass[Animatable, T]): Animatable[T] =
    new Animatable[T] {
      override type Anim = Array[Any]

      override def toAnim(value: T, configure: Spring => Spring): Array[Any] = {
        var i     = -1
        val anims = Array.ofDim[Any](ctx.parameters.length)
        ctx.parameters.foreach { param =>
          i += 1
          anims(i) = param.typeclass.toAnim(param.dereference(value), configure)
        }
        anims
      }

      override def tick(anim: Array[Any], time: Double): Boolean = {
        var i       = -1
        var allDone = true
        ctx.parameters.foreach { param =>
          i += 1
          val typeclass = param.typeclass
          val paramDone = typeclass.tick(anim(i).asInstanceOf[typeclass.Anim], time)
          allDone = allDone && paramDone
        }
        allDone
      }

      override def update(anim: Array[Any], newValue: T): Unit = {
        var i = -1
        ctx.parameters.foreach { param =>
          i += 1
          val typeclass = param.typeclass
          typeclass.update(anim(i).asInstanceOf[typeclass.Anim], param.dereference(newValue))
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

  implicit def gen[T]: Animatable[T] = macro Magnolia.gen[T]
}
