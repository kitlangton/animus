package example.slides

import com.raquo.laminar.api.L._

trait Slide {
  def render($section: Signal[Int]): HtmlElement
}
