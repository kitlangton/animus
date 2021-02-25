package example

import animus.SignalOps
import com.raquo.domtypes.jsdom.defs.events.TypedTargetMouseEvent
import com.raquo.laminar.api.L._
import example.Main.{animateHeight, boxStyles, boxStylesVert, glowingOverlay, textProgress}
import example.lessons.zlayer.Textual
import example.slides.Slides.slideOpen
import org.scalajs.dom
import org.scalajs.dom.{document, window}

import scala.scalajs.js.timers.setTimeout

trait Section extends Owner {
  val audioFileName: String
  val title: String
  val text: Textual
  val timeline: Timeline
  def render($step: Signal[Int]): HtmlElement

  def mainRender(
      sectionNumber: Int,
      $isUnplayed: Signal[Boolean],
      $isPlaying: Signal[Boolean],
      handlePlay: Double => Unit,
      $step: Signal[Int],
      $currentTime: Signal[Double],
      $debug: Signal[Boolean]
  ): HtmlElement =
    div(
      borderLeft("1px solid #555"),
      borderRight("1px solid #555"),
      startButton(sectionNumber, $isUnplayed, $isPlaying, handlePlay, $step, $currentTime, $debug),
      render($step)
        .amend(
          marginLeft("-1px"),
          marginRight("-1px")
        )
    )

  def startButton(
      sectionNumber: Int,
      $isUnplayed: Signal[Boolean],
      $isPlaying: Signal[Boolean],
      handlePlay: Double => Unit,
      $step: Signal[Int],
      $currentTime: Signal[Double],
      $debug: Signal[Boolean]
  ): Div = {
    val isHovering = Var(false)
    val appeared   = Var(false)

    val timelineVar = Var(Option.empty[Timeline])

    val $step2: Signal[Int] = timelineVar.signal.combineWith($debug).flatMap {
      case (Some(timeline), true) =>
        $currentTime.map { time =>
          timeline.entries.findLast(_.time <= time + 0.3).map(_.step).getOrElse(0)
        }
      case _ => $step
    }

    setTimeout(300) {
      appeared.set(true)
    }

    val actionBus = new EventBus[Unit]

    div(
      windowEvents.onKeyDown.filter(e => e.key == "u" && !e.ctrlKey) --> { _ => actionBus.writer.onNext(()) },
      windowEvents.onKeyDown.filter(e => e.key == "u" && e.ctrlKey) --> { _ => timelineVar.set(None) },
      actionBus.events.withCurrentValueOf($currentTime.combineWith($debug)) --> {
        case (_, (time, true)) =>
          if (timelineVar.now().isEmpty) {
            timelineVar.set(Some(Timeline.empty))
          } else {
            timelineVar.update(_.map(tl => tl.appended(time, tl.entries.length + 1)))
          }
        case _ => ()
      },
      position.relative,
      div(
        boxStylesVert,
        onClick --> { _ =>
          handlePlay(0.0)
        },
        onMouseEnter.mapTo(true) --> isHovering.writer,
        onMouseLeave.mapTo(false) --> isHovering.writer,
        cursor <-- $isUnplayed.map { if (_) "pointer" else "default" },
        overflow.hidden,
        background <-- $isUnplayed.combineWith(appeared.signal)
          .map {
            case (false, _) => Color.black
            case (_, false) => Color(90, 90, 155, 0.6)
            case (_, true)  => Color(30, 30, 105, 0.6)
          }
          .spring
          .map(_.toCss),
        height <-- $isUnplayed.map { if (_) 3.0 else 16.0 }.spring.px,
        height("3px"),
        div(
          display.flex,
          alignItems.center,
          position.relative,
          top <-- $isUnplayed.map { if (_) -7.5 else -1.0 }.spring.px,
          justifyContent.spaceBetween,
          fontSize("12px"),
          div(
            display.flex,
            s"$sectionNumber. ${title.toUpperCase()}$nbsp",
            div(
              opacity <-- $debug.map { if (_) 1.0 else -2.0 }.spring,
              span(
                fontWeight.bold,
                child.text <-- $step.map(_.toString)
              )
            ),
            div(
              display.flex,
              marginLeft("12px"),
              cursor.pointer,
              color("red"),
              s"• RECORDING TIMELINE",
              onClick --> { event =>
                event.stopPropagation()
                window.alert(timelineVar.now().getOrElse(Timeline.empty).copyableString)
              },
              opacity <-- $debug.combineWith(timelineVar.signal).map { case (d, t) =>
                if (d && t.isDefined) 1.0 else 0.0
              }.spring
            )
          ),
          div(
            opacity <-- $isUnplayed.map { if (_) 1.0 else -2.0 }.spring,
            display.flex,
            div(
              opacity(0.8),
              "⭢ PRESS",
              marginRight("8px")
            ),
            div(
              fontWeight.bold,
              "SPACE",
              position.relative,
              padding("0px 8px"),
              background <-- appeared.signal
                .map {
                  case true  => Color(50, 50, 125)
                  case false => Color(90, 90, 145)
                }
                .spring
                .map(_.toCss)
            ),
            div(
              opacity(0.8),
              "TO CONTINUE",
              marginLeft("8px")
            )
          )
        )
      ),
//      slideOpen($debug) {
//        var elWidth: Double     = 0.0
//        var pxPerSecond: Double = 0.0
//
//        var duration = 0.0
//        val player   = audio(src(s"audio/${audioFileName}.mp3")).ref
//        player.load()
//        player.onloadeddata = { _ =>
//          duration = player.duration
//          pxPerSecond = elWidth / duration
//          println(duration)
//        }
//
//        div(
//          boxStyles,
//          zIndex(5),
//          background("black"),
//          position.relative,
//          onMountCallback { el =>
//            elWidth = el.thisNode.ref.getBoundingClientRect().width
//            pxPerSecond = elWidth / duration
//          },
////          div(
////            position.absolute,
////            top("0"),
////            bottom("0"),
////            width("1px"),
////            background("rgba(100,100,100,1)"),
////            left <-- $currentTime.map { _ * pxPerSecond }.px
////          ),
//          children <-- timelineVar.signal.map { _.getOrElse(timeline).entries }.split(identity) { (key, entry, _) =>
//            div(
//              onClick --> { _ =>
//                handlePlay(entry.time - 0.5)
//              },
//              position.absolute,
//              left(s"${entry.time * pxPerSecond}px"),
//              top("0px"),
//              width("1px"),
//              height("24px"),
//              background("white")
//            )
//          }
//        )
//      },
      slideOpen($isUnplayed.map(!_), true) {
        div(
          onClick --> { event =>
            handlePlay(0.0)
          },
          cls("text-block"),
          cls.toggle("is-playing") <-- $isPlaying.signal,
          zIndex(2),
          position.relative,
          glowingOverlay($isUnplayed),
          text.render($step2, $debug.combineWith(timelineVar.signal.map(_.isEmpty)).map { case (b, b2) => b && b2 })
        )
      }
    )
  }
}
