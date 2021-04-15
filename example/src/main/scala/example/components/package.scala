package example

import com.raquo.laminar.api.L._
import animus._

package object components {
  def codeBlock(
      codeString: String = "",
      highlights: Signal[Set[Int]] = Val(Set.empty),
      language: String = "scala"
  ): HtmlElement =
    codeBlockSignal(Val(codeString), highlights, language)

  def codeBlockSignal(
      codeSignal: Signal[String],
      highlights: Signal[Set[Int]] = Val(Set.empty),
      language: String = "scala"
  ): HtmlElement =
    pre(
      div(
        fontSize := "16px",
        background("rgb(20,20,30)"),
        padding := "12px",
        overflowX.scroll,
        border("1px solid #333"),
        borderRadius := "4px",
        position.relative,
        div(
          code(
            children <-- Transitions.transitionList(
              codeSignal
                .map { code =>
                  val result = Highlight.highlight(language, code.trim).value
                  result.split("\n").toList.zipWithIndex
                }
            )(identity) { (_, value, $value, transition) =>
              val (code, idx) = value
              div(
                onMountCallback { el =>
                  el.thisNode.ref.innerHTML = code
                }
              )
            }
          )
        )
      )
    )

}
