package pixi

import com.raquo.airstream.core.{Observable, Observer}
import com.raquo.airstream.eventbus.EventBus
import com.raquo.airstream.eventstream.EventStream
import com.raquo.airstream.ownership._
import com.raquo.airstream.signal.Signal
import pixi.Builders.container
import pixi.ChildrenInserter.{Child, Children}
import pixi.PIXI.{Graphics, InteractionEvent}
//import Builders.container
//import ChildrenInserter.{Child, Children}
//import GameDesigner.Coord
//import GameDesignerState.SpriteId
import scala.collection.{immutable, mutable}
import scala.scalajs.js
import scala.scalajs.js.UndefOr

object Color {
  def black: Color = Color(0x000000)
  def red: Color   = Color(0xff0000)
  def blue: Color  = Color(0x0000ff)
  def white: Color = Color(0xffffff)

  def fromBrightness(d: Double): Color = Color((d * 255).toInt + ((d * 255).toInt << 8) + ((d * 255).toInt << 16))
}

case class Color(hex: Int)

trait ReactivePixi[+Ref <: PIXI.Container] extends Modifier[ReactivePixi[PIXI.Container]] { self =>
  val ref: Ref

  val dynamicOwner: DynamicOwner = new DynamicOwner(() => ())

  private val pilotSubscription: TransferableSubscription =
    new TransferableSubscription(
      activate = () => dynamicOwner.activate,
      deactivate = () => dynamicOwner.deactivate
    )

  var children: mutable.Buffer[ReactivePixi.Base] =
    mutable.Buffer.empty[ReactivePixi.Base]

  var maybeParent: Option[ReactivePixi.Base] = None

  def activate(): Unit = {
    println(s"ACTIVATING $self")
    dynamicOwner.activate()
  }

  def replaceChild(
      oldChild: Option[ReactivePixi.Base],
      newChild: ReactivePixi.Base
  ): Unit = {
    oldChild match {
      case Some(oldChild) if oldChild != newChild =>
        val indexOfChild = children.indexOf(oldChild)
        if (indexOfChild != -1) {
          oldChild.willSetParent(None)
          newChild.willSetParent(Some(self))

          ref.removeChild(oldChild.ref)
          ref.addChild(newChild.ref)

          // 2. Update this node
          children.update(indexOfChild, newChild)

          // 3. Update children
          oldChild.setParent(None)
          newChild.setParent(Some(self))
        }
      case None =>
        appendChild(newChild)
    }

  }

  def appendChild(
      child: ReactivePixi.Base
  ): Unit = {
    child.willSetParent(Some(self))
    ref.addChild(child.ref)
    children += child
    child.setParent(Some(self))
  }

  def removeChild(
      child: ReactivePixi.Base
  ): Unit = {
    child.willSetParent(None)
    ref.removeChild(child.ref)
    children -= child
    child.setParent(None)
  }

  override def apply(parentNode: ReactivePixi.Base): Unit = {
    parentNode.appendChild(child = self)
  }

  @inline def bindFn[V](
      observable: Observable[V]
  )(
      onNext: V => Unit
  ): DynamicSubscription = {
    DynamicSubscription.subscribeFn(this.dynamicOwner, observable, onNext)
  }

  @inline def bindCallback[El <: ReactivePixi.Base](
      element: El
  )(
      activate: (El, Owner) => Unit
  ): DynamicSubscription = {
    DynamicSubscription.subscribeCallback(
      element.dynamicOwner,
      owner => {
        activate(element, owner)
      }
    )
  }
  @inline def bindSubscription[El <: ReactivePixi.Base](
      element: El
  )(
      subscribe: (El, Owner) => Subscription
  ): DynamicSubscription = {
    DynamicSubscription(
      element.dynamicOwner,
      { owner =>
        subscribe(element, owner)
      }
    )
  }

