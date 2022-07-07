package com.raquo.airstream.core

import animus.Animatable
import com.raquo.airstream.common.SingleParentObservable
import org.scalajs.dom

import scala.util.Try

class SpringSignal[A](override protected val parent: Signal[A])(implicit animatable: Animatable[A])
    extends Signal[A]
    with WritableSignal[A]
    with SingleParentObservable[A, A] {

  private var anim: animatable.Anim = _
  private var animating             = false

  def tick(): Unit =
    dom.window.requestAnimationFrame(step)

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

  override protected[airstream] def initialValue: Try[A] = {
    val value = parent.tryNow()
    value.foreach { a =>
      anim = animatable.toAnim(a)
    }
    value
  }

  override protected[airstream] val topoRank: Int = Protected.topoRank(parent) + 1

  def fireQuick(value: A): Unit = {
    externalObservers.foreach(_.onNext(value))
    internalObservers.foreach(InternalObserver.onNext(_, value, null))
  }

  override protected[airstream] def onNext(nextValue: A, transaction: Transaction): Unit = {
    animatable.update(anim, nextValue)
    if (!animating) {
      animating = true
      tick()
    }
  }

  override protected[airstream] def onError(nextError: Throwable, transaction: Transaction): Unit = ()

  override protected[airstream] def onTry(nextValue: Try[A], transaction: Transaction): Unit =
    nextValue.foreach(onNext(_, null))
}
