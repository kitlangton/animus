package example.slides

import animus.{ObservableOps, SignalOps}
import com.raquo.laminar.api.L
import com.raquo.laminar.api.L._
import example.Highlight
import example.slides.Components.codeBlock
import example.slides.Slides.{slideOpen, slideOpenWidth}

object LiveCodeSlide extends Slide {
  override def render($section: L.Signal[Int]): HtmlElement = {
    div(
      (1 to 35).map { i =>
        slideOpen($section.map((i to 35).toSet)) {
          h1("LIVE CODING TIME", color("red"), textAlign.center)
        }
      }.reverse
    )
  }
}

object Cool extends Slide {
  override def render($section: Signal[Int]): HtmlElement =
    div(
      slideOpen($section.map(_ == 0)) {
        h1("...", opacity(0.6))
      },
      slideOpen($section.map(_ == 1)) {
        h1("HELLO")
      },
      slideOpen($section.map(_ == 2)) {
        h1("MY NAME IS ", span("KIT", color("orange")))
      },
      slideOpen($section.map(_ == 4)) {
        h2("WE'RE HIRING!", opacity(0.6))
      },
      slideOpen($section.map(Set(3, 4))) {
        h1("I WRITE CODE AT ", span("AXONI", color("cyan")))
      },
      slideOpen($section.map(Set(5))) {
        h1("PART I", opacity(0.6))
      },
      slideOpen($section.map(Set(5))) {
        h1(h1("FREE COFFEE"))
      },
      slideOpen($section.map(Set(5))) {
        h1(h1("☕️"))
      },
      slideOpen($section.map(Set(6))) {
        h1("PART II", opacity(0.6))
      },
      slideOpen($section.map(Set(6))) {
        h1(h1("FUNCTIONAL EFFECTS"))
      },
      slideOpen($section.map(Set(6))) {
        h1(h1("MAD MAN"))
      },
      slideOpen($section.map(Set(6))) {
        h1("AGAINST THE CLOCK", color("red"))
      },
      slideOpen($section.map(Set(6))) {
        h1(h1("LIVE CODE"))
      },
      slideOpen($section.map(Set(6))) {
        h1("OH NO", color("red"))
      }
    )
}

