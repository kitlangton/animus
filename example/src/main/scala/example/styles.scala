package example

import animus.SignalOps
import com.raquo.laminar.api.L._

import scala.util.Try

object styles {
  def fixedFullScreen: Modifier[HtmlElement] = Seq(
    position.fixed,
    top := "0",
    bottom := "0",
    left := "0",
    right := "0",
    width := "100vw",
    height := "100vh"
  )

  def transitionOpacity[A]($isVisible: Signal[Boolean]): Mod[HtmlElement] =
    opacity <-- $isVisible.map { if (_) 1.0 else 0.0 }.spring

  def transitionOpacitySpring[A]($isVisible: Signal[Boolean]): Mod[HtmlElement] =
    opacity <-- $isVisible.map { if (_) 1.0 else 0.0 }.spring

}
