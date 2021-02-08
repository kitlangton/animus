package example

import animus.Animation.from

import scala.scalajs.js.timers.setTimeout
import com.raquo.laminar.api.L._
import animus._
import org.scalajs.dom.window

import scala.util.Random

object Docs {

  def slideOpen($isOpen: Signal[Boolean]): Mod[HtmlElement] = {
    Seq(
      overflowY.hidden,
      opacity <-- $isOpen.map { if (_) 1.0 else 0.0 }.spring,
      onMountBind { el: MountContext[HtmlElement] =>
        height <-- $isOpen.map {
          if (_)
            el.thisNode.ref.scrollHeight.toDouble
          else 0.0
        }.spring.px
      },
      onMountBind { _: MountContext[HtmlElement] =>
        marginBottom <-- $isOpen.map {
          if (_)
            4.0
          else 0.0
        }.spring.px
      }
    )
  }

  def slideOpenAfter(ms: Int): Mod[HtmlElement] = {
    val openVar = Var(false)
    Seq(
      onMountCallback { _: MountContext[HtmlElement] =>
        setTimeout(ms)(openVar.set(true))
      },
      slideOpen(openVar.signal)
    )
  }

  def typeText(string: String): Div = {
    val openVar = Var(false)
    div(
      onMountCallback { _: MountContext[HtmlElement] =>
        setTimeout(0)(openVar.set(true))
      },
      string
        .split("")
        .zipWithIndex
        .map { case (char, idx) =>
          val delayedStart = openVar.signal.changes
            .delay(idx * 80)
            .startWith(false)
          div(
            display.inlineFlex,
            overflowX.hidden,
            char,
            opacity <-- delayedStart.map { if (_) 1.0 else 0.0 }.spring,
            onMountBind { el =>
              width <-- delayedStart.map { if (_) el.thisNode.ref.scrollWidth.toDouble else 0.0 }.spring.px
            }
          )

        }
        .toList,
      div(
        display.inlineFlex,
        height("35px"),
        marginLeft("4px"),
//        borderRadius("2px")
        position.relative,
        top("4px"),
        width("5px"),
        background("red")
      )
    )
  }

  def section(content: Mod[HtmlElement]*): Div = div(
    position.relative,
    background("rgb(10,10,10)"),
    border("1px solid #333"),
    borderRadius("4px"),
    height("400px"),
    width("300px"),
    padding("12px 18px"),
    margin("8px"),
    boxSizing.borderBox,
    content
  )

  object Mod {
    def apply(mods: Mod[HtmlElement]*): Mod[HtmlElement] = mods
  }

  val flexCenter: Mod[HtmlElement] = Mod(
    display.flex,
    alignItems.center,
    justifyContent.center
  )

  val sectionVar = Var(0)

  val windowWidth: Double = window.innerWidth / 2

  private def circleView(color: String, styler: Style[String]): Div = {
    div(
      width <-- $size,
      height <-- $size,
      border("1px solid transparent"),
      styler(color),
      borderRadius("150px")
    )
  }

  def $size = EventStream
    .periodic((Random.nextDouble() * 800 + 600).toInt)
    .mapTo(Random.nextDouble() * 300 + 10)
    .startWith(50.0)
    .spring
    .px

  def view: Div = {
    div(
      height("100%"),
      windowEvents.onKeyDown.map(_.key) --> {
        case "ArrowRight" => sectionVar.update(_ + 1)
        case "ArrowLeft"  => sectionVar.update(_ - 1)
      },
      display.flex,
      flexDirection.column,
      flexCenter,
      svg.svg(
        svg.width("150"),
        svg.height("150"),
        svg.g(
          svg.transform <-- EventStream
            .periodic(100)
            .map(i => i * 10.0)
            .spring(0)
            .map { d =>
              s"rotate(${d} 75 75)"
            },
          List.tabulate(10)(wheel)
        )
      )
//      h1("Animus", color("red"))
    )
  }

  private def wheel(idx: Int) =
    svg.g(
      svg.transform("translate(25 25)"),
      svg.opacity <-- from(0).wait(idx * 50).to(1).run.map(_.toString),
      svg.path(
        svg.transform <-- EventStream
          .periodic(500)
          .map(i => i * 45.0 + idx * 45.0)
          .delay(idx * 100)
          .spring(idx * 45.0)
          .map { d =>
            s"rotate(${d} 51 51)"
          },
        svg.fill("transparent"),
        svg.d(
          "M0 100C0.69577 100 10.385 50 50 50C91.5516 50 100 0 100 0"
        ),
        svg.strokeWidth("2"),
        svg.stroke <-- EventStream.periodic(300).mapTo(Random.nextDouble() * 255).spring(255.0).map { d =>
          s"rgb($d, 0, 0)"
        }
      )
    )

  def cool = div(
    div(
      display.flex,
      section(
        h1(typeText("Animus")),
        div(
          h1("docs", slideOpenAfter(1500)),
          h1("coming", slideOpenAfter(1000)),
          h1("soon.", slideOpenAfter(500)),
          opacity(0.6)
        )
      ),
      section(
        div(
          flexCenter,
          height("100%"),
          circleView("blue", background),
          circleView("green", background),
          circleView("blue", background),
          circleView("white", background),
          circleView("red", background),
          circleView("blue", background),
          circleView("gray", background),
          circleView("blue", background),
          circleView("gray", background),
          circleView("red", background),
          circleView("white", background),
          circleView("gray", background),
          circleView("green", background),
          circleView("red", background)
        )
      )
    ),
    div(
      display.flex,
      section(
        overflow.hidden,
        flexCenter,
        div(
          flexCenter,
          circleView("blue", borderColor).amend(position.absolute),
          circleView("green", borderColor).amend(position.absolute),
          circleView("blue", borderColor).amend(position.absolute),
          circleView("gray", borderColor).amend(position.absolute),
          circleView("white", borderColor).amend(position.absolute),
          circleView("blue", borderColor).amend(position.absolute),
          circleView("gray", borderColor).amend(position.absolute),
          circleView("blue", borderColor).amend(position.absolute),
          circleView("white", borderColor).amend(position.absolute),
          circleView("red", borderColor).amend(position.absolute),
          circleView("blue", borderColor).amend(position.absolute),
          circleView("gray", borderColor).amend(position.absolute),
          circleView("white", borderColor).amend(position.absolute),
          circleView("red", borderColor).amend(position.absolute),
          circleView("blue", borderColor).amend(position.absolute),
          circleView("white", borderColor).amend(position.absolute),
          circleView("blue", borderColor).amend(position.absolute),
          circleView("gray", borderColor).amend(position.absolute),
          circleView("blue", borderColor).amend(position.absolute),
          circleView("white", borderColor).amend(position.absolute),
          circleView("green", borderColor).amend(position.absolute),
          circleView("blue", borderColor).amend(position.absolute),
          circleView("gray", borderColor).amend(position.absolute),
          circleView("red", borderColor).amend(position.absolute),
          circleView("white", borderColor).amend(position.absolute),
          circleView("gray", borderColor).amend(position.absolute),
          circleView("blue", borderColor).amend(position.absolute),
          circleView("gray", borderColor).amend(position.absolute),
          transformOrigin("center"),
          transform("scale(1.8)")
        )
      ),
      section(
        flexCenter,
        flexDirection.column,
        h1("Animations", slideOpenAfter(2000)),
        h1("for all", slideOpenAfter(2500)),
        h1("occasions.", slideOpenAfter(3000))
      )
    )
  )

}
