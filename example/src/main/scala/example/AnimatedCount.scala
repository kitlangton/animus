package example

import animus.*
import com.raquo.laminar.api.L.*

case class AnimatedCount($count: Signal[Int]) extends Component:
  val $digits: Signal[List[(String, Int)]] =
    $count.map(_.toString.split("").reverse.zipWithIndex.reverse.toList)

  override def body: HtmlElement =
    div(
      fontWeight("1000"),
      fontSize("24px"),
      padding("0 4px"),
      color("orange"),
      fontFamily("Source Code Pro"),
      display.flex,
      children <-- $digits.splitTransition(_._2) { case (_, _, signal, t0) =>
        div(
          children <-- signal
            .map(_._1)
            .splitOneTransition(identity) { (_, int, _, t1) =>
              div(
                int,
                t0.opacity,
                t0.width,
                t1.height
              )
            }
        )
      }
    )
