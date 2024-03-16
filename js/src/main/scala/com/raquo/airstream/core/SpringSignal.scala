package com.raquo.airstream.core

import animus.{Animatable, Spring}
import com.raquo.airstream.common.SingleParentSignal
import org.scalajs.dom.window.requestAnimationFrame
import animus.VectorArithmetic

import scala.util.Try
import animus.SpringConfig

class SpringSignal[A](
  override protected val parent: Signal[A],
  configureSpring: SpringConfig[A] => SpringConfig[A]
)(implicit
  vectorArithmetic: VectorArithmetic[A]
) extends Signal[A]
    with WritableSignal[A]
    with SingleParentSignal[A, A] {

  private var spring: SpringConfig[A] = _
  private var animating               = false

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
      val isDone = spring.update(t)
      fireQuick(spring.value)
      if (isDone) { animating = false }
      requestAnimationFrame(step)
    }

  override protected def currentValueFromParent(): Try[A] = {
    val value = parent.tryNow()
    value.foreach { a =>
      spring = configureSpring(SpringConfig.make(a))
    }
    value
  }

  override protected[airstream] val topoRank: Int = Protected.topoRank(parent) + 1

  def fireQuick(value: A): Unit =
    new Transaction(fireValue(value, _))

  override protected[airstream] def onTry(nextValue: Try[A], transaction: Transaction): Unit =
    nextValue.foreach { a =>
      spring.setTarget(a)
      if (!animating) {
        animating = true
        tick()
      }
    }

}
