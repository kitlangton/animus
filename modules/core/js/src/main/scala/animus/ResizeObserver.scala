package animus

import com.raquo.laminar.api.L._
import org.scalajs.dom
import org.scalajs.dom.DOMRect

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
trait ResizeObserverEntry extends js.Object {
  def contentRect: DOMRect = js.native
}

@js.native
@JSGlobal
class ResizeObserver(callback: js.Function1[js.Array[ResizeObserverEntry], Unit]) extends js.Object {
  def observe(element: dom.Element): Unit = js.native
}

object ResizeObserver {
  def resize(element: Element): EventStream[DOMRect] = {
    val bus = new EventBus[DOMRect]
    val observer = new ResizeObserver({ e =>
      e.headOption.foreach(e => bus.writer.onNext(e.contentRect))
    })
    observer.observe(element.ref)
    bus.events
  }

  def resize(element: org.scalajs.dom.Element): EventStream[DOMRect] = {
    val bus = new EventBus[DOMRect]
    val observer = new ResizeObserver({ e =>
      e.headOption.foreach(e => bus.writer.onNext(e.contentRect))
    })
    observer.observe(element)
    bus.events
  }
}
