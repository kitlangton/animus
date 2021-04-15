package example

import animus._
import com.raquo.laminar.api.L
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.{document, html}

import scala.language.implicitConversions

object Main {
  def main(args: Array[String]): Unit =
    documentEvents.onDomContentLoaded.foreach { _ =>
      val container = document.body
      render(container, body)
    }(unsafeWindowOwner)

  def body: Div = {
    div(
      margin("40px"),
      em(h1("ANIMUS")),
      AnimateTextExample()
    )
  }

  trait Component {
    def body: HtmlElement
  }

  object Component {
    implicit def toLaminarElement(component: Component): HtmlElement = component.body
  }

  case class AnimateTextExample() extends Component {
    val textVar                                  = Var("")
    val $characters: Signal[List[(String, Int)]] = textVar.signal.map(_.split("").zipWithIndex.toList)
    val $isEmpty                                 = textVar.signal.map(_.isEmpty)

    val keyEvents = windowEvents.onKeyDown.map(_.key) --> {
      case "Backspace" => textVar.update(_.dropRight(1))
      case " "         => textVar.update(_ + "•")
      case "Shift"     => ()
      case "Meta"      => ()
      case "Alt"       => ()
      case "Control"   => ()
      case "Enter"     => textVar.update(_ + "¶")
      case x           => textVar.update(_ + x.toUpperCase)
      case _           => ()
    }

    def body: Div = {
      div(
        fontWeight.bold,
        keyEvents,
        div(
          opacity <-- $isEmpty.map {
            if (_) 1.0 else 0.6
          }.spring,
          transformOrigin("bottom left"),
          transform <-- $isEmpty.map {
            if (_) 1.0 else 0.6
          }.spring.map { s => s"scale($s)" },
          "TYPE SOMETHING."
        ),
        children <-- $characters.splitTransition(identity) { (_, value, _, transition) =>
          val character = value._1
          div(
            character,
            Option.when(Set("•", "¶")(character))(opacity(0.4)),
            display.inlineFlex,
            color("orange"),
            transition.width,
            transition.height
          )
        }
      )
    }

  }
}