  def willSetParent(maybeNextParent: Option[ReactivePixi.Base]): Unit = {
    //println(s"> willSetParent of ${this.ref.tagName} to ${maybeNextParent.map(_.ref.tagName)}")

    // @Note this should cover ALL cases not covered by setParent
    if (isUnmounting(maybePrevParent = maybeParent, maybeNextParent = maybeNextParent)) {
      setPilotSubscriptionOwner(maybeNextParent)
    }
  }

  /** Don't call setParent directly – willSetParent will not be called. Use methods like `appendChild` defined on `ParentNode` instead. */
  private def setParent(maybeNextParent: Option[ReactivePixi.Base]): Unit = {
    //println(s"> setParent of ${this.ref.tagName} to ${maybeNextParent.map(_.ref.tagName)}")

    val maybePrevParent = maybeParent
    maybeParent = maybeNextParent

    // @Note this should cover ALL cases not covered by willSetParent
    if (!isUnmounting(maybePrevParent = maybePrevParent, maybeNextParent = maybeNextParent)) {
      setPilotSubscriptionOwner(maybeNextParent)
    }
  }

  private[this] def isUnmounting(
      maybePrevParent: Option[ReactivePixi.Base],
      maybeNextParent: Option[ReactivePixi.Base]
  ): Boolean = {
    val isPrevParentActive = maybePrevParent.exists(_.dynamicOwner.isActive)
    val isNextParentActive = maybeNextParent.exists(_.dynamicOwner.isActive)
    isPrevParentActive && !isNextParentActive
  }

  private[this] def setPilotSubscriptionOwner(maybeNextParent: Option[ReactivePixi.Base]): Unit = {
    println(
      " - setPilotSubscriptionOwner of " + this + " (active = " + dynamicOwner.isActive + ") to " + maybeNextParent
    )

    maybeNextParent.fold(pilotSubscription.clearOwner) { nextParent =>
      pilotSubscription.setOwner(nextParent.dynamicOwner)
    }
  }

  private var eventListeners: mutable.Buffer[EventPropBinder] =
    mutable.Buffer.empty

  def indexOfEventListener(listener: EventPropBinder): Int = {
    val notFoundIndex = -1
    if (eventListeners.isEmpty) {
      notFoundIndex
    } else {
      var found = false
      var index = 0
      while (!found && index < eventListeners.length) {
        if (listener equals eventListeners(index)) {
          found = true
        } else {
          index += 1
        }
      }
      if (found) index else notFoundIndex
    }
  }

  // TODO: Add Pixi Events
  def addEventListener[Ev](listener: EventPropBinder): Boolean = {
    val shouldAddListener = indexOfEventListener(listener) == -1
    if (shouldAddListener) {
      // 1. Update this node
      if (eventListeners.isEmpty) {
        eventListeners = mutable.Buffer(listener)
      } else {
        eventListeners += listener
      }
      // 2. Update the DOM
      ref.interactive = true
      ref.on(listener.key, listener.domValue)
      //      DomApi.addEventListener(self, listener)
    }
    shouldAddListener
  }

  /** @return Whether listener was removed (false if such a listener was not found) */
  def removeEventListener[Ev](listener: EventPropBinder): Boolean = {
    val index                = indexOfEventListener(listener)
    val shouldRemoveListener = index != -1
    if (shouldRemoveListener) {
      // 1. Update this node
      eventListeners.remove(index)
      // 2. Update the DOM
      if (eventListeners.isEmpty) {
        ref.interactive = false
      }
      // TODO : REMOVE LISTENER?
      //      ref.interactive = true
      //      ref.li
      //      DomApi.removeEventListener(element, listener)
    }
    shouldRemoveListener
  }
}

object ReactivePixi {
  type Base = ReactivePixi[PIXI.Container]
}

trait Modifier[-El] {
  def apply(element: El): Unit = ()
}

object Modifier {
  def apply[El](f: El => Unit): Modifier[El] = new Modifier[El] {
    override def apply(element: El): Unit = f(element)
  }
}

