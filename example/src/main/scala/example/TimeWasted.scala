package example

import animus._
import com.raquo.laminar.api.L._

import scala.util.Random

object TimeWasted extends Component {
  val $count: Signal[Int] =
    EventStream.periodic(1000).toSignal(0)

  val $degrees: Signal[Double] =
    $count.scanLeft(_ => 0.0)((acc, _) => acc + (Random.nextDouble() * 600))

  def body: Div = div(
    marginTop("24px"),
    marginBottom("24px"),
    display.flex,
    alignItems.center,
    flexDirection.column,
    div(
      marginBottom("12px"),
      border("1px solid #333"),
      borderRadius("60px"),
      display.flex,
      padding("4px"),
      justifyContent.center,
      width("60px"),
      height("60px"),
      position.relative,
      div(
        width("3px"),
        height("30px"),
        background("#333"),
        borderRadius("4px"),
        transform <-- $degrees.map { deg =>
          s"rotate(${deg}deg)"
        },
        transformOrigin("bottom")
      ),
      div(
        position.absolute,
        width("3px"),
        height("30px"),
        borderRadius("4px"),
        background("orange"),
        transform <-- $degrees.spring.map { deg =>
          s"rotate(${deg}deg)"
        },
        transformOrigin("bottom")
      )
    ),
    div(
      fontSize("16px"),
      marginBottom("24px"),
      display.flex,
      height("20px"),
      alignItems.center,
//      fontStyle.italic,
      div(
        opacity(0.6),
        s"YOU'VE WASTED${nbsp}"
      ),
      AnimatedCount($count).amend(
        position.relative,
        top("-1px"),
        left("-1px")
      ),
      div(
        opacity(0.6),
        s"${nbsp}SECOND",
        div(
          "S",
          display.inlineFlex
//          Transitions.width($count.map(_ != 1))
        ),
        "."
      )
    )
  )

}
