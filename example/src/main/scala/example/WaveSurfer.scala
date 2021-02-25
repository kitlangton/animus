package example

import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobal, JSImport}

@js.native
@JSImport("wavesurfer.js", JSImport.Default)
class WaveSurfer(options: js.Object) extends js.Object {
  def play(): Unit            = js.native
  def load(url: String): Unit = js.native
}

object WaveSurfer {
  def create(id: String) =
    new WaveSurfer(js.Dynamic.literal(container = id))

  def create(element: dom.html.Element) =
    new WaveSurfer(js.Dynamic.literal(container = element))
}
