package example.lessons.zlayer

import animus.SignalOps
import com.raquo.laminar.api.L._
import example.Main.{boxStyles, boxStylesVert, codeBlock, codeBlockSignal}
import example.slides.Slides.slideOpen
import example.{Section, Timeline, TimelineEntry}

sealed trait Textual { self =>
  def size: Int = self match {
    case Textual.Group(vector)       => vector.map(_.size).sum
    case Textual.Paragraph(contents) => contents.map(_.size).sum
    case Textual.Text(string)        => string.split(" ").length
    case Textual.Bold(content)       => content.size
  }

  def render($step: Signal[Int], start: Int = 0): HtmlElement = self match {
    case Textual.Group(vector) =>
      var start0 = start
      div(
        vector.map { textual =>
          val result = textual.render($step, start0)
          start0 += textual.size
          result
        }
      )
    case Textual.Paragraph(vector) =>
      var start0 = start
      div(
        padding("12px"),
        borderTop("1px solid #555"),
        vector.map { textual =>
          val result = textual.render($step, start0)
          start0 += textual.size
          result
        }
      )
    case Textual.Bold(content) =>
      content.render($step, start).amend(fontWeight.bold)
    case Textual.Text(string) =>
      val indexedWords = string.split(" ").zipWithIndex
      println(indexedWords.map(_._1).toList)
      span(
        indexedWords.map { case (str, idx) =>
          val ending = if (idx == indexedWords.lastOption.map(_._2).getOrElse(0)) "" else " "
          span(
            str + ending,
            opacity <-- $step.map { i => if (idx < i - start) 1.0 else 0.3 }.spring
          )
        }.toList
      )
  }

  def ++(that: Textual): Textual =
    (self, that) match {
      case (Textual.Paragraph(c1), Textual.Paragraph(c2)) => Textual.Paragraph(c1 ++ c2)
      case (Textual.Paragraph(c1), _)                     => Textual.Paragraph(c1 :+ that)
      case (_, Textual.Paragraph(c2))                     => Textual.Paragraph(self +: c2)
      case _                                              => Textual.Paragraph(Vector(self, that))
    }

  def bold: Textual = Textual.Bold(self)
}

object Textual {
  def group(content: Textual*): Textual  = Textual.Group(content.toVector)
  def apply(string: String): Textual     = Textual.paragraph(string)
  def paragraph(string: String): Textual = Textual.Paragraph(Vector(Textual.Text(string)))
  def bold(string: String)               = Textual.Bold(Textual.Text(string))

  implicit def string2Textual(string: String): Textual = Textual(string)

  case class Group(vector: Vector[Textual])      extends Textual
  case class Paragraph(content: Vector[Textual]) extends Textual
  case class Text(string: String)                extends Textual
  case class Bold(content: Textual)              extends Textual
}

object Section_1 extends Section {
  val title                          = "Introduction"
  override val audioFileName: String = "zlayer-intro"
  override val timeline: Timeline =
    Timeline(
      Vector(
        TimelineEntry(0.179777, 1),
        TimelineEntry(0.446249, 2),
        TimelineEntry(0.605924, 3),
        TimelineEntry(0.791884, 4),
        TimelineEntry(1.801992, 5),
        TimelineEntry(2.146949, 6),
        TimelineEntry(2.351287, 7),
        TimelineEntry(2.514252, 8),
        TimelineEntry(3.007662, 9),
        TimelineEntry(3.375855, 10),
        TimelineEntry(3.621193, 11),
        TimelineEntry(4.167389, 12),
        TimelineEntry(5.160481, 13),
        TimelineEntry(5.300405, 14),
        TimelineEntry(5.439557, 15),
        TimelineEntry(6.033704, 16),
        TimelineEntry(7.036253, 17),
        TimelineEntry(7.842782, 18)
      )
    )

  override val text: Textual =
    Textual.group(
      Textual(s"Let's explore$nbsp") ++ Textual.bold("ZLayer") ++ Textual(
        s", which ZIO uses to elegantly solve dependency injection in a type-safe, composable, lovely-to-use API."
      )
    )

  println(text)

  override def render($step: Signal[Int]): HtmlElement = div(
    slideOpen($step.map { _ >= 3 }, true) {
      div(
        position.relative,
        codeBlockSignal(
          Val(
            """
val live : ZLayer[DBConfig, Throwable, UserService] = ???
         """.trim
          ),
          $step.map {
//            case s if s >= 3 && s <= 15 => Set(1)
            case _ => Set.empty
          }
        )
      )
    }
  )

}

object Section_2 extends Section {
  val title = "Type Anatomy"

  override val text: Textual =
    Textual.group(
      "You can think of a ZLayer as a (potentially partial) function. It requires some input and returns an output.",
      "From left to right, the type parameters of ZLayer represent the input, error (which may be Nothing), and output types."
    )

  override val audioFileName: String = "zlayer-type-anatomy"

