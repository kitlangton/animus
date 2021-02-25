package example

import animus.SignalOps
import com.raquo.domtypes.jsdom.defs.events.TypedTargetMouseEvent
import com.raquo.laminar.api.L._
import example.Main.{animateHeight, boxStyles, boxStylesVert, glowingOverlay, textProgress}
import example.lessons.zlayer.Textual
import example.slides.Slides.slideOpen
import org.scalajs.dom

import scala.scalajs.js.timers.setTimeout

trait Section extends Owner {
  val audioFileName: String
  val title: String
  val text: Textual
  val timeline: Timeline
  def render($step: Signal[Int]): HtmlElement

  def debug: Div = {
    val timelineVar = Var(Timeline.empty)
    val unplayed    = Var(true)
    val isPlaying   = Var(false)
    val stepVar     = Var(0)
    val debugging   = Var(false)

    val audioPlayer = {
      val player = audio().ref
      player
    }

    audioPlayer.src = s"audio/$audioFileName.mp3"
    audioPlayer.load()

    val duration = Var(0.0)
//    val words    = text.split(" ").toVector

//    audioPlayer.onloadeddata = { _ =>
//      val dur = audioPlayer.duration
//      duration.set(dur)
//
//      val timePerWord = dur / words.length.toDouble
//
//      val entries = (0 to words.length).map { i => TimelineEntry(timePerWord * i, i + 1) }.toVector
//      timelineVar.set(Timeline(entries))
//    }

    audioPlayer.ontimeupdate = { _ =>
      if (!debugging.now()) {
        val time = audioPlayer.currentTime
        timelineVar.now().entries.findLast(_.time < time + 0.3).map(_.step).foreach { step => stepVar.set(step) }
      }
    }

    def play(time: Double = 0.0) = {
      unplayed.set(false)
      audioPlayer.currentTime = time
      audioPlayer.play()
      isPlaying.set(true)
    }

    div(
      windowEvents.onKeyDown --> { ev =>
        if (ev.key == "ArrowDown") {
          if (audioPlayer.paused) {
            debugging.set(true)
            timelineVar.set(Timeline.empty)
            stepVar.set(0)
            play()
          } else {
            val nextStep = stepVar.now() + 1
            timelineVar.update(_.appended(audioPlayer.currentTime, nextStep))
            stepVar.set(nextStep)
          }
        }
      },
      div(
        borderLeft("1px solid #555"),
        borderRight("1px solid #555"),
        startButton(
          1,
          unplayed.signal,
          isPlaying.signal,
          time => {
            debugging.set(false)

            play(time)
          },
          stepVar.signal,
          Val(true)
        ),
        render(stepVar.signal)
          .amend(
            marginLeft("-1px"),
            marginRight("-1px")
          )
      ),
      child <-- duration.signal.map { duration =>
        div(
          position.relative,
          height(s"${duration * 20}px"),
          width("1px"),
          borderLeft("1px solid gray")
//          words.zipWithIndex.map { case (word, idx) =>
//            div(
//              fontSize("8px"),
//              word,
//              position.absolute,
//              top <-- timelineVar.signal.map { timeline =>
//                timeline.entries
//                  .find(_.step == idx)
//                  .map { entry =>
//                    entry.time * 20
//                  }
//                  .getOrElse(0.0)
//              }.px
//            )
//          }
        )
      },
      pre(
        fontSize("10px"),
        child.text <-- timelineVar.signal.map(_.toString)
      )
    )
  }

  def mainRender(
      sectionNumber: Int,
      $isUnplayed: Signal[Boolean],
      $isPlaying: Signal[Boolean],
      handlePlay: Double => Unit,
      $step: Signal[Int],
      $debug: Signal[Boolean]
  ): HtmlElement = {
    val debugMode = Var(false)

    div(
      child <-- debugMode.signal.map {
        if (_) debug
        else
          div(
            borderLeft("1px solid #555"),
            borderRight("1px solid #555"),
            startButton(sectionNumber, $isUnplayed, $isPlaying, handlePlay, $step, $debug),
            render($step)
              .amend(
                marginLeft("-1px"),
                marginRight("-1px")
              )
          )
      }
    )
  }

  def startButton(
      sectionNumber: Int,
      $isUnplayed: Signal[Boolean],
      $isPlaying: Signal[Boolean],
      handlePlay: Double => Unit,
      $step: Signal[Int],
      $debug: Signal[Boolean]
  ): Div = {
    val isHovering = Var(false)
    val appeared   = Var(false)

    setTimeout(300) {
      appeared.set(true)
    }

    div(
      cls("text-block"),
      cls.toggle("is-playing") <-- $isPlaying.signal,
      cls.toggle("played") <-- $isPlaying.signal.combineWith($isUnplayed).map { case (b1, b2) => !(b1 || b2) },
      position.relative,
      div(
        boxStylesVert,
//        onClick --> { (event) =>
//          handlePlay(0.0)
//        },
        onMouseEnter.mapTo(true) --> isHovering.writer,
        onMouseLeave.mapTo(false) --> isHovering.writer,
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
      slideOpen($debug) {
        var elWidth: Double     = 0.0
        var pxPerSecond: Double = 0.0
        div(
          boxStyles,
          zIndex(5),
          background("black"),
          position.relative,
          overflowX.scroll,
          onMountBind { el =>
            elWidth = el.thisNode.ref.getBoundingClientRect().width
            val lastPosition: Double = timeline.entries.lastOption.map(_.time).getOrElse(5)
            pxPerSecond = elWidth / lastPosition
            val start = el.thisNode.ref.getBoundingClientRect().left
            onClick --> { event =>
              println(event.clientX, start)
              handlePlay((event.clientX - start).toDouble / pxPerSecond)
            }
          },
          div(
            position.absolute,
            top("0"),
            bottom("0"),
            left("0"),
            background("rgba(100,100,100,0.5)"),
            width <-- $step.map(step =>
              timeline.entries.find(_.step == step).map(_.time).getOrElse(10.0) * pxPerSecond
            ).spring.px
          ),
          onMountInsert { el =>
            timeline.entries.map { entry =>
              div(
                position.absolute,
                left(s"${entry.time * pxPerSecond}px"),
                width("5px"),
                height("5px"),
                borderRadius("5px"),
                background("white"),
                opacity <-- $step.map { i =>
                  if (entry.step <= i) 1.0 else 0.4
                }.spring
              )
            }
          }
        )
      },
      slideOpen($isUnplayed.map(!_)) {
        div(
          onClick --> { event =>
            handlePlay(0.0)
          },
          zIndex(2),
          position.relative,
          glowingOverlay($isUnplayed),
          text.render($step)
        )
      }
    )
  }
}