trait PixiBuilder[+Ref <: PIXI.Container] {
  def apply(modifiers: Modifier[ReactivePixi[Ref]]*): ReactivePixi[Ref] = {
    val element = build()
    modifiers.foreach(modifier => modifier(element))
    element
  }

  /** Create a Scala DOM Builder element from this Tag */
  protected def build(): ReactivePixi[Ref]
}

object Builders {
  object sprite extends PixiBuilder[PIXI.Sprite] {
    override protected def build(): ReactivePixi[PIXI.Sprite] =
      new ReactivePixi[PIXI.Sprite] {
        override val ref: PIXI.Sprite = new PIXI.Sprite()
      }
  }

  object graphics extends PixiBuilder[PIXI.Graphics] {
    override protected def build(): ReactivePixi[PIXI.Graphics] =
      new ReactivePixi[PIXI.Graphics] {
        override val ref: PIXI.Graphics = new PIXI.Graphics()
      }
  }

  object container extends PixiBuilder[PIXI.Container] {
    override protected def build(): ReactivePixi[PIXI.Container] =
      new ReactivePixi[PIXI.Container] {
        override val ref: PIXI.Container = new PIXI.Container()
      }
  }

  case class textSprite(string: String, fontSize0: Int = 18, fill0: Color = Color.white)
      extends PixiBuilder[PIXI.Text] {
    override protected def build(): ReactivePixi[PIXI.Text] =
      new ReactivePixi[PIXI.Text] {
        override val ref: PIXI.Text = new PIXI.Text(
          string,
          new PIXI.TextStyle {
            override val fill: UndefOr[Double]  = js.defined(fill0.hex)
            override val fontSize: UndefOr[Int] = js.defined(fontSize0)
          }
        )
      }
  }

  val $ticker = new EventBus[Double]

  PIXI.Ticker.shared.add(t => $ticker.writer.onNext(t))

  def hstack(modifiers: Modifier[ReactivePixi[PIXI.Container]]*): ReactivePixi[PIXI.Container] = container(
    Seq(Props.inContext { (el: ReactivePixi[PIXI.Container]) =>
      $ticker.events --> Observer { (_: Double) =>
        var width = 0.0
        el.ref.children.foreach { child =>
          val rect: PIXI.Rectangle = child.getBounds()
          child.x = width
          width += rect.width
        }
      }
    }) ++
      modifiers: _*
  )

  def vstack(modifiers: Modifier[ReactivePixi[PIXI.Container]]*): ReactivePixi[PIXI.Container] = container(
    Seq(Props.inContext { (el: ReactivePixi[PIXI.Container]) =>
      $ticker.events --> Observer { (_: Double) =>
        var height = 0.0
        el.ref.children.foreach { child =>
          val rect: PIXI.Rectangle = child.getBounds()
          child.y = height
          height += rect.height
        }
      }
    }) ++
      modifiers: _*
  )

  implicit class RichSignal[A](val signal: Signal[A]) extends AnyVal {
    def -->(observer: Observer[A]): PixiBinder[ReactivePixi.Base] = { el =>
      el.bindFn(signal)(observer.onNext)
    }

    def -->(onNext: A => Unit): PixiBinder[ReactivePixi.Base] = {
      PixiBinder(_.bindFn(signal)(onNext))
    }
  }

  implicit class RichEventStream[A](val signal: EventStream[A]) extends AnyVal {
    def -->(observer: Observer[A]): PixiBinder[ReactivePixi.Base] = { el =>
      el.bindFn(signal)(observer.onNext)
    }

    def -->(onNext: A => Unit): PixiBinder[ReactivePixi.Base] = {
      PixiBinder(_.bindFn(signal)(onNext))
    }
  }
}

trait ReactivePixiProp[Ref <: PIXI.Container, V] {
  def set(element: Ref, value: V)

  @inline def apply(value: V): Modifier[ReactivePixi[Ref]] = {
    this := value
  }

  def maybe(value: Option[V]): Modifier[ReactivePixi[Ref]] =
    value match {
      case Some(value) =>
        Modifier { el =>
          set(el.ref, value)
        }
      case None =>
        Modifier { el =>
          el
        }
    }

