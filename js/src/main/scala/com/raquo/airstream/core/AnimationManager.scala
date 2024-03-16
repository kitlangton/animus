package com.raquo.airstream.core

import animus.SpringConfig
import org.scalajs.dom.window.requestAnimationFrame

object AnimationManager {
  var animating = false

  var animationId: Int = 0
  def nextAnimationId(): Int = {
    animationId += 1
    animationId
  }

  val animations = scalajs.js.Map.empty[Int, SpringConfig[_]]

  def addAnimation[V](spring: SpringConfig[V]): Int = {
    val id = nextAnimationId()
    animations += (id -> spring)
    if (!animating) {
      animating = true
      requestAnimationFrame(tick)
    }
    id
  }

  def removeAnimation(id: Int): Unit =
    animations.delete(id)

  private def tick(t: Double): Unit = {
    animations.foreach { case (id, spring) =>
      spring.update(t)
      if (spring.isDone) {
        animations.delete(id)
      }
    }
    // Continue requesting frames or stop if no animations are left
    if (animations.nonEmpty) requestAnimationFrame(tick)
    else animating = false
  }

}
