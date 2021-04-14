package com.raquo.airstream.animus

import animus.Animatable
import com.raquo.airstream.common.SingleParentObservable
import com.raquo.airstream.core.{Signal, Transaction}
import org.scalajs.dom

import scala.scalajs.js
import scala.util.Try

object RequestAnimationFrame {
  val tickers: js.Array[Double => Unit] = scalajs.js.Array()

  def tick(): Unit = dom.window.requestAnimationFrame(step)

  val step: scalajs.js.Function1[Double, Unit] = (t: Double) => {
    tickers.foreach(_.apply(t))
    tick()
  }

  def add(ticker: Double => Unit): Unit = tickers += ticker

  tick()
}

class SpringSignal[A](val parent: Signal[A])(implicit animatable: Animatable[A])
    extends Signal[A]
    with SingleParentObservable[A, A] {

  override protected[this] def initialValue: Try[A] = {
    val value = parent.tryNow()
    value.foreach { a =>
      anim = animatable.toAnim(a)
    }
    value
  }

  override val topoRank: Int = parent.topoRank + 1

  private var anim: animatable.Anim = _
  private var animating             = false

  def fireQuick(value: A): Unit = {
    externalObservers.foreach { observer =>
      observer.onNext(value)
    }

    internalObservers.foreach { observer =>
      observer.onNext(value, null)
    }
  }

  override def onNext(nextValue: A, transaction: Transaction): Unit = {
    animatable.update(anim, nextValue)
    if (!animating) {
      animating = true
      RequestAnimationFrame.add { t =>
        animatable.tick(anim, t)
        fireQuick(animatable.fromAnim(anim))
      }
    }
  }

  override protected[airstream] def onError(nextError: Throwable, transaction: Transaction): Unit = ()

  override protected[airstream] def onTry(nextValue: Try[A], transaction: Transaction): Unit =
    nextValue.foreach(onNext(_, null))
}

object SpringSignal {
  implicit final class SignalOps[A](val signal: Signal[A]) extends AnyVal {
    def sprinkle(implicit animatable: Animatable[A]): Signal[A] =
      new SpringSignal[A](signal)
  }
}
