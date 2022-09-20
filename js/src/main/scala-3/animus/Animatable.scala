package animus

import magnolia1._

trait Animatable[A] {

  type Anim

  type A0 = A

  def toAnim(start: A, value: A): Anim

  def fromAnim(anim: Anim): A

  def tick(anim: Anim, time: Double): Boolean

  def update(anim: Anim, newValue: A): Unit
}

object Animatable extends AutoDerivation[Animatable] with AnimatableImplicits {

  def join[T](ctx: CaseClass[Animatable, T]): Animatable[T] =
    new Animatable[T] {
      override type Anim = Array[Any]

      override def toAnim(start: T, value: T): Array[Any] = {
        var i     = -1
        val anims = Array.ofDim[Any](ctx.params.length)
        ctx.params.foreach { param =>
          i += 1
          anims(i) = param.typeclass.toAnim(param.deref(start), param.deref(value))
        }
        anims
      }

      override def tick(anim: Array[Any], time: Double): Boolean = {
        var i       = -1
        var allDone = true
        ctx.params.foreach { param =>
          i += 1
          val typeclass = param.typeclass
          val paramDone = typeclass.tick(anim(i).asInstanceOf[typeclass.Anim], time)
          allDone = allDone && paramDone
        }
        allDone
      }

      override def update(anim: Array[Any], newValue: T): Unit = {
        var i = -1
        ctx.params.foreach { param =>
          i += 1
          val typeclass = param.typeclass
          typeclass.update(anim(i).asInstanceOf[typeclass.Anim], param.deref(newValue))
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

  override def split[T](ctx: SealedTrait[Animatable, T]): Animatable[T] =
    new Animatable[T] {
      override type Anim = Array[Any]

      /* TODO: assumes toAnim is always called before tick, update, and fromAnim or
       *  won't work by saving the value and using it to multiplex!  Is this OK? */
      var valueOpt: Option[T] = None

      override def toAnim(start: T, value: T): Array[Any] = {
        valueOpt = Some(value)
        ctx.choose(value)(sub => sub.typeclass.toAnim(sub.cast(start), sub.cast(value)).asInstanceOf[Anim])
      }

      override def tick(anim: Array[Any], time: Double): Boolean =
        ctx.choose(valueOpt.get) { sub =>
          val animatable = sub.typeclass
          animatable.tick(anim.asInstanceOf[animatable.Anim], time)
        }

      override def update(anim: Array[Any], newValue: T): Unit =
        ctx.choose(valueOpt.get) { sub =>
          val animatable = sub.typeclass
          animatable.update(anim.asInstanceOf[animatable.Anim], newValue.asInstanceOf[animatable.A0])
        }

      override def fromAnim(anim: Array[Any]): T =
        ctx.choose(valueOpt.get) { sub =>
          val animatable = sub.typeclass
          animatable.fromAnim(anim.asInstanceOf[animatable.Anim])
        }
    }
}
