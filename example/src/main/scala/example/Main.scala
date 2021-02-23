package example

import com.raquo.laminar.api.L._
import example.slides.Slides
import org.scalajs.dom.document

object Main {
  def main(args: Array[String]): Unit =
    documentEvents.onDomContentLoaded.foreach { _ =>
      val container = document.body
      render(container, body)
    }(unsafeWindowOwner)

  val body: Div = Slides.view
}
