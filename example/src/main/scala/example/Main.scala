package example

import animus.{Animatable, DeriveAnimatable, ObservableOps, ResizeObserver, SignalOps}
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import example.Docs.flexCenter
import example.Main.{Coord, positions}
import org.scalajs.dom.{document, html}
import zio._
import zio.duration.durationInt

import scala.scalajs.js.timers.setTimeout

object Main {
  def main(args: Array[String]): Unit =
    documentEvents.onDomContentLoaded.foreach { _ =>
      val container = document.body
      render(container, body)
    }(unsafeWindowOwner)

  trait App
  val appLayer =
    new VisualLayer[Has[UserService] with Has[Analytics] with Has[ProductService], App](
      "App.live",
      List("UserService", "ProductService", "Analytics"),
      List("App"),
      new App {}
    )

  trait UserService
  val userServiceLayer: VisualLayer[Has[UserRepo], UserService] =
    new VisualLayer("UserService.live", List("UserRepo"), List("UserService"), new UserService {})

  trait UserRepo
  val userRepoLayer: VisualLayer[Has[Database], UserRepo] =
    new VisualLayer("UserRepo.live", List("Database"), List("UserRepo"), new UserRepo {})

  trait ProductService
  val productServiceLayer: VisualLayer[Has[ProductRepo] with Has[UserService], ProductService] =
    new VisualLayer(
      "ProductService.live",
      List("ProductRepo", "UserService"),
      List("ProductService"),
      new ProductService {}
    )

  trait ProductRepo
  val productRepoLayer: VisualLayer[Has[Database], ProductRepo] =
    new VisualLayer("ProductRepo.live", List("Database"), List("ProductRepo"), new ProductRepo {})

  trait Database
  val databaseLayer: VisualLayer[Any, Database] =
    new VisualLayer("Database.live", List.empty, List("Database"), new Database {})

  trait Analytics
  val analyticsLayer: VisualLayer[Any, Analytics] =
    new VisualLayer("Analytics.live", List.empty, List("Analytics"), new Analytics {})

  private val builtUserService: ZLayer[Any, Nothing, Has[UserService]] =
    databaseLayer.live >>> userRepoLayer.live >>> userServiceLayer.live

  private val builtProductService: ZLayer[Any, Nothing, Has[ProductService]] =
    builtUserService ++ (databaseLayer.live >>> productRepoLayer.live) >>> productServiceLayer.live

  val liveLayer: ZLayer[Any, Nothing, Has[App]] =
    (analyticsLayer.live ++ builtUserService ++ builtProductService) >>> appLayer.live

  case class Coord(x: Double, y: Double) {
    def negate: Coord         = Coord(-x, -y)
    def +(that: Coord): Coord = Coord(x + that.x, y + that.y)
  }

  implicit val coordAnimatable: Animatable[Coord] = DeriveAnimatable.gen[Coord]

  var positions = Var(Map.empty[String, Coord])

  var displayTop = Var(false)

  private val layerSelection = LayerSelection(
    List(
      appLayer,
      userServiceLayer,
      userRepoLayer,
      productServiceLayer,
      productRepoLayer,
      analyticsLayer,
      databaseLayer
    )
  )

  val body: Div = div(
    position.relative,
    height("100vh"),
    flexCenter,
    flexDirection.column,
    layerSelection.allLayers,
    onDblClick --> { _ =>
      zio.Runtime.default.unsafeRunAsync_(ZIO.service[App].provideCustomLayer(liveLayer))
    }
  )
}

object LamUtils {
  def storePosition(name: String, element: HtmlElement): ReactiveHtmlElement[html.Div] = {
    val offset  = Var(Coord(0, 0))
    val offset2 = Var(Coord(0, 0))
    val offsets = offset.signal
      .combineWith(offset2.signal.spring)
      .map { case (coord, coord1) =>
        coord + coord1
      }
    div(
      element
        .amend(
          position.relative,
          left <-- offsets.signal.map(_.x).px,
          top <-- offsets.signal.map(_.y).px,
          inContext { el =>
            EventStream.periodic(500).delay(500) --> { _ =>
              val rect = el.ref.getBoundingClientRect()
              val top  = rect.top
              val left = rect.left
              positions.update(_.updated(name, Coord(left, top)))
            }
          },
          onUnmountCallback { el =>
            val rect = el.ref.getBoundingClientRect()
            val top  = rect.top
            val left = rect.left
            positions.update(_.updated(name, Coord(left, top)))
          },
          onMountCallback { el =>
            setTimeout(0) {
              val rect = el.thisNode.ref.getBoundingClientRect()
              val top  = rect.top
              val left = rect.left
              positions.now().get(name) match {
                case Some(value) =>
                  println(Coord(left, top), value)
                  val difference = Coord(value.x - left, value.y - top)
                  offset.set(difference)
                  setTimeout(0) {
                    offset2.set(difference.negate)
                  }
                case None =>
                  positions.update(_.updated(name, Coord(left, top)))
              }
            }
          }
        )
    )
  }
}
