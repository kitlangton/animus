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
    cls(
      "flex text-6xl tracking-widest"
    ),
    fontFamily("Bebas Neue"),
    List("A", "N", "I", "M", "U", "S").map { letter =>
      div(
        cls("text-6xl relative"),
        fontSize("80px"),
        letter,
        top <-- wobble.px,
        left <-- wobble.px,
        styleProp("filter") <-- wobble.spring.map { w =>
          s"blur(${w}px)"
        },
        transform <-- wobble.spring.map { w =>
          s"rotate(${w * 5 - 5}deg)"
        }
      )
    }
  )

  final case class Todo(title: String)

  val todos = Vector(
    Todo("Learn Scala 3"),
    Todo("Learn Laminar"),
    Todo("Learn Animus"),
    Todo("Build something cool"),
    Todo("Profit!"),
    Todo("Question the morality of your success"),
    Todo("Donate all your profits in a fit of guilt"),
    Todo("Start a new project in search of purpose and redemption"),
    Todo("Fail repeatedly, questioning your competence"),
    Todo("Descend into isolation and existential crisis"),
    Todo("Reread 'Notes from Underground', find frightening parallels"),
    Todo("Attempt to connect with others but fail due to technology alienation"),
    Todo("Fall into a pit of despair"),
    Todo("Encounter a stranger who challenges your perspective"),
    Todo("Engage in philosophical debates with the stranger"),
    Todo("Become absorbed in the works of Nietzsche"),
    Todo("Decide to write a manifesto"),
    Todo("Lose yourself in the process, alienating friends and family"),
    Todo("Eventually finish and publish the manifesto"),
    Todo("It is met with heavy criticism and misunderstanding"),
    Todo("Feel crushed and spiraling deeper into solitude"),
    Todo("Attempt to find comfort in stoicism"),
    Todo("Only find more questions, not answers"),
    Todo("Confront your mortality and the impermanence of all things"),
    Todo("Decide to reinvent yourself and re-learn compassion"),
    Todo("Stumble upon an old machine you'd created"),
    Todo("Discard it, reflecting on the naivety of your past ambitions"),
    Todo("Volunteer at a local shelter in search of humility"),
    Todo("Conduct philosophical talks at the shelter"),
    Todo("Slowly start to regain faith in humanity"),
    Todo("Write a book about your transformation"),
    Todo("Your book touches many people, becomes a success"),
    Todo("Feel a sense of satisfaction but also emptiness"),
    Todo("Realize you still have lots to learn"),
    Todo("Commit to lifelong learning"),
    Todo("Inspire others with your journey"),
    Todo("Reflect on the futility of ambition"),
    Todo("Accept the fluidity and ambiguity of life"),
    Todo("Find peace in solitude and simplicity"),
    Todo("Live out your days quietly, introspectively"),
    Todo("Pass on, leaving behind a complex legacy"),
    Todo("Silence"),
    Todo("Silence"),
    Todo("Silence"),
    Todo("The horrid buzzing af a colossal, metallic wasp"),
    Todo("SCREAMING ENDLESS SCREAMING"),
    Todo("Transition into the ethereal, leaving earthly existence behind"),
    Todo("Experience the sensation of floating in a vast, tranquil emptiness"),
    Todo("Realize consciousness still persists, even in the void"),
    Todo("Recognize the void itself is an embodiment of your consciousness"),
    Todo("Start to perceive a faint shimmer in the distance"),
    Todo("Move towards it, feeling an inexplicable pull"),
    Todo("Emerge in a new world, reincarnated as a curious child"),
    Todo("Experience a life filled with learning and exploration"),
    Todo("Develop an interest in computers and technology"),
    Todo("Stumble upon an old book about programming in your teens"),
    Todo("Intrigued, you spend countless hours studying it"),
    Todo("Discover the power of creating and manipulating software"),
    Todo("Specialize in computer science, focusing on programming languages"),
    Todo("Start from the basics - 'Hello World' in Python"),
    Todo("Gradually move on to more complex tasks"),
    Todo("Descend into the world of Java, understanding OOP"),
    Todo("Eventually, you come across a peculiar language - Scala"),
    Todo("Dive deep into Scala 2, mesmerized by its elegance and power"),
    Todo("As your understanding grows, you prepare to take on a new challenge"),
    Todo("And so begins your journey to Learn Scala 3")
  )

  // start at the given index, then take the next 3 elements, rotating back to the start
  def rotate[A](start: Int, take: Int, as: Vector[A]): Vector[A] =
    val size = as.size
    val i    = start      % size
    val j    = (i + take) % size
    if j < i then as.slice(i, size) ++ as.slice(0, j)
    else as.slice(i, j)

  val todosSignal =
    EventStream
      .periodic(1000)
      .toSignal(0)
      .map(rotate(_, 3, todos))
      .map(_.zipWithIndex)

  val colors = List("text-neutral-200", "text-neutral-400", "text-neutral-600")

  def todosView = div(
    cls("flex flex-col mt-32 text-3xl tracking-widest"),
    fontFamily("Bebas Neue"),
    children <-- todosSignal.splitTransition(_._1) { (_, value, signal, t) =>
      div(
        cls("transition-colors duration-300"),
        cls <-- signal.map { value =>
          colors(value._2 % colors.size)
        },
        div(
          cls("flex space-x-2 py-2 "),
          div(
            cls("flex-wrap flex  space-x-2"),
            transformOrigin("left"),
            transform <-- signal
              .map { value =>
                value._2 match
                  case 0 => 1.0
                  case 1 => 0.9
                  case _ => 0.8
              }
              .spring
              .map { s =>
                s"scale(${s})"
              },
            styleProp("filter") <-- signal
              .map { value =>
                value._2 match
                  case 0 => 0.0
                  case 1 => 0.5
                  case _ => 1
              }
              .spring
              .map { s =>
                s"blur(${s}px)"
              },
            value._1.title.split(" ").map { word =>
              div(
                word,
                cls("relative"),
                top <-- wobble.px,
                left <-- wobble.px,
                transform <-- wobble.spring.map(w => s"rotate(${w * 3 - 3}deg)")
              )
            }
          )
        ),
        t.opacity,
        t.height,
        t.blur
      )
    }
  )

  def body =
    div(
      fontFamily("Playfair Display"),
      cls("z-10 relative font-bold text-neutral-100 h-screen flex flex-col"),
      cls("tracking-wider flex-col p-14 sm:p-24 overflow-x-hidden"),
      animusText,
      div(
        cls("text-lg mt-4 items-center max-w-lg italic"),
        div(
          cls("items-center flex whitespace-nowrap overflow-hidden"),
          "is a",
          children <-- article.splitOneTransition(identity) { (_, string, _, t) =>
            div(
              cls("overflow-hidden"),
              string,
              t.width
            )
          },
          adjectiveOne.body,
          div("and"),
          AdjectiveView().body
        ),
        div("spring animation library for Scala.js.")
      )
//      todosView
    )

  ////////////////
  // ADJECTIVES //
  ////////////////

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
        cls("flex flex-col my-[8px] mx-2 overflow-hidden not-italic"),
        // skew
        transform("skewX(-15deg)"),
        fontFamily("Bebas Neue"),
        fontSize("24px"),
        div(
          cls("flex whitespace-nowrap text-neutral-900 bg-neutral-300 px-1.5 py-1 relative top-1 overflow-hidden"),
          children <-- adjective.splitOneTransition(identity) { (_, string, _, t) =>
            div(
              cls("overflow-hidden"),
              string,
              t.width,
              t.opacity,
              t.blur
            )
          }
        )
      )
