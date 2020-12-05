package animus

import com.raquo.airstream.eventstream.EventStream
import org.scalajs.dom

object RAFStream extends EventStream[Double] {
  override val topoRank: Int = 1

  var started = false

  def tick(): Int =
    dom.window.requestAnimationFrame(step)

  def step(t: Double): Unit = {
    fireValue(t, null)
    tick()
  }

  override protected[this] def onStart(): Unit =
    if (!started) {
      started = true
      tick()
    }

  override protected[this] def onStop(): Unit = {
//    started = false
  }
}