object FreeStuff extends Slide {
  override def render($section: Signal[Int]): HtmlElement =
    div(
      slideOpen($section.map(Set(0, 1, 2, 3))) {
        h2("FREE", opacity(0.6), margin("0"))
      },
      slideOpen($section.map(_ == 0)) {
        h1(span("STUFF"))
      },
      slideOpen($section.map(_ == 1)) {
        h1("MONAD")
      },
      slideOpen($section.map(_ == 2)) {
        h1("APPLICATIVE")
      },
      slideOpen($section.map((3 to 7).toSet)) {
        h1(
          slideOpenWidth($section.map(_ == 4)) {
            span(s"FREE${nbsp}", opacity(0.6))
          },
          slideOpenWidth($section.map(_ == 5)) {
            span(s"MONOID${nbsp}", opacity(0.6))
          },
          span(
            "MONOID"
          )
        )
      },
      slideOpen($section.map(_ == 4)) {
        h3(em(""""The List is the Free Monoid""""), span(" — Some Guru", opacity(0.6)))
      },
      slideOpen($section.map((7 to 11).toSet)) {
        monoidCodeBlock
      },
      slideOpen($section.map((8 to 9).toSet), dynamicHeight = true) {
        div(
          slideOpenWidth($section.map(_ == 8)) {
            div(display.flex, pre(span("combine", color("orange")), "(", span("empty", opacity(0.6)), ", x)"))
          },
          slideOpenWidth($section.map(_ == 9)) {
            div(pre("x"))
          },
          margin("0")
        )
      },
      monoidExample($section, "0", "+", "3"),
      monoidExample($section, "1", "*", "8"),
      monoidExample($section, """""""", "++", """"WOWIES""""),
      monoidExample($section, "List.empty", "++", "List(1,2,3)"),
      monoidExample($section, "true", "&&", "false"),
      monoidExample($section, "false", "||", "true"),
      slideOpen($section.map((10 to 17).toSet)) {
        h1("A")
      },
      slideOpen($section.map((11 to 17).toSet)) {
        h1("FREE", color("orange"))
      },
      slideOpen($section.map((12 to 17).toSet)) {
        h1(em("WHATEVER"))
      },
      slideOpen($section.map((13 to 17).toSet)) {
        h1("IS BASICALLY")
      },
      slideOpen($section.map((14 to 17).toSet)) {
        h1("A HYPER-SPECIFIC,", color("orange"))
      },
      slideOpen($section.map((14 to 17).toSet)) {
        h1("HARD-CODED", color("orange"))
      },
      div(
        display.flex,
        slideOpen($section.map((18 to 23).toSet)) {
          h5(
            width("200px"),
            "A ",
            span("FREE ", color("orange")),
            span(em("MONOID ")),
            span("IS BASICALLY "),
            span("A HYPER-SPECIFIC, ", color("orange")),
            span("HARD-CODED ", color("orange")),
            span("SYNTAX TREE ", color("orange")),
            span("FOR "),
            span(em("MONOID*"))
          )
        },
        slideOpen($section.map((20 to 23).toSet)) {
          h5(
            marginLeft("20px"),
            em("*WITH A PLUS ONE")
          )
        }
      ),
      slideOpen($section.map((15 to 17).toSet)) {
        h1("SYNTAX TREE", color("orange"))
      },
      slideOpen($section.map((16 to 17).toSet)) {
        h1("FOR SAID")
      },
      slideOpen($section.map((17 to 17).toSet)) {
        h1(em("WHATEVER"))
      },
      slideOpen($section.map((18 to 23).toSet)) {
        monoidCodeBlock
      },
      slideOpen($section.map((19 to 19).toSet)) {
        codeBlock(
          """
sealed trait Fronoid

case object Empty extends Fronoid
case class Combine(x: Fronoid, y: Fronoid) extends Fronoid
            """.trim
        )
      },
      slideOpen($section.map((20 to 23).toSet)) {
        codeBlock(
          """
sealed trait Fronoid[+A]

case object Empty  extends Fronoid[Nothing]
case class Combine[+A](x: Fronoid[A], y: Fronoid[A]) extends Fronoid[A]
            """.trim
        )
      },
      slideOpen($section.map((21 to 23).toSet)) {
        codeBlock(
          """
case class Value[+A](a: A) extends Fronoid[A]
            """.trim
        )
      },
      slideOpen($section.map((22 to 23).toSet)) {
        codeBlock(
          """
sealed trait List[+A]

case object Nil extends List[Nothing]
case class Cons[+A](head: A, tail: List[A]) extends List[A]
            """.trim
        )
      },
      slideOpen($section.map((23 to 23).toSet)) {
        codeBlock(
          """
def fold(z: A)(op: (A, A) => A): A = ???
            """.trim
        )
      },
      slideOpen($section.map((24 to 38).toSet)) {
        codeBlock(
          """
trait Monad[F[_]] {
  def pure[A](value: A): F[A]
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
}
            """.trim
        )
      },
      slideOpen($section.map((25 to 38).toSet)) {
        strong(span("I.", opacity(0.6)), " SUPER SPECIFIC SYNTAX TREE")
      },
      slideOpen($section.map((26 to 38).toSet)) {
        codeBlock(
          """
sealed trait Fonad[F[_], A] 

case class Pure[F[_], A](value: A) extends Fonad[F,A]
case class FlatMap[F[_], A, B](fa: Fonad[F, A], f: A => Fonad[F, B]) extends Fonad[F,B]
            """.trim
        )
      },
      slideOpen($section.map((27 to 38).toSet)) {
        strong(span("II.", opacity(0.6)), " THE SIDECAR")
      },
      slideOpen($section.map((28 to 38).toSet)) {
        codeBlock(
          """
case class Effect[F[_], A](value: F[A]) extends Fonad[F,A]
            """.trim
        )
      }
    )

  private def monoidCodeBlock = {
    codeBlock(
      """
trait Monoid[A] {
  def empty: A
  def combine(x: A, y: A): A
}
            """.trim
    )
  }

  private def monoidExample($section: L.Signal[Int], empty: String, combine: String, x: String) = {
    slideOpen($section.map((8 to 9).toSet), dynamicHeight = true) {
      div(
        margin("0"),
        display.flex,
        slideOpenWidth($section.map(_ == 8)) {
          div(display.flex, pre(empty, opacity(0.6)), pre(s" $combine ", color("orange")))
        },
        pre(x)
      )
    }
  }
}
