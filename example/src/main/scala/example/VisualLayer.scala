package example

import animus._
import com.raquo.laminar.api.L
import com.raquo.laminar.api.L._
import example.Docs.flexCenter
import zio.clock.Clock
import zio._
import zio.duration._

import scala.util.Random

class VisualLayer[-R, A: Tag](name: String, val inputs: List[String], val outputs: List[String], providing: A) {

  trait Service {
    def use: ZIO[Any, Nothing, Unit] = ZIO.sleep(1.second).provideLayer(Clock.live)
  }

  private val loadedVar = Var(0.0)

  def inputName: Div =
    inputs match {
      case Nil  => div("Any")
      case list => div(list.mkString(" with "))
    }

  def boxStyles($highlighted: Signal[Boolean] = Val(false)): Mod[HtmlElement] = Seq(
    position.relative,
    fontFamily("'Source Code Pro', monospace"),
    padding("8px 8px"),
    background <-- $highlighted.map { if (_) 0.7 else 0.2 }.spring.map { d => s"rgba(80,80,80,$d)" },
    flexCenter,
    flexDirection.column,
    margin("4px"),
    fontWeight.bold,
    fontSize("16px"),
//    border("1px solid #333"),
    border <-- $highlighted.map { if (_) 1 else 0.5 }.spring.map { d => s"1px solid rgba(80,80,100,$d)" },
    borderRadius("4px"),
    overflow.hidden
  )

  def renderWithDependencies(
      highlightedName: Signal[Boolean],
      highlightInputs: Signal[Boolean],
      highlightOutputs: Signal[Boolean]
  ) = tr(
    fontFamily("'Source Code Pro', monospace"),
    td(
      div("val", opacity(0.5), margin("0px 8px"))
    ),
    td(
      div(boxStyles(highlightedName), div(name))
    ),
    td(
      div(":", opacity(0.5), margin("0px 8px"))
    ),
    td(
      div(boxStyles(highlightInputs), inputName)
    ),
    td(
      div("→", fontSize("24px"), opacity(0.5), margin("0px 8px"))
    ),
    td(
      div(boxStyles(highlightOutputs), div(outputs.mkString(" with ")))
    )
  )

  def render: Div = div(
    position.relative,
    boxStyles(),
    opacityBinding,
    div(name, zIndex(2)),
    div(
      position.absolute,
      top("0"),
      left("0"),
      height("100%"),
      background("blue"),
      opacity(0.7),
      width <-- loadedVar.signal.spring.percent
    )
  )

  val opacityBinding: Binder[HtmlElement] = opacity <-- loadedVar.signal.map {
    case i if i >= 100 => 1.0
    case 0             => 0.6
    case _             => 0.8
  }.spring

  val live: ZLayer[R, Nothing, Has[A]] = {
    def randomInt = Random.nextInt(7) + 4

    val acquire = (for {
      _ <- UIO(loadedVar.update(_ + 1)).delay(randomInt.millis).repeatN(99)
      _ <- ZIO.sleep(500.millis)
    } yield providing).provideLayer(Clock.live)

    val release = (for {
      _ <- UIO(loadedVar.update(_ - 1)).delay(randomInt.millis).repeatN(99)
      _ <- ZIO.sleep(500.millis)
    } yield ()).provideLayer(Clock.live)

    ZLayer.fromAcquireRelease(acquire)(_ => release)
  }
}
