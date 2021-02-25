package example.lessons.monoid

import animus.SignalOps
import com.raquo.laminar.api.L._
import example.Docs.flexCenter
import example.Main.boxStyles
import example.lessons.zlayer.Textual
import example.slides.Slides.slideOpen
import example.{Color, Section, Timeline, TimelineEntry}

object ThirdSection extends Section {
  val title                          = "Test"
  override val audioFileName: String = "test"
  override val timeline: Timeline =
    Timeline(
      Vector(
        TimelineEntry(0.205108, 1),
        TimelineEntry(0.437759, 2),
        TimelineEntry(0.789713, 3),
        TimelineEntry(0.949025, 4),
        TimelineEntry(2.078429, 5),
        TimelineEntry(2.461037, 6),
        TimelineEntry(2.657134, 7),
        TimelineEntry(2.810864, 8),
        TimelineEntry(3.014613, 9),
        TimelineEntry(3.378508, 10),
        TimelineEntry(3.672467, 11),
        TimelineEntry(3.84456, 12),
        TimelineEntry(4.305786, 13),
        TimelineEntry(4.907245, 14),
        TimelineEntry(5.27418, 15),
        TimelineEntry(5.721738, 16),
        TimelineEntry(5.995639, 17),
        TimelineEntry(6.181445, 18),
        TimelineEntry(7.767198, 19),
        TimelineEntry(7.940844, 20),
        TimelineEntry(8.154305, 21),
        TimelineEntry(8.329089, 22),
        TimelineEntry(8.472163, 23),
        TimelineEntry(8.900591, 24),
        TimelineEntry(9.273975, 25),
        TimelineEntry(9.521743, 26)
      )
    )

  override val text: Textual =
    Textual.paragraph(
      "This is a test. This is just a test of a silly, interactive teaching framework I’m hacking together. Hopefully it will become much cooler in time."
    )

  override def render($step: Signal[Int]): HtmlElement = div(
    slideOpen($step.map { _ > 0 }) {
      div(
        boxStyles,
        "HELLO EVERYONE!"
      )
    },
    slideOpen($step.map { _ > 5 }) {
      div(
        boxStyles,
        span(
          s"We're on word: ",
          opacity(0.6)
        ),
        child.text <-- $step.map(n => (n).toString),
        span(
          opacity(0.6),
          " / 26"
        )
      )
    },
    slideOpen($step.map { _ >= 8 }) {
      div(
        boxStyles,
        display.flex,
        padding("2px"),
        List.tabulate(26) { idx =>
          div(
            flex("1"),
            height("30px"),
            border("1px solid black"),
            flexCenter,
            fontSize("8px"),
            color("black"),
            (idx + 1).toString,
            background <-- $step.map { n =>
              if (idx <= n) Color.white
              else Color.white.copy(alpha = 0)
            }.spring.map(_.toCss)
          )
        }
      )
    }
  )

}
