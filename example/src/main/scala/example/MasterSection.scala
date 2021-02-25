package example

import animus.{RAFStream, SignalOps}
import com.raquo.laminar.api.L._
import example.Main.boxStyles
import org.scalajs.dom.html.Audio
import org.scalajs.dom.raw.AudioContext
import org.scalajs.dom.{document, window}

class MasterSection(sections: Vector[Section]) {
  private val debugVar: Var[Boolean]                 = Var(true)
  private val currentSectionVar: Var[Int]            = Var(0)
  private val sectionStepMap: Var[Map[Section, Int]] = Var(Map.empty[Section, Int].withDefaultValue(-1))

  private val $visibleSections: Signal[Vector[Section]] =
    currentSectionVar.signal.map { n => sections.take(n + 1) }

  def keyDownEvents = windowEvents.onKeyDown --> { event =>
    event.key match {
      case "‡"          => debugVar.update(!_)
      case "ArrowRight" => currentSectionVar.update(_ + 1)
      case "ArrowLeft"  => currentSectionVar.update(_ - 1)
      case " " =>
        event.preventDefault()
        if (playingSection.now().isEmpty)
          handlePlay(sections(currentSectionVar.now()))
        else {
          playingSection.set(None)
          audioPlayer.pause()
        }
      case _ => ()
    }
  }

  def onSectionFinish(section: Section): Unit = {
    val index = sections.indexOf(section)
    currentSectionVar.set(index + 1)
  }

  val playedSections     = Var(Set.empty[Section])
  val playingSection     = Var(Option.empty[Section])
  val audioPlayer: Audio = audio().ref

  def handlePlay(section: Section, time: Double = 0.0): Unit = {
    Sounds.playDink()
    playedSections.update(_ + section)
    playingSection.set(Some(section))
    audioPlayer.src = s"audio/${section.audioFileName}.mp3"
    audioPlayer.currentTime = time
    audioPlayer.play()
    sectionStepMap.update(_.updated(section, 0))

    audioPlayer.onended = { _ =>
//      Sounds.playPing()
      playingSection.set(None)
      if (audioPlayer.currentTime >= audioPlayer.duration) {
        onSectionFinish(section)
      }
    }

    audioPlayer.ontimeupdate = { _ =>
      val time = audioPlayer.currentTime
      section.timeline.entries
        .findLast(_.time < time + 0.3)
        .map(_.step)
        .foreach { step =>
          sectionStepMap.update(_.updated(section, step))
        }
    }
  }

  def progressView: Div = div(
    boxStyles,
    display.flex,
    List.tabulate(sections.length) { idx =>
      val section    = sections(idx)
      val $isActive  = currentSectionVar.signal.map { _ == idx }
      val $hasPlayed = playedSections.signal.map { _.contains(section) }

      div(
        width("8px"),
        height("8px"),
        border("1px solid gray"),
        div(
          width("100%"),
          height("100%"),
          opacity <--
            $isActive.combineWith($hasPlayed).map {
              case (true, _) => 1
              case (_, true) => 0.5
              case _         => 0
            }.spring,
          background("white")
        ),
        marginRight("4px")
      )
    }
  )

  def header =
    div(
      position("sticky"),
      top("-1px"),
      zIndex(3),
      background("black"),
      div(
        progressView,
        div(
          boxStyles,
          borderBottom("1px solid #555"),
          fontSize("20px"),
          strong("ZLayers and You")
        ),
        cls("section"),
        cls.toggle("section-faded") <-- playingSection.signal.map(s => s.isDefined)
      )
    )

  val scrollTarget = Var(document.body.scrollHeight.toDouble)

  def render: Div = {
    div(
      RAFStream --> { _ =>
        scrollTarget.set(document.body.scrollHeight)
      },
      scrollTarget.signal --> { scrollY =>
        window.scrollTo(0, scrollY.toInt)
      },
      position.relative,
      paddingBottom("-1px"),
      keyDownEvents,
      header,
      div(
        overflowY.scroll,
        children <-- Transitions.transitionList($visibleSections)(identity) { (_, section, _, $status) =>
          val $isUnplayed = playedSections.signal.map(!_.contains(section))
          div(
            transform("translateZ(0.1px)"),
            div(
              section
                .mainRender(
                  sections.indexOf(section) + 1,
                  $isUnplayed = $isUnplayed,
                  $isPlaying = playingSection.signal.map(_.contains(section)),
                  handlePlay = time => handlePlay(section, time),
                  $step = sectionStepMap.signal.map(_(section)),
                  $debug = debugVar.signal
                ),
              overflowY.hidden,
              marginTop <-- $status.combineWith($isUnplayed)
                .map { case (s, unplayed) =>
                  if (!s.isActive) -1.0 else if (unplayed) -1.0 else 16.0
                }
                .spring
                .px,
              inContext { el =>
                val $height: Signal[String] = $status.map { s =>
                  if (!s.isActive) 0.0
                  else el.ref.scrollHeight.toDouble
                }.spring.px
                height <-- $isUnplayed.flatMap { b => if (!b) Val("auto") else $height }
              }
            ),
            cls("section"),
            cls.toggle("section-faded") <-- playingSection.signal.map(s => s.isDefined && !s.contains(section)),
            //            opacity <-- playingSection.signal.map {
            //              case Some(s) if s == section => 1.0
            //              case None                    => 1.0
            //              case _                       => 0.6
            //            }.spring,
            borderBottom("1px solid #555")
          )
        }
      )
    )
  }

}
