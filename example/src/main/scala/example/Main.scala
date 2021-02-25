package example

import animus.SignalOps
import com.raquo.laminar.api.L._
import example.lessons.monoid.{FirstSection, SecondSection, ThirdSection}
import example.lessons.zlayer.{Section_1, Section_2, SomeSection}
import org.scalajs.dom.document
import org.scalajs.dom.html.Audio

import scala.scalajs.js.timers.setTimeout
import scala.util.Random

object Main {
  def main(args: Array[String]): Unit =
    documentEvents.onDomContentLoaded.foreach { _ =>
      val container = document.body
      render(container, body)
    }(unsafeWindowOwner)

  lazy val master = new MasterSection(
    Vector(
      Section_1,
      Section_2,
      SomeSection("Three"),
      SomeSection("Four"),
      SomeSection("Five"),
      SomeSection("Six"),
      SomeSection("Seven"),
      SomeSection("Eight"),
      SomeSection("Nine"),
      SomeSection("Ten"),
      SomeSection("Eleven"),
      SomeSection("Twelve"),
      SomeSection("Thirteen"),
      SomeSection("Fourteen"),
      SomeSection("Fifteen")
    )
  )

  def body: Div = {
    div(
      margin("20px"),
      div(
        margin("0px auto"),
        maxWidth("700px"),
        fontFamily("Source Code Pro"),
//        Section_1.debug
        master.render,
        paddingBottom("20px")
      )
    )
  }

  val boxStyles: Mod[HtmlElement] = Seq(
    border("1px solid #555"),
    padding("12px"),
    borderBottom("0px")
  )

  val boxStylesVert: Mod[HtmlElement] = Seq(
    borderTop("1px solid #555"),
    padding("12px")
  )

  def boxStyles($isHidden: Signal[Boolean]): Mod[HtmlElement] = Seq(
    border("1px solid #555"),
    padding <-- $isHidden.map { if (_) 0.0 else 12.0 }.spring.map { s => s"${s}px 12px" },
    borderBottom("0px")
  )

//  def codeBlock(codeString: String = "", language: String = "scala"): HtmlElement =
//    pre(
//      boxStyles,
//      background("rgb(20,20,27)"),
//      fontSize := "18px",
//      div(
//        padding := "10px",
//        overflowY.hidden,
//        borderRadius := "4px",
//        position.relative,
//        div(
//          code(
//            onMountCallback { el =>
//              val result = Highlight.highlight(language, codeString.trim).value
//              el.thisNode.ref.innerHTML = result
//            }
//          )
//        )
//      )
//    )

  def codeBlock(
      codeString: String = "",
      highlights: Signal[Set[Int]] = Val(Set.empty),
      language: String = "scala"
  ): HtmlElement =
    codeBlockSignal(Val(codeString), highlights, language)

  def codeBlockSignal(
      codeSignal: Signal[String],
      highlights: Signal[Set[Int]] = Val(Set.empty),
      language: String = "scala"
  ): HtmlElement =
    pre(
      boxStyles,
//      background("#0E160E"),
      background("rgb(20,20,27)"),
      fontSize := "16px",
      div(
        padding := "10px",
        paddingLeft := "0px",
        overflowY.hidden,
        borderRadius := "4px",
        position.relative,
        div(
          code(
            children <-- Transitions.transitionList(
              codeSignal
                .map { code =>
                  val result = Highlight.highlight(language, code.trim).value
                  result.split("\n").toList.zipWithIndex
                }
            )(identity) { (_, value, $value, $status) =>
              val (code, idx) = value
              val lineNumber  = idx + 1
              div(
                Transitions.transitionHeight($status.map(_.isActive)),
                display.flex,
                alignItems.baseline,
                background <-- highlights.map { h => if (h.contains(lineNumber)) 1.0 else 0.0 }.spring.map { d =>
                  s"rgba(38,38,53,$d)"
                },
                div(
                  (lineNumber).toString,
                  opacity(0.3),
                  position.relative,
                  fontSize("14px"),
                  marginRight("10px")
                ),
                div(
                  paddingTop("1px"),
                  paddingBottom("1px"),
                  div(
                    onMountCallback { el =>
                      el.thisNode.ref.innerHTML = code
                    }
                  )
                )
              )
            }
//            onMountCallback { el =>
//              val result = Highlight.highlight(language, codeString.trim).value
//              el.thisNode.ref.innerHTML = result
//            }
          )
        )
      )
    )

  def animateHeight(isVisible: Signal[Boolean], hiddenHeight: Double = 0.0) = {
    onMountBind { el: MountContext[HtmlElement] =>
      maxHeight <-- isVisible
        .map {
          if (_) el.thisNode.ref.scrollHeight.toDouble
          else hiddenHeight
        }
        .spring
        .px
    }
  }

  def textProgress(string: String, $progress: Signal[Int]): Div =
    div(
      position.relative,
      string
        .split("\n")
        .zipWithIndex
        .map { case (str, idx) =>
          div(
            display.inlineFlex,
            str + nbsp,
            opacity <-- $progress.map { i => if (idx < i) 1.0 else 0.3 }.spring
          )
        }
        .toList
    )

  def glowingOverlay($isUnplayed: Signal[Boolean]) =
    div {
      val isGlowing = Var(false)
      val $glow     = isGlowing.signal.map { if (_) 0.3 else 0.0 }
      Seq(
        $isUnplayed --> { isUnplayed =>
          if (!isUnplayed) {
            isGlowing.set(true)
            setTimeout(400) {
              isGlowing.set(false)
            }
          }
        },
        border <-- $glow.spring.map { g => s"6px solid rgba(200,200,200,$g)" },
        background <-- $glow.spring.map { g => s"rgba(150,150,150, $g)" },
        position.absolute,
        top("0"),
        left("0"),
        right("0"),
        bottom("0")
      )
    }
}

object Sounds {
  private val dink: Audio = useSound("slap")

  def useSound(name: String) = {
    val sound = audio(src(s"audio/${name}.mp3")).ref
    sound.preload = "auto"
    sound.load()
    sound
  }

  private val ping: Audio  = useSound("dink")
  private val ping2: Audio = useSound("dink2")
  private val ping3: Audio = useSound("dink3")
  private val ping4: Audio = useSound("dink4")
  private val ping5: Audio = useSound("dink5")
  private val ping6: Audio = useSound("dink6")
  val pings                = Vector(ping, ping2, ping3, ping4, ping5, ping6)

  def playDink(): Unit = {
    dink.currentTime = 0.0
    dink.play()
  }

  def playPing(): Unit = {
    val randomPing = pings(Random.nextInt(pings.length))
    randomPing.currentTime = 0.0
    randomPing.play()
  }
}

case class TimelineEntry(time: Double, step: Int)

case class Timeline(entries: Vector[TimelineEntry]) {
  def appended(time: Double, step: Int): Timeline =
    copy(entries = entries.appended(TimelineEntry(time, step)))

  def copyableString: String =
    s"Timeline.fromTimes(${entries.map(_.time).mkString(", ")})"
}

object Timeline {
  def empty: Timeline = Timeline(Vector.empty)
  def fromTimes(times: Double*): Timeline =
    Timeline(times.zipWithIndex.map { case (t, i) => TimelineEntry(t, i + 1) }.toVector)
}

//case class AudioSegment(audio: String, text: String, timeline: Timeline)
//
//object AudioSegment {
//  val stacking: AudioSegment = AudioSegment(
//    "stacking",
//    "These are like unlit dynamite. Stackable units of computation. Lorem ipsum dolor amet. Feliz navidad fortuna para amour a delirious child waltz.",
//    Timeline.example
//  )
//}
