package example

import animus._
import com.raquo.laminar.api.L._
import org.scalajs.dom.document

import scala.scalajs.js.timers.setTimeout
import scala.util.Random

object Main {
  def main(args: Array[String]): Unit =
    documentEvents.onDomContentLoaded.foreach { _ =>
      val container = document.body
      render(container, body)
    }(unsafeWindowOwner)

  def body: Div = {
    val revealed = Var(false)

    setTimeout(1000) {
      revealed.set(true)
    }

    div(
      margin("40px"),
      div(
        maxWidth("600px"),
        margin("0 auto"),
        AnimatedTitle,
        TimeWasted,
        AnimateTextExample()
      )
    )
  }

}
