package example

import animus.SignalOps
import com.raquo.laminar.api.L.*

import scala.util.Random

object PerformanceTest:
  val positionVar: Var[Double] = Var(0.0)

  def every[A](a: => A, ms: => Int = 300): Signal[A] =
    EventStream.periodic(ms).map(_ => a).toSignal(a)

  private var mousePosition = (0.0, 0.0)

  val colors = Vector("#888", "#ccc", "#ddd", "#fff")

  def randomCube =

    def coord =
      ((Random.nextDouble() * 150) - 75 + mousePosition._1, (Random.nextDouble() * 150) - 75 + mousePosition._2)

    val $xyPosition: Signal[(Double, Double)] =
      every(coord, Random.nextInt(1000) + 300).spring

    svg.rect(
      svg.width("2px"),
      svg.height("2px"),
      svg.fill("white"),
      svg.x <-- $xyPosition.map(_._1).px,
      svg.y <-- $xyPosition.map(_._2).px
    )

  def body: Div =
    div(
      margin("40px"),
//      h1("ANIMUS"),
      windowEvents(_.onMouseMove) --> { e => mousePosition = e.clientX -> e.clientY },
      windowEvents(_.onKeyDown).map(_.key.toIntOption.getOrElse(0).toDouble * 40) --> positionVar,
      div(
        position.fixed,
        left("0"),
        right("0"),
        top("0"),
        bottom("0"),
        svg.svg(
          svg.width("100vw"),
          svg.height("100vh"),
          List.fill(1000) {
            randomCube
          }
        )
      )
//      child.text <-- positionVar.signal.sprinkle.px,
//      div(
//        width("40px"),
//        height("40px"),
//        background("red"),
//        position.relative,
//        left <-- positionVar.signal.sprinkle.px
//      )
    )
