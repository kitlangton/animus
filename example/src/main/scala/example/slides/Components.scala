package example.slides

import animus.{ObservableOps, SignalOps}
import com.raquo.laminar.api.L._
import example.Highlight

object Components {
  def codeBlock(
      codeString: String = "",
      language: String = "scala",
      codeSignal: Option[Signal[String]] = None,
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
        codeSignal.map { signal =>
          div(
            opacity := "0.0",
            code(
              inContext { (el: HtmlElement) =>
                signal --> { str =>
                  val result = Highlight.highlight(language, str).value
                  el.ref.innerHTML = result
                }
              }
            )
          )
        },
        div(
          codeSignal.map { _ =>
            Seq(
              position.absolute,
              top := "10px"
            )
          },
          code(
            codeSignal.map { signal =>
              inContext { (el: HtmlElement) =>
                signal --> { str =>
                  val result = Highlight.highlight(language, str).value
                  el.ref.innerHTML = result
                }
              }
            },
            onMountCallback { el =>
              val result = Highlight.highlight(language, codeString.trim).value
              el.thisNode.ref.innerHTML = result
            }
          )
        ),
        codeSignal.map { signal =>
          onMountBind { (el: MountContext[HtmlElement]) =>
            height <-- signal
              .mapTo {
                if (el.thisNode.ref.firstElementChild != null)
                  el.thisNode.ref.firstElementChild.getBoundingClientRect().height + 25
                else
                  0.0
              }
              .spring
              .px
          }
        }
      )
    )
  }

}
