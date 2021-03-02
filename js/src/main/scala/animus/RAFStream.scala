package animus

import com.raquo.airstream.core.EventStream
import org.scalajs.dom

object RAFStream extends EventStream[Double] {
  override val topoRank: Int = 1

  var started = false

  def tick(): Unit = {
    dom.window.requestAnimationFrame(step(_))
    ()
  }

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