  def :=(value: V): Modifier[ReactivePixi[Ref]] = Modifier { el =>
    set(el.ref, value)
  }

  def <--($value: Observable[V]): PixiBinder[ReactivePixi[Ref]] = {
    PixiBinder { element =>
      element.bindFn($value) { value =>
        set(element.ref, value)
      }
    }
  }
}

object Props {
  val draw: ReactivePixiProp[PIXI.Graphics, Drawing] = (graphics, d) => Drawing.render(d, graphics)

  val x: ReactivePixiProp[PIXI.Container, Double]      = _.x = _
  val y: ReactivePixiProp[PIXI.Container, Double]      = _.y = _
  val alpha: ReactivePixiProp[PIXI.Container, Double]  = _.alpha = _
  val width: ReactivePixiProp[PIXI.Container, Double]  = _.width = _
  val height: ReactivePixiProp[PIXI.Container, Double] = _.scale.x = _
  val scaleX: ReactivePixiProp[PIXI.Container, Double] = _.scale.y = _
  val scaleY: ReactivePixiProp[PIXI.Container, Double] = _.scale.y = _
  val scale: ReactivePixiProp[PIXI.Container, Double] = (container, d) => {
    container.scale.x = d
    container.scale.y = d
  }

  val zIndex: ReactivePixiProp[PIXI.Container, Double] = _.zIndex = _

  val sortableChildren: ReactivePixiProp[PIXI.Container, Boolean] = _.sortableChildren = _

  val mask: ReactivePixiProp[PIXI.Container, PIXI.Container] = _.mask = _

  // Paragraph
  val text: ReactivePixiProp[PIXI.Text, String] = _.text = _
  //  val fontSize: ReactivePixiProp[PIXI.Paragraph, Double]  = _. = _

  // Sprite
  val buttonMode: ReactivePixiProp[PIXI.Sprite, Boolean]     = _.buttonMode = _
  val interactive: ReactivePixiProp[PIXI.Container, Boolean] = _.interactive = _

  object anchor {
    val x: ReactivePixiProp[PIXI.Sprite, Double]    = _.anchor.x = _
    val y: ReactivePixiProp[PIXI.Sprite, Double]    = _.anchor.y = _
    val coord: ReactivePixiProp[PIXI.Sprite, Coord] = (sprite, coord) => sprite.anchor.set(coord.x, coord.y)
    val both: ReactivePixiProp[PIXI.Sprite, Double] = _.anchor.set(_)

    val center: Modifier[ReactivePixi[PIXI.Sprite]] = both := 0.5
  }

  val children: ChildrenReceiver.type = ChildrenReceiver
  val child: ChildReceiver.type       = ChildReceiver

//  val positionAndOrientation: ReactivePixiProp[PIXI.Sprite, (Coord, Orientation)] =
//    (element: PIXI.Sprite, posAndOr: (Coord, Orientation)) => Sprites.positionSprite(posAndOr._1, posAndOr._2, element)

  def inContext[El <: ReactivePixi.Base](makeModifier: El => Modifier[El]): Modifier[El] = {
    new Modifier[El] {
      override def apply(element: El): Unit =
        makeModifier(element).apply(element)
    }
  }

  def onMountCallback[El <: ReactivePixi.Base](fn: El => Unit): Modifier[El] = {
    new Modifier[El] {
      override def apply(element: El): Unit = {
        var ignoreNextActivation = element.dynamicOwner.isActive
        element.bindCallback[El](element) { (element, owner) =>
          if (ignoreNextActivation) {
            ignoreNextActivation = false
          } else {
            fn(element)
          }
        }
      }
    }
  }

