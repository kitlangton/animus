package example.lessons.monoid

import com.raquo.laminar.api.L._
import example.Main.codeBlock
import example.lessons.zlayer.Textual
import example.slides.Slides.slideOpen
import example.{Section, Timeline, TimelineEntry}

object SecondSection extends Section {
  val title                          = "Examples"
  override val audioFileName: String = "monoid2"
  override val timeline: Timeline =
    Timeline(
      Vector(
        TimelineEntry(0.157478, 1),
        TimelineEntry(0.338707, 2),
        TimelineEntry(0.576287, 3),
        TimelineEntry(0.724673, 4),
        TimelineEntry(0.85033, 5),
        TimelineEntry(1.558799, 6),
        TimelineEntry(1.795696, 7),
        TimelineEntry(1.940311, 8),
        TimelineEntry(2.2499, 9),
        TimelineEntry(2.375462, 10),
        TimelineEntry(2.63482, 11),
        TimelineEntry(3.344763, 12),
        TimelineEntry(3.485314, 13),
        TimelineEntry(3.617947, 14),
        TimelineEntry(3.889267, 15),
        TimelineEntry(4.100663, 16),
        TimelineEntry(4.400737, 17),
        TimelineEntry(5.67807, 18),
        TimelineEntry(5.981045, 19),
        TimelineEntry(6.146677, 20),
        TimelineEntry(7.026597, 21),
        TimelineEntry(7.325673, 22),
        TimelineEntry(7.463643, 23),
        TimelineEntry(7.733487, 24),
        TimelineEntry(8.26424, 25),
        TimelineEntry(8.607731, 26)
      )
    )

  override def render($step: Signal[Int]): HtmlElement = div(
    slideOpen($step.map { _ > 17 }, true) {
      codeBlock("""
implicit val stringMonoid = new Monoid[String] {
  def empty: String = ""
  def combine(x: String, y: String): String = x ++ y
}
         """.trim)
    },
    slideOpen($step.map { _ > 0 }, true) {
      codeBlock("""
implicit val additionMonoid = new Monoid[Int] {
  def empty: Int = 0
  def combine(x: Int, y: Int): Int = x + y
}
         """.trim)
    }
  )

  override val text: Textual =
    Textual.paragraph(
      "Int can be a Monoid with 0 as the empty value and addition as the combine operation. String as well, with the empty string and concatenation."
    )
}
