package pixi

import org.scalajs.dom.Element

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport(module = "pixi.js", name = JSImport.Namespace)
object PIXI extends js.Object {

  @js.native
  object Loader extends js.Object {
    val shared: PIXI.Loader = js.native
  }

  @js.native
  object Ticker extends js.Object {
    val shared: PIXI.Ticker = js.native
  }

  def SCALE_MODES: ScaleModes = js.native

  def settings: Settings = js.native

  @js.native
  trait Settings extends js.Object {
    var SCALE_MODE: ScaleMode
    var RENDER_OPTIONS: RenderOptions
  }

  @js.native
  trait RenderOptions extends js.Object {
    var antialias: Boolean = js.native
  }

  type ScaleMode
  @js.native
  trait ScaleModes extends js.Object {
    val NEAREST: ScaleMode
    val LINEAR: ScaleMode
  }

  @js.native
  trait ApplicationOptions extends js.Object {
    var width: js.UndefOr[Double]        = js.native
    var height: js.UndefOr[Double]       = js.native
    var antialias: js.UndefOr[Boolean]   = js.native
    var transparent: js.UndefOr[Boolean] = js.native
    var forceCanvas: js.UndefOr[Boolean] = js.native
    var resolution: js.UndefOr[Int]      = js.native
  }

  @js.native
  class Application(
      options: ApplicationOptions
  ) extends js.Object {

    val renderer: Renderer = js.native

    val stage: Container = js.native

    val view: Element = js.native

    val ticker: Ticker = js.native
  }

  @js.native
  class Renderer() extends js.Object {

    def resize(width: Double, height: Double): Unit = js.native

    var antialias: Boolean  = js.native
    var autoResize: Boolean = js.native

    @js.native
    trait Plugins extends js.Object {
      val interaction: InteractionManager
    }

    val plugins: Plugins = js.native

    def generateTexture(
        displayObject: DisplayObject,
        scaleMode: ScaleMode = SCALE_MODES.NEAREST,
        resolution: Int = 1,
        region: Rectangle
    ): Texture = js.native
  }

  @js.native
  class Container extends js.Object with EventEmitter with DisplayObject {
    var sortableChildren: Boolean = js.native

    var mask: PIXI.Container = js.native

    def addChild(displayObject: DisplayObject): Unit    = js.native
    def removeChild(displayObject: DisplayObject): Unit = js.native

    var children: js.Array[DisplayObject] = js.native

    var hitArea: Rectangle = js.native

    var height: Double = js.native
    var width: Double  = js.native

    var onChildrenChange: js.Function0[Unit] = js.native

    var interactive: Boolean = js.native
  }

  @js.native
  class ObservablePoint extends js.Object {
    var x: Double = js.native
    var y: Double = js.native

    def set(x: Double, y: Double): Unit = js.native
    def set(both: Double): Unit         = js.native
  }

  @js.native
  class Text(var text: String, var style: TextStyle) extends Sprite {}

  import scala.scalajs.js
  import scala.scalajs.js.annotation._

  // @ScalaJSDefined
  trait TextStyle extends js.Object {
    val fontFamily: js.UndefOr[String] = js.undefined
    val fontSize: js.UndefOr[Int]      = js.undefined
    val fill: js.UndefOr[Double]       = js.undefined
    val align: js.UndefOr[String]      = js.undefined
  }

  //  object TextStyle {
  //    def apply(foo: Int, bar: String): TextStyle =
  //      js.Dynamic.literal(foo = foo, bar = bar).asInstanceOf[TextStyle]
  //  }

  //  @js.native
  //  trait TextStyle(fontFamily: String = "Helvetica",
  //                  fontSize: Int = 34,
  //                  fill: Double = 0xFFFFFF,
  //                  align: String = "right")
  //      extends js.Object

  @js.native
  class Sprite() extends Container {
    var texture: Texture        = js.native
    val anchor: ObservablePoint = js.native
  }

  @js.native
  class AnimatedSprite(spritesheet: js.Array[Texture] = js.Array()) extends Sprite {
    def play(): Unit = js.native

    var animationSpeed: Double = js.native
  }

  @js.native
  object Sprite extends js.Object {
    def from(texture: Texture): Sprite = js.native
  }

  @js.native
  class Texture extends js.Object {
    var rotate: Int = js.native

    def update(): Unit          = js.native
    var requiresUpdate: Boolean = js.native
  }

  @js.native
  class Ticker() extends js.Object {
    def remove(f: js.Function1[Double, Unit]): Unit = js.native

    def add(f: js.Function1[Double, Unit]): Unit = js.native
  }

  //  val sheet = PIXI.Loader.shared.resources["assets/spritesheet.json"].spritesheet;
  //  PIXI.Loader.shared.add("assets/spritesheet.json").load(setup);
  @js.native
  class Loader() extends js.Object {
    def add(file: String): Loader            = js.native
    def load(cb: js.Function0[Unit]): Loader = js.native
    def resources: ResourceObject            = js.native
  }

  @js.native
  class ResourceObject() extends js.Object {
    @JSBracketAccess
    def apply(string: String): Resource = js.native
  }

  @js.native
  class Resource() extends js.Object {
    def spritesheet: Spritesheet = js.native
  }

  @js.native
  class Spritesheet() extends js.Object {
    def animations: Animations = js.native
  }

  @js.native
  class Animations() extends js.Object {
    @JSBracketAccess
    def apply(string: String): js.Array[Texture] = js.native
  }
  @js.native
  class Graphics() extends Container {

    def beginFill(color: Int, alpha: Double = 1.0): Graphics = js.native

    def endFill(): Graphics = js.native

    def lineStyle(width: Double, color: Int, alpha: Double): Graphics = js.native

    // Shapes
    def drawCircle(x: Double, y: Double, radius: Double): Graphics = js.native

    def drawRect(x: Double, y: Double, width: Double, height: Double): Graphics = js.native

    def lineTo(x: Double, y: Double): Graphics = js.native

    def moveTo(x: Double, y: Double): Graphics = js.native

    def quadraticCurveTo(cpX: Double, cpY: Double, toX: Double, toY: Double): Graphics = js.native

    def clear(): Graphics = js.native
  }

  @js.native
  class Rectangle(var x: Double, var y: Double, var width: Double, var height: Double) extends js.Object {}

  @js.native
  trait InteractionManager extends js.Object with EventEmitter {}

  @js.native
  trait InteractionData extends js.Object {
    def global: Point
  }

  @js.native
  trait InteractionEvent extends js.Object {
    def stopPropagation(): Unit

    def data: InteractionData
  }

  @js.native
  trait Point extends js.Object {
    def x: Double
    def y: Double
  }

  @js.native
  trait EventEmitter extends js.Object {
    def x: Double
    def y: Double

    def _eventsCount: Int = js.native

    def on(event: String, callback: js.Function1[InteractionEvent, Unit]) =
      js.native
  }

  @js.native
  trait DisplayObject extends js.Object {
    def getBounds(): Rectangle = js.native

    var customId: String = js.native

    def toLocal(position: Point, from: DisplayObject): Point = js.native

    def parent: Container = js.native

    var angle: Double    = js.native
    var rotation: Double = js.native

    var scale: ObservablePoint = js.native
    var pivot: ObservablePoint = js.native

    var alpha: Double = js.native
    var tint: Double  = js.native

    var visible: Boolean = js.native

    var name: String = js.native

    var x: Double      = js.native
    var y: Double      = js.native
    var zIndex: Double = js.native

    var buttonMode: Boolean = js.native

    def isSprite: Boolean = js.native
  }

}