  @inline def onMountBind[El <: ReactivePixi.Base](fn: El => PixiBinder[El]): Modifier[El] = {
    var maybeSubscription: Option[DynamicSubscription] = None
    onMountUnmountCallback(
      mount = { element =>
        val binder = fn(element)
        maybeSubscription = Some(binder.bind(element))
      },
      unmount = { _ =>
        maybeSubscription.foreach(_.kill())
        maybeSubscription = None
      }
    )
  }

  def onMountUnmountCallback[El <: ReactivePixi.Base](mount: El => Unit, unmount: El => Unit): Modifier[El] = {
    new Modifier[El] {
      override def apply(element: El): Unit = {
        var ignoreNextActivation = element.dynamicOwner.isActive
        element.bindSubscription[El](element) { (c, owner) =>
          if (ignoreNextActivation) {
            ignoreNextActivation = false
          } else {
            mount(c)
          }
          new Subscription(owner, cleanup = () => unmount(element))
        }
      }
    }
  }

}

trait PixiBinder[-El <: ReactivePixi.Base] extends Modifier[El] {
  def bind(element: El): DynamicSubscription

  override def apply(element: El): Unit = {
    bind(element)
  }
}

object PixiBinder {
  def apply[El <: ReactivePixi.Base](fn: El => DynamicSubscription): PixiBinder[El] = { (element: El) =>
    fn(element)
  }
}

object ChildrenReceiver {
  def <--($children: Observable[Children]): Modifier[ReactivePixi.Base] = {
    ChildrenInserter[ReactivePixi.Base]($children)
  }
}

object ChildReceiver {
  val maybe: MaybeChildReceiver.type = MaybeChildReceiver

  def <--($child: Observable[Child]): Modifier[ReactivePixi.Base] = {
    ChildInserter[ReactivePixi.Base]($child)
  }
}

object ChildrenInserter {

  type Child = ReactivePixi.Base

  type Children = immutable.Seq[Child]

  def apply[El <: ReactivePixi.Base](
      $children: Observable[Children]
  ): Modifier[El] = new Modifier[El] {
    var previousChildren: mutable.Buffer[Child] = mutable.Buffer.empty[Child]

    override def apply(element: El): Unit = {
      element.bindFn($children) { nextChildren =>
        // Add new children
        nextChildren.foreach { child =>
          if (!previousChildren.exists(_.ref == child.ref)) {
            println(s"ADDING CHILd $child")
            element.appendChild(child)
          }
        }

        // Remove missing children
        val removedChildren = previousChildren.filter(prevChild => !nextChildren.exists(_.ref == prevChild.ref))
        removedChildren.foreach(element.removeChild)

        previousChildren = nextChildren.toBuffer
      }
    }
  }
}

object ChildInserter {
  def apply[El <: ReactivePixi.Base](
      $child: Observable[ReactivePixi.Base]
  ): Modifier[El] = new Modifier[El] {
    override def apply(element: El): Unit = {
      var lastChild: Option[ReactivePixi.Base] = None
      element.bindFn($child) { nextChild =>
        element.replaceChild(lastChild, nextChild)
        lastChild = Some(nextChild)
      }
    }
  }
}

object MaybeChildReceiver {
  def <--($maybeChildNode: Observable[Option[Child]]): Modifier[ReactivePixi.Base] = {
    val emptyNode = container()
    ChildInserter[ReactivePixi.Base]($maybeChildNode.map(_.getOrElse(emptyNode)))
  }
}

object Implicits {
  implicit def seqToModifier[A, El <: ReactivePixi.Base](
      modifiers: scala.collection.Seq[A]
  )(implicit evidence: A => Modifier[El]): Modifier[El] = {
    // @TODO[Performance] See if we might want a separate implicit conversion for cases when we don't need `evidence`
    new Modifier[El] {
      override def apply(element: El): Unit = {
        modifiers.foreach(evidence(_).apply(element))
      }
    }
  }
}

class EventProp(key: String) {
  @inline def -->[El <: ReactivePixi.Base](observer: Observer[InteractionEvent]): EventPropBinder = {
    EventPropBinder(observer, key)
  }

