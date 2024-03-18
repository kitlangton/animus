package example

import com.raquo.laminar.api.L.*

trait View:
  def body: HtmlElement

object View:
  given Conversion[View, HtmlElement] = _.body
