package example

import com.raquo.laminar.api.L._
import org.scalajs.dom.document
import animus._

object Main {
  def main(args: Array[String]): Unit = {
    documentEvents.onDomContentLoaded.foreach { _ =>
      val container = document.body
      render(container, Docs.view)
    }(unsafeWindowOwner)
  }
}
