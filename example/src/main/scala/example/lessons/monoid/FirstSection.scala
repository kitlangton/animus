package example.lessons.monoid

import animus.SignalOps
import com.raquo.laminar.api.L._
import example.Main.codeBlock
import example.lessons.zlayer.Textual
import example.slides.Slides.slideOpen
import example.{Section, Timeline, TimelineEntry}

object FirstSection extends Section {
  val title                          = "Introduction"
  override val audioFileName: String = "monoid"
  override val timeline: Timeline =
    Timeline(
      Vector(
        TimelineEntry(0.17974, 1),
        TimelineEntry(0.398355, 2),
        TimelineEntry(0.628249, 3),
        TimelineEntry(0.948265, 4),
        TimelineEntry(1.161217, 5),
        TimelineEntry(1.367986, 6),
        TimelineEntry(1.494538, 7),
        TimelineEntry(1.990621, 8),
        TimelineEntry(2.402305, 9),
        TimelineEntry(3.495679, 10),
        TimelineEntry(4.196114, 11),
        TimelineEntry(4.439381, 12),
        TimelineEntry(4.762513, 13),
        TimelineEntry(5.24528, 14),
        TimelineEntry(5.519742, 15),
        TimelineEntry(5.711581, 16),
        TimelineEntry(6.279567, 17),
        TimelineEntry(6.71558, 18),
        TimelineEntry(7.095833, 19),
        TimelineEntry(8.026264, 20),
        TimelineEntry(8.335817, 21),
        TimelineEntry(9.195791, 22),
        TimelineEntry(9.60884, 23),
        TimelineEntry(9.908071, 24),
        TimelineEntry(10.369965, 25),
        TimelineEntry(10.736191, 26),
        TimelineEntry(11.106672, 27),
        TimelineEntry(11.399212, 28),
        TimelineEntry(11.706413, 29),
        TimelineEntry(12.104927, 30),
        TimelineEntry(12.357731, 31),
        TimelineEntry(12.513356, 32)
      )
    )

  override val text: Textual =
    Textual.paragraph(
      "A monoid is a type class with two functions. Empty, a nullary function which summons a special empty value. And combine, a binary function which smushes any two values into a third."
    )

  override def render($step: Signal[Int]): HtmlElement = div(
    slideOpen($step.map { _ > 0 }) {
      div(
        position.relative,
        codeBlock("""
trait Monoid[A] {
  def empty: A
  def combine(x: A, y: A): A
}
         """.trim),
        div(
          position.absolute,
          width("8px"),
          height("8px"),
          borderRadius("8px"),
          background("yellow"),
          opacity <-- $step.map { s => if (s >= 10 && s <= 30) 1.0 else 0.0 }.spring,
          top <-- $step.map { s => if (s <= 20) 50.0 else 70.0 }.spring.px,
          left("30px")
        )
      )
    }
  )

}
