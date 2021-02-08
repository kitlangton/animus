package example

import animus.DeriveAnimatable.Typeclass
import animus.{DeriveAnimatable, SignalOps}
import com.raquo.laminar.api.L._
import org.scalajs.dom
import org.scalajs.dom.window
import pixi.Builders.{container, graphics, textSprite}
import pixi.Drawing.{Circle, CurvedLine, Line, Rectangle}
import pixi.EventProp.{mouseDown, pointerDown, pointerMove}
import pixi.PIXI.ApplicationOptions
import pixi.Props.{draw, x, y}
import pixi.{Color, Coord, Fill, PIXI, Stroke}

import java.util.UUID
import scala.scalajs.js.Dynamic

//object Stocks {
//  val list = List(100.0, 200.0, 80.0, 160.0, 100.0)
//
//  def calculate(list: List[Double]): State =
//    list.zipWithIndex.map { case (d, i) => State(i, d) }.reverse.reduce(_ combine _)
//
//  def main(args: Array[String]): Unit = {
//    val result = calculate(list)
//    println(result)
//    println(State(1, 200) combine State(0, 100))
//  }
//
//  case class State(
//      buyIndex: Int = 0,
//      sellIndex: Int = 0,
//      buyPrice: Double = 0,
//      sellPrice: Double = 0,
//      smallestPrice: Double = 0,
//      largestPrice: Double = 0
//  ) {
//    def maxProfit: Double = sellPrice - buyPrice
//
//    def combine(that: State): State = {
//      val Seq(s1, s2) = Seq(this, that).sortBy(_.buyIndex)
//      val combined =
//        State(
//          buyIndex = s1.buyIndex,
//          sellIndex = s2.sellIndex,
//          buyPrice = s1.buyPrice,
//          sellPrice = s2.sellPrice,
//          smallestPrice = s1.smallestPrice.min(s2.smallestPrice),
//          largestPrice = s1.largestPrice.max(s2.largestPrice)
//        )
//      Seq(this, that, combined).maxBy(_.maxProfit)
//    }
//  }
//
//  object State {
//    def apply(index: Int, price: Double) = new State(index, index, price, price, price, price)
//  }
//
//}
//
//object Main {
//  def startApp = {
//    val options = Dynamic.literal().asInstanceOf[ApplicationOptions]
//    options.antialias = true
//    options.width = window.innerWidth
//    options.transparent = false
//    options.height = window.innerHeight
//    val app = new PIXI.Application(options)
//    app.renderer.autoResize = true
//    dom.document.body.appendChild(app.view)
//    shapes.activate()
//    app.stage.addChild(shapes.ref)
//    shapes.ref.interactive = true
//    shapes.ref.hitArea = new PIXI.Rectangle(0, 0, window.innerWidth, window.innerHeight);
//  }
//
//  val coordVar = Var(Coord(0, 0))
//
//  implicit val animatableCoord: Typeclass[Coord] = DeriveAnimatable.gen[Coord]
//
//  case class Node(coord: Coord) {
//    val uuid: UUID = UUID.randomUUID()
//  }
//
//  val objects      = Var(List.empty[Node])
//  val arrows       = Var(List.empty[CurvedLine])
//  val selectedNode = Var(Option.empty[UUID])
//
//  lazy val shapes =
//    container(
//      textSprite("Hello")(
//        y(300),
//        x(300)
//      ),
//      graphics(
//        pointerDown --> { e =>
//          objects.update(_.appended(Node(Coord(e.data.global.x, e.data.global.y))))
//        },
//        draw(
//          Rectangle(
//            width = window.innerWidth,
//            height = window.innerHeight,
//            fill = Fill(Color.black),
//            stroke = Stroke(alpha = 0)
//          )
//        )
//      ),
//      pointerMove --> { e =>
//        coordVar.set(Coord(e.data.global.x, e.data.global.y))
//      },
//      pixi.Props.children <-- arrows.signal.split(identity) { (key, arrow, _) =>
//        graphics(
//          draw(arrow)
//        )
//      },
//      pixi.Props.children <-- objects.signal.split(_.uuid) { (key, node, _) =>
//        graphics(
//          draw <-- selectedNode.signal.map { id =>
//            val size = if (id.contains(node.uuid)) 15 else 10
//            Circle(node.coord, size)
//          },
//          pointerDown --> { _ =>
//            selectedNode.now().flatMap(id => objects.now().find(id == _.uuid)) match {
//              case Some(startNode) =>
//                val newLine = CurvedLine(startNode.coord, node.coord)
//                arrows.update(_.appended(newLine))
//                selectedNode.set(None)
//              case None =>
//                selectedNode.set(Some(node.uuid))
//            }
//          }
//        )
//      },
//      graphics(
//        draw <-- coordVar.signal.spring.map { coord =>
//          Rectangle(Coord(100, 100), coord.x, coord.y, Fill(alpha = 0))
//        }
//      ),
//      graphics(
//        draw <-- coordVar.signal.spring.map { coord =>
//          CurvedLine(Coord(0, 0), coord)
//        }
//      )
//    )
//
//  def main(args: Array[String]): Unit =
//    documentEvents.onDomContentLoaded.foreach { _ =>
//      startApp
//    }(unsafeWindowOwner)
//
//}
