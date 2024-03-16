package example

import animus.*
import com.raquo.airstream.timing.PeriodicStream
import com.raquo.laminar.api.L.*
import org.scalajs.dom.document

def periodic[A](min: Int, max: Int, value: => A): Signal[A] =
  PeriodicStream[A](
    value,
    _ => Some(value -> scala.util.Random.between(min, max)),
    false
  ).startWith(value)

final case class Origin(
    x: Double,
    y: Double
) derives VectorArithmetic

final case class Rect(
    origin: Origin,
    width: Double,
    height: Double,
    red: Double = scala.util.Random.nextDouble(),
    green: Double = scala.util.Random.nextDouble(),
    blue: Double = scala.util.Random.nextDouble()

//    rotation: Double = 0
) derives VectorArithmetic:
  def x: Double = origin.x
  def y: Double = origin.y

final case class RectView(
    styles: String = "bg-red-600"
) extends View:
  def windowWidth = org.scalajs.dom.window.innerHeight

  def randomRect = Rect(
    Origin(0, 0),
    10,
//    scala.util.Random.nextDouble() * 80,
    scala.util.Random.nextDouble() * windowWidth
    // 0 // scala.util.Random.nextDouble() * 30 - 15
  )

  val rectSignal = periodic(500, 1000, randomRect).spring

  def body =
    div(
      top <-- rectSignal.map(_.y).px,
      left <-- rectSignal.map(_.x).px,
      width <-- rectSignal.map(_.width).px,
      height <-- rectSignal.map(_.height).px,
      background <-- rectSignal.map { r =>
        s"rgb(${r.red * 255},${r.red * 255},${r.red * 255})"
      },
      opacity(0.15)
//      transform <-- rectSignal.map { r =>
//        s"rotate(${r.rotation}deg)"
//      }
    )

trait View:
  def body: HtmlElement

object View:
  given Conversion[View, HtmlElement] = _.body

object Main:
  def main(args: Array[String]): Unit =
    documentEvents(_.onDomContentLoaded).foreach { _ =>
      val container = document.getElementById("app")
      val _         = render(container, body)
    }(unsafeWindowOwner)

  val adjectiveOne = AdjectiveView()

  extension (c: Char)
    def isVowel: Boolean =
      c match
        case 'a' | 'e' | 'i' | 'o' | 'u' => true
        case _                           => false

  val article = adjectiveOne.adjective.map { adj =>
    if adj.headOption.exists(_.isVowel) then "n" else ""
  }

  def wobble = EventStream
    .periodic(50)
    .map(_ => scala.util.Random.nextDouble() * 2)
    .toSignal(0.0)

  val animusText = div(
    cls("flex"),
    List("A", "N", "I", "M", "U", "S").map { letter =>
      div(
        cls("text-6xl relative"),
        letter,
        top <-- wobble.px,
        left <-- wobble.px,
        styleProp("filter") <-- wobble.spring.map { w =>
          s"blur(${w}px)"
        }
      )
    }
  )

  def body =
    div(
      cls("bg-neutral-900"),
      div(
        cls("z-10 relative font-bold overflow-hidden text-6xl text-neutral-100 h-screen flex flex-col"),
        cls("tracking-wider flex-col p-4 sm:p-24"),
        animusText,
        div(
          cls("text-lg mt-4 items-center max-w-lg"),
          div(
            cls("items-center flex flex-wrap"),
            "is a",
            children <-- article.splitOneTransition(identity) { (_, string, _, t) =>
              div(
                string,
                t.width
              )
            },
            adjectiveOne.body,
            div(s"and"),
            AdjectiveView().body
          ),
          div(s"spring animation library for Scala.js.")
        )
      ),
      div(
        cls("w-full flex justify-center items-center"),
        cls("fixed inset-0"),
        transform := "translateZ(0)",
        List.fill(200)(
          RectView("bg-neutral-800")
        )
      )
    )

  lazy val adjectives = Array(
    "aesthetic",
    "pulchritudinous",
    "resplendent",
    "seraphic",
    "sublime",
    "exquisite",
    "succulent",
    "delicious",
    "luscious",
    "velvety",
    "ebullient",
    "effervescent",
    "incandescent",
    "luminescent",
    "ethereal",
    "iridescent",
    "quintessential",
    "vivacious",
    "sumptuous",
    "pellucid",
    "euphonious",
    "mellifluous",
    "opalescent",
    "coruscating",
    "diaphanous",
    "lustrous",
    "rhapsodic",
    "limpid",
    "refulgent",
    "dulcet"
  )

  def randomAdjective =
    adjectives(scala.util.Random.nextInt(adjectives.size))

  def adjectiveStream: Signal[String] =
    periodic(1000, 3000, randomAdjective)
      .composeChanges(_.delay(1))

  final case class AdjectiveView():
    val adjective = adjectiveStream

    def body =
      div(
        cls(s"flex flex-col my-[8px] mx-2 overflow-hidden"),
        div(
          cls("flex whitespace-nowrap text-neutral-900 bg-neutral-300 px-2"),
          children <-- adjective.splitOneTransition(identity) { (_, string, _, t) =>
            div(
              string,
              t.width,
              t.opacity,
              t.blur
            )
          }
        )
      )
