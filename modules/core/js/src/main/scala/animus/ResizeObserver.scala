package animus

import com.raquo.airstream.core.Protected
import com.raquo.airstream.ownership.Subscription
import com.raquo.laminar.DomApi
import com.raquo.laminar.api.L
import com.raquo.laminar.api.L.*
import com.raquo.laminar.lifecycle.MountContext
import com.raquo.laminar.nodes.ReactiveElement
import org.scalajs.dom
import org.scalajs.dom.DOMRect

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
trait ResizeObserverEntry extends js.Object:
  def contentRect: DOMRect = js.native

@js.native
@JSGlobal
class ResizeObserver(callback: js.Function1[js.Array[ResizeObserverEntry], Unit]) extends js.Object:
  def observe(element: dom.Element): Unit   = js.native
  def unobserve(element: dom.Element): Unit = js.native

class ResizeListener(callback: DOMRect => Unit) extends Binder[ReactiveElement.Base]:
  override def bind(element: ReactiveElement.Base): DynamicSubscription =
    val observer = new ResizeObserver({ e =>
      e.headOption.foreach(e => callback(e.contentRect))
    })

    val subscribe = (ctx: MountContext[ReactiveElement.Base]) =>
      observer.observe(element.ref)
      callback(ctx.thisNode.ref.getBoundingClientRect())
      new Subscription(
        ctx.owner,
        cleanup = () => observer.unobserve(element.ref)
      )

    ReactiveElement.bindSubscriptionUnsafe(element)(subscribe)

object ResizeObserver:

  def -->(sink: Sink[DOMRect]): ResizeListener =
    this.-->(sink.toObserver.onNext(_))

  def -->(callback: DOMRect => Unit): ResizeListener =
    new ResizeListener(callback)

  def resize(element: Element): EventStream[DOMRect] =
    val bus = new EventBus[DOMRect]
    val observer = new ResizeObserver({ e =>
      e.headOption.foreach(e => bus.writer.onNext(e.contentRect))
    })
    observer.observe(element.ref)
    bus.events

  def resize(element: org.scalajs.dom.Element): EventStream[DOMRect] =
    val bus = new EventBus[DOMRect]
    val observer = new ResizeObserver({ e =>
      e.headOption.foreach(e => bus.writer.onNext(e.contentRect))
    })
    observer.observe(element)
    bus.events
