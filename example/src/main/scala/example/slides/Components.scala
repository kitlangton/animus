package example.slides

import animus.{ObservableOps, SignalOps}
import com.raquo.laminar.api.L._
import example.Highlight

object Components {
  def codeBlock(
      codeString: String = "",
      language: String = "scala",
      marginBottom: Boolean = true
  ): HtmlElement = {
    pre(
      cls := "code",
      borderRadius := "4px",
      margin.maybe(Option.when(!marginBottom)("0px")),
      fontSize := "14px",
      div(
        padding := "10px",
        overflowY.hidden,
        borderRadius := "4px",
        position.relative,
        div(
          code(
            onMountCallback { el =>
              val result = Highlight.highlight(language, codeString.trim).value
              el.thisNode.ref.innerHTML = result
            }
          )
        )
      )
    )
  }

}