  override val timeline: Timeline =
    Timeline(
      Vector(
        TimelineEntry(0.265761, 1),
        TimelineEntry(0.590652, 2),
        TimelineEntry(0.805802, 3),
        TimelineEntry(0.939311, 4),
        TimelineEntry(1.162133, 5),
        TimelineEntry(1.431803, 6),
        TimelineEntry(2.202745, 7),
        TimelineEntry(2.338585, 8),
        TimelineEntry(2.463996, 9),
        TimelineEntry(2.824275, 10),
        TimelineEntry(3.271074, 11),
        TimelineEntry(4.15558, 12),
        TimelineEntry(4.301601, 13),
        TimelineEntry(4.543313, 14),
        TimelineEntry(4.987679, 15),
        TimelineEntry(5.615861, 16),
        TimelineEntry(5.775717, 17),
        TimelineEntry(6.067737, 18),
        TimelineEntry(6.704662, 19),
        TimelineEntry(7.516325, 20),
        TimelineEntry(7.656889, 21),
        TimelineEntry(7.789887, 22),
        TimelineEntry(8.079119, 23),
        TimelineEntry(8.450133, 24),
        TimelineEntry(8.583925, 25),
        TimelineEntry(8.840512, 26),
        TimelineEntry(9.231733, 27),
        TimelineEntry(9.456566, 28),
        TimelineEntry(9.748088, 29),
        TimelineEntry(9.996997, 30),
        TimelineEntry(10.323645, 31),
        TimelineEntry(11.112121, 32),
        TimelineEntry(11.713091, 33),
        TimelineEntry(11.881068, 34),
        TimelineEntry(12.024603, 35),
        TimelineEntry(12.260484, 36),
        TimelineEntry(12.778842, 37),
        TimelineEntry(12.980244, 38),
        TimelineEntry(13.249783, 39)
      )
    )

  override def render($step: Signal[Int]): HtmlElement = div(
    slideOpen($step.map { _ >= 0 }, true) {
      div(
        position.relative,
        div(
          position.absolute,
          fontSize("16px"),
          top("5px"),
          "▼",
          left <-- $step.map {
            case s if (30 to 31).contains(s) => 290.0
            case s if (31 to 36).contains(s) => 367.0
            case s if (37 to 39).contains(s) => 447.0
            case _                           => -20.0
          }.spring.px
        ),
        codeBlock(
          """
val exampleLayer: ZLayer[Input, Nothing, Output] = ???

def exampleFunction(input: Input): Output = ???
         """.trim
        )
      )
    }
  )
}

case class SomeSection(title: String) extends Section {

  override val text: Textual =
    Textual.group(
      "You can think of a ZLayer as a (potentially partial) function. It requires some input and returns an output.",
      "From left to right, the type parameters of ZLayer represent the input, error (which may be Nothing), and output types."
    )

  override val audioFileName: String = "zlayer-type-anatomy"

  override val timeline: Timeline =
    Timeline(
      Vector(
        TimelineEntry(0.265761, 1),
        TimelineEntry(0.590652, 2),
        TimelineEntry(0.805802, 3),
        TimelineEntry(0.939311, 4),
        TimelineEntry(1.162133, 5),
        TimelineEntry(1.431803, 6),
        TimelineEntry(2.202745, 7),
        TimelineEntry(2.338585, 8),
        TimelineEntry(2.463996, 9),
        TimelineEntry(2.824275, 10),
        TimelineEntry(3.271074, 11),
        TimelineEntry(4.15558, 12),
        TimelineEntry(4.301601, 13),
        TimelineEntry(4.543313, 14),
        TimelineEntry(4.987679, 15),
        TimelineEntry(5.615861, 16),
        TimelineEntry(5.775717, 17),
        TimelineEntry(6.067737, 18),
        TimelineEntry(6.704662, 19),
        TimelineEntry(7.516325, 20),
        TimelineEntry(7.656889, 21),
        TimelineEntry(7.789887, 22),
        TimelineEntry(8.079119, 23),
        TimelineEntry(8.450133, 24),
        TimelineEntry(8.583925, 25),
        TimelineEntry(8.840512, 26),
        TimelineEntry(9.231733, 27),
        TimelineEntry(9.456566, 28),
        TimelineEntry(9.748088, 29),
        TimelineEntry(9.996997, 30),
        TimelineEntry(10.323645, 31),
        TimelineEntry(11.112121, 32),
        TimelineEntry(11.713091, 33),
        TimelineEntry(11.881068, 34),
        TimelineEntry(12.024603, 35),
        TimelineEntry(12.260484, 36),
        TimelineEntry(12.778842, 37),
        TimelineEntry(12.980244, 38),
        TimelineEntry(13.249783, 39)
      )
    )

  override def render($step: Signal[Int]): HtmlElement = div(
    slideOpen($step.map { _ >= 0 }, true) {
      div(
        position.relative,
        div(
          position.absolute,
          fontSize("16px"),
          top("5px"),
          "▼",
          left <-- $step.map {
            case s if (30 to 31).contains(s) => 290.0
            case s if (31 to 36).contains(s) => 367.0
            case s if (37 to 39).contains(s) => 447.0
            case _                           => -20.0
          }.spring.px
        ),
        codeBlock(
          """
val exampleLayer: ZLayer[Input, Nothing, Output] = ???

def exampleFunction(input: Input): Output = ???
         """.trim
        )
      )
    }
  )
}
