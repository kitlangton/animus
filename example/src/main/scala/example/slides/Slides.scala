package example.slides

import com.raquo.laminar.api.L._
import example.{Transitions, styles}
import org.scalajs.dom.document

case class SlidesState(currentSlide: Int, sections: Map[Int, Int] = Map.empty) {
  def sectionForSlide(index: Int): Int = sections.getOrElse(index, 0)

  def currentSection: Int = sectionForSlide(currentSlide)

  def nextSlide: SlidesState = copy(currentSlide = currentSlide + 1)
  def prevSlide: SlidesState = copy(currentSlide = (currentSlide - 1).max(0))

  def nextSection: SlidesState = copy(sections = sections.updatedWith(currentSlide) {
    case Some(value) => Some(value + 1)
    case None        => Some(0)
  })

  def prevSection: SlidesState = copy(sections = sections.updatedWith(currentSlide) {
    case Some(value) => Some((value - 1).max(0))
    case None        => Some(0)
  })
}

object SlidesState {
  def empty: SlidesState = SlidesState(0)
}

object Slides extends Owner {
  lazy val slidesStateVar: Var[SlidesState]  = Var(SlidesState.empty)
  lazy val $slidesState: Signal[SlidesState] = slidesStateVar.signal

  lazy val userControlModeVar: Var[Boolean] = Var(false)

  lazy val $currentSlide: Signal[Int]   = $slidesState.map(_.currentSlide)
  lazy val $currentSection: Signal[Int] = $slidesState.map(_.currentSection)

  def view: Div = {
    div(
      windowEvents.onKeyDown.map(_.key) --> Observer[String] {
        case "f"          => val _ = document.body.requestFullscreen()
        case "ArrowRight" => slidesStateVar.update(_.nextSlide)
        case "ArrowLeft"  => slidesStateVar.update(_.prevSlide)
        case "ArrowUp"    => slidesStateVar.update(_.prevSection)
        case "ArrowDown"  => slidesStateVar.update(_.nextSection)
        case _            => ()
      },
      position("fixed"),
      top("0"),
      right("0"),
      left("0"),
      bottom("0"),
      width("100vw"),
      height("100vh"),
      slideshow
    )
  }

  private lazy val slideshow =
    div(
      overflowX.hidden,
      overflowY.scroll,
      marginTop := "40px",
      styles.fixedFullScreen,
      renderSlides(
        //
      )
    )

  val $lastAndCurrentSlide: Signal[(Int, Int)] =
    $slidesState.map(_.currentSlide)
      .foldLeft[(Int, Int)](s => (s, s)) { case ((_, b), c) => (b, c) }

  private def renderSlides(slides: Slide*): Div =
    div(
      slides.zipWithIndex.map { case (slide, idx) =>
        div(
          child.maybe <-- Transitions.transitionOption(
            $currentSlide.map(curr => Option.when(curr == idx)(slide)),
            { $isVisible =>
              val offset = $isVisible.combineWith($lastAndCurrentSlide)
                .map {
                  case (true, _) => 0.0
                  case (_, (last, curr)) if last <= curr =>
                    if (curr > idx) -200.0 else 200.0
                  case (_, (_, curr)) =>
                    if (curr < idx) 200.0 else -200.0
                }
                .spring
                .px
              Seq(
                position.relative,
                left <-- offset,
                opacity <-- $isVisible.map { if (_) 1.0 else 0.0 }.spring
              )
            }
          ) { slide =>
            div(
              child <-- slide.map(s => renderSlide(idx, s.render($slidesState.map(_.sectionForSlide(idx)))))
            )
          }
        )
      }
    )

  def slideOpen($isOpen: Signal[Boolean], dynamicHeight: Boolean = false)(content: => HtmlElement): HtmlElement =
    div(child.maybe <-- Transitions.slide($isOpen, dynamicHeight)(content))

  private def renderSlide(number: Int, content: HtmlElement) = {
    val $isActive = $currentSlide.map(_ == number)

    div(
      position.absolute,
      left := "50%",
      div(
        width := "800px",
        zIndex <-- $isActive.map { b => if (b) 100 else 0 },
        transform := "translateX(-50%)",
        position.relative,
        content
      )
    )
  }

}
