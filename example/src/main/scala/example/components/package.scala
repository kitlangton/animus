package example

import animus.*
import com.raquo.laminar.api.L.*

package object components:
  def FadeInWords(string: String, delay: Int = 0): Modifier[HtmlElement] =
    string.split(" ").zipWithIndex.toList.map { case (word, idx) =>
      val $opacity = Animation.from(0).wait(delay + 150 * idx).to(1).run
      div(
        word + nbsp,
        display.inlineFlex,
        opacity <-- $opacity,
        position.relative,
        Transitions.height($opacity.map(_ > 0)),
        onMountBind { el =>
          top <-- Animation.from(el.thisNode.ref.scrollHeight).wait(delay + 150 * idx).to(0).run.px
        }
      )
    }

//  def codeBlock(
//    codeString: String = "",
//    highlights: Signal[Set[Int]] = Val(Set.empty),
//    language: String = "scala"
//  ): HtmlElement =
//    codeBlockSignal(Val(codeString), highlights, language)
//
//  def codeBlockSignal(
//    codeSignal: Signal[String],
//    highlights: Signal[Set[Int]] = Val(Set.empty),
//    language: String = "scala"
//  ): HtmlElement =
//    pre(
//      div(
//        fontSize := "16px",
//        background("rgb(20,20,30)"),
//        padding := "12px",
//        overflowX.scroll,
//        border("1px solid #333"),
//        borderRadius := "4px",
//        position.relative,
//        div(
//          code(
//            children <-- Transitions.transitionList(
//              codeSignal.map { code =>
//                val result = Highlight.highlight(language, code.trim).value
//                result.split("\n").toList.zipWithIndex
//              }
//            )(identity) { (_, value, $value, transition) =>
//              val (code, idx) = value
//              div(
//                onMountCallback { el =>
//                  el.thisNode.ref.innerHTML = code
//                }
//              )
//            }
//          )
//        )
//      )
//    )
//
