package com.raquo.airstream.core

import animus.Animator
import org.scalajs.dom.window.requestAnimationFrame

object AnimationManager:
  private val animations       = scalajs.js.Map.empty[Int, Animator[_]]
  private var animating        = false
  private var animationId: Int = 0
  private def nextAnimationId(): Int =
    animationId += 1
    animationId

  def addAnimation[V](spring: Animator[V]): Int =
    val id = nextAnimationId()
    animations += (id -> spring)
    if !animating then
      animating = true
      val _ = requestAnimationFrame(tick(_))
    id

  def removeAnimation(id: Int): Unit =
    val _ = animations.delete(id)

  var lastTick: Double = 0

  val max = 1000.0 / 30

  val tick: scalajs.js.Function1[Double, Unit] = n =>
    val delta: Double        = (n - lastTick) min max
    val deltaSeconds: Double = delta / 1_000.0
    lastTick = n

    animations.foreach { case (id, spring) =>
      spring.update(deltaSeconds)
      if spring.isDone then
        val _ = animations.delete(id)
    }

    if animations.nonEmpty then
      val _ = requestAnimationFrame(tick(_))
    else animating = false
