package example

import animus.*
import com.raquo.laminar.api.L.*
import org.scalajs.dom.document

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
      cls("bg-neutral-900 font-bold min-h-screen text-6xl text-neutral-100"),
      cls("tracking-wider flex-col p-24"),
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
    )

  val adjectives = Vector(
    "aesthetic",
    "pulchritudinous",
    "resplendent",
    "seraphic",
    "sublime",
    "exquisite",
    "succulent",
    "delicious",
    "luscious",
    "velvety"
  )

  def randomAdjective =
    adjectives(scala.util.Random.nextInt(adjectives.size))

  def adjectiveStream: Signal[String] =
    EventStream
      .periodic(
        700
      )
      .mapTo(randomAdjective)
      .toSignal(randomAdjective)
      .composeChanges(_.delay(1))

  final case class AdjectiveView():
    val adjective = adjectiveStream

    def body =
      div(
        cls(s"flex flex-col my-[8px] rounded-sm bg-neutral-700 px-1.5 mx-2 overflow-hidden"),
        div(
          cls("flex whitespace-nowrap"),
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
