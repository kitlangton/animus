package com.raquo.airstream.core

import animus.Animator
import org.scalajs.dom.window.requestAnimationFrame

object AnimationManager {
  private val animations       = scalajs.js.Map.empty[Int, Animator[_]]
  private var animating        = false
  private var animationId: Int = 0
  private def nextAnimationId(): Int = {
    animationId += 1
    animationId
  }

  def addAnimation[V](spring: Animator[V]): Int = {
    val id = nextAnimationId()
    animations += (id -> spring)
    if (!animating) {
      animating = true
      val _ = requestAnimationFrame(tick(_))
    }
    id
  }

  def removeAnimation(id: Int): Unit = {
    val _ = animations.delete(id)
  }

  val tick: scalajs.js.Function1[Double, Unit] = _ => {
    animations.foreach { case (id, spring) =>
      spring.update()
      if (spring.isDone) {
        val _ = animations.delete(id)
      }
    }

    if (animations.nonEmpty) {
      val _ = requestAnimationFrame(tick(_))
    } else {
      animating = false
    }
  }

}
