package example

import animus._
import com.raquo.laminar.api.L._
import example.components.FadeInWords
import org.scalajs.dom.document

import scala.scalajs.js.timers.setTimeout
import scala.util.Random

object Main {
  def main(args: Array[String]): Unit =
    documentEvents(_.onDomContentLoaded).foreach { _ =>
      val container = document.getElementById("app")
      val _         = render(container, body)
    }(unsafeWindowOwner)

  def body: Div = {
    val revealed = Var(false)

    setTimeout(1000) {
      revealed.set(true)
    }

    div(
      margin("40px"),
      div(
        div(
          opacity <-- Animation.from(0).wait(1000).to(1).run,
          position.fixed,
          top("0"),
          right("0"),
          fontSize("14px"),
          opacity(0.6),
          margin("48px"),
          a(
            target("_blank"),
            fontWeight.bold,
            textDecoration.none,
            color("white"),
            href("https://github.com/kitlangton/animus"),
            "GITHUB"
          )
        ),
        maxWidth("600px"),
        margin("0 auto"),
//        BackAndForthForeverTest,
        PerformanceTest.body
//        AnimatedTitle,
//        TimeWasted,
//        TheBasics
      )
    )
  }
}

object BackAndForthForeverTest extends Component {

  def square(configure: Animator[Double] => Animator[Double]): Div = {
    val $x: Signal[Double] =
      EventStream
        .periodic(1000)
        .toSignal(0)
        .map {
          case x if x % 2 == 0 => 400.0
          case _ => 0.0
        }
        .spring(configure)

    def randomColor =
      List("red", "green", "blue", "yellow", "purple", "orange", "pink", "chartreuse", "green")(Random.nextInt(9))

    div(
      left <-- $x.map(_.toString + "px"),
      width("50px"),
      height("50px"),
      position.relative,
      backgroundColor(randomColor),
      borderRadius("4px")
    )
  }

  override def body: HtmlElement =
    div(
      display.flex,
      flexDirection.column,
      styleProp("gap")("8px"),
      square(identity),
      square(_.wobbly),
      square(_.gentle),
      square(_.stiff),
      square(_.slow),
      square(_.molasses)
    )
}

object AnimateTest extends Component {
  val numberVar = Var(Set(0, 8, 3))
  override def body: HtmlElement =
    div(
      windowEvents(_.onKeyDown).map(_.key.toIntOption.getOrElse(0)) --> { int =>
        numberVar.update(set => if (set(int)) set - int else set + int)
      },
      children <-- numberVar.signal.map(_.toList).splitTransition(identity) { (_, int, _, transition) =>
        div(
          fontWeight.bold,
          int.toString,
          transition.height
        )
      }
    )

}

object TheBasics extends Component {
  override def body: HtmlElement = {
    val string = "Animus is a spring animation library for Laminar.".toUpperCase

    val $double: Signal[Double] = EventStream
      .periodic(1000)
      .toSignal(0)
      .mapTo(Random.nextDouble() * 500)

    div(
      marginBottom("48px"),
      fontWeight.bold,
      div(
        textAlign.center,
        FadeInWords(string, 100),
        marginBottom("48px")
      ),
      div(
        textAlign.center,
        FadeInWords("HERE IS A SIMPLE EXAMPLE", 1500)
      ),
      div(
        opacity <-- Animation.from(0).wait(2000).to(1).run,
        inContext { el =>
          height <-- Animation.from(0).wait(2000).to(el.ref.scrollHeight).run.px
        },
        overflowY.hidden,
        marginTop("24px"),
        div(
          position.relative,
          div(
            position.absolute,
            width("100%"),
            height("1px"),
            top("16px"),
            background("gray")
          ),
          marginBottom("4px"),
          div(
            display.flex,
            alignItems.center,
            justifyContent.center,
            margin("0 auto"),
            textAlign.center,
            width("100%")
          ),
          div(
            position.relative,
            div(
              background("#555"),
              width("32px"),
              height("32px"),
              position.relative,
              left <-- $double.px
            ),
            div(
              top("8px"),
              position.absolute,
              width("16px"),
              height("16px"),
              borderRadius("16px"),
              background("white"),
              left <-- $double.map(_ + 8).spring.px
            )
          )
        ),
        pre(
          fontSize("16px"),
          code(
            color("#666"),
            child.text <-- $double.map(_.toInt).px,
            position.relative,
            left <-- $double.map(_.toInt).px
          )
        ),
        pre(
          fontSize("16px"),
          code(
            color("white"),
            child.text <-- $double.spring.map(_.toInt).px,
            position.relative,
            left <-- $double.spring.map(_.toInt).px
          ),
          marginBottom("12px")
        )
//        codeBlock(
//          """
//val $position : Signal[Double] = ???
//
//val box =
//  Box(
//    left <-- $position.px
//  )
//
//val circle =
//  Circle(
//    left <-- $position.spring.px
//  )
//          """.trim
//        )
      )
    )
  }

}
