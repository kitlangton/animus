package com.raquo.airstream.core

import animus.{Animatable, Spring}
import com.raquo.airstream.common.SingleParentSignal
import org.scalajs.dom.window.requestAnimationFrame

import scala.util.Try

class SpringSignal[A](override protected val parent: Signal[A], configureSpring: Spring => Spring)(implicit
  animatable: Animatable[A]
) extends Signal[A]
    with WritableSignal[A]
    with SingleParentSignal[A, A] {

  private var anim: animatable.Anim = _
  private var animating             = false

  def tick(): Unit = {
    val _ = requestAnimationFrame(step)
  }

  override def onStart(): Unit = {
    super.onStart()
    if (!animating) {
      animating = true
      tick()
    }
  }

  override def onStop(): Unit = {
    super.onStop()
    animating = false
  }

  private val step: scalajs.js.Function1[Double, Unit] = (t: Double) =>
    if (animating) {
      val isDone = animatable.tick(anim, t)
      fireQuick(animatable.fromAnim(anim))
      if (isDone) {
        animating = false
      }
      tick()
    }

  override protected def currentValueFromParent(): Try[A] = {
    val value = parent.tryNow()
    value.foreach { a =>
      anim = animatable.toAnim(a, configureSpring)
    }
    value
  }

  override protected[airstream] val topoRank: Int = Protected.topoRank(parent) + 1

  def fireQuick(value: A): Unit =
    new Transaction(fireValue(value, _))

  override protected[airstream] def onTry(nextValue: Try[A], transaction: Transaction): Unit =
//    println(s"onTry ${nextValue}}")
    nextValue.foreach { a =>
      animatable.update(anim, a)
      if (!animating) {
        animating = true
        tick()
      }
    }

}