  @inline def -->[El <: ReactivePixi.Base](onNext: InteractionEvent => Unit): EventPropBinder = {
    -->(Observer(onNext))
  }
}

object EventProp {
  val pointerDown = new EventProp("pointerdown")
  val pointerOver = new EventProp("pointerover")
  val pointerOut  = new EventProp("pointerout")
  val pointerMove = new EventProp("pointermove")
  val mouseDown   = new EventProp("mousedown")
  val mouseMove   = new EventProp("mousemove")
}

class EventPropBinder(
    val key: String,
    val value: InteractionEvent => Unit
) extends PixiBinder[ReactivePixi.Base] {

  /** To make sure that you remove the event listener successfully in JS DOM, you need to
    * provide the same Javascript callback function that was originally added as a listener.
    * However, the implicit conversion from a Scala function to a JS function creates a new
    * JS function every time, so we would never get referentially equal JS functions if we
    * used the Scala-to-JS conversion more than once. Therefore, we need to perform that
    * conversion only once and save the result. This method encapsulates such conversion.
    */
  val domValue: js.Function1[InteractionEvent, Unit] = value

  override def bind(element: ReactivePixi.Base): DynamicSubscription = {
    element.bindSubscription(element) { (element, owner) =>
      element.addEventListener(this)
      new Subscription(
        owner,
        cleanup = () => {
          element.removeEventListener(this)
        }
      )
    }
  }

  override def equals(that: Any): Boolean = {
    that match {
      case setter: EventPropBinder if (key == setter.key) && (domValue == setter.domValue) =>
        true
      case _ => false
    }
  }
}

object EventPropBinder {
  def apply[El <: ReactivePixi.Base](
      observer: Observer[InteractionEvent],
      key: String
  ): EventPropBinder = {

    val callback = (ev: InteractionEvent) => {
      observer.onNext(ev)
    }

    new EventPropBinder(key, callback)
  }
}

case class Stroke(width: Double = 1.0, color: Color = Color.white, alpha: Double = 1.0)
case class Fill(color: Color = Color.white, alpha: Double = 1.0)

sealed trait Drawing

object Drawing {
  case class Rectangle(
      origin: Coord = Coord(0.0, 0.0),
      width: Double,
      height: Double,
      fill: Fill = Fill(),
      stroke: Stroke = Stroke()
  ) extends Drawing

  case class Circle(origin: Coord, radius: Double, fill: Fill = Fill(), stroke: Stroke = Stroke()) extends Drawing

  case class Line(start: Coord, end: Coord, stroke: Stroke = Stroke(2))       extends Drawing
  case class CurvedLine(start: Coord, end: Coord, stroke: Stroke = Stroke(2)) extends Drawing

  def render(drawing: Drawing, graphics: Graphics): Graphics = {
    graphics.clear()
    _render(drawing, graphics)
  }

  def _render(drawing: Drawing, graphics: Graphics): Graphics = {
    drawing match {
      case Rectangle(origin, width, height, fill, stroke) =>
        graphics
          .useFill(fill)
          .useStroke(stroke)
          .drawRect(origin.x, origin.y, width, height)

      case Circle(origin, radius, fill, stroke) =>
        graphics
          .useFill(fill)
          .useStroke(stroke)
          .drawCircle(origin.x, origin.y, radius)

      case Line(start, end, stroke) =>
        graphics
          .useStroke(stroke)
          .moveTo(start.x, start.y)
          .lineTo(end.x, end.y)

      case CurvedLine(start, end, stroke) =>
        graphics
          .useStroke(stroke)
          .moveTo(start.x, start.y)
          .quadraticCurveTo(300, 300, end.x, end.y)

    }
  }

  implicit class GraphicsSyntax(val graphics: Graphics) extends AnyVal {
    def useFill(fill: Fill): Graphics =
      graphics.beginFill(fill.color.hex, fill.alpha)
    def useStroke(stroke: Stroke): Graphics =
      graphics.lineStyle(stroke.width, stroke.color.hex, stroke.alpha)
  }
}
