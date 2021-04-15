package example

import com.raquo.airstream.timing.PeriodicEventStream
import com.raquo.laminar.api.L._
import animus._

object AnimatedTitle extends Component {
  val activeVar = Var(true)

  val $time: Signal[Double] =
    new PeriodicEventStream[Double](
      initial = 0.0,
      next = eventNumber => Some((eventNumber + 1.0, ((1000 / 60).toDouble * Math.sin(eventNumber / 50)).toInt)),
      emitInitial = true,
      resetOnStop = true
    ).toSignal(0.0)

  def body: HtmlElement = h1(
    cursor.pointer,
    textAlign.center,
    onClick --> { _ => activeVar.update(!_) },
    onMouseOver --> { _ => activeVar.set(false) },
    onMouseOut --> { _ => activeVar.set(true) },
    "ANIMUS".zipWithIndex.map { case (char, idx) =>
      val $top     = $time.map { i => Math.sin((i + idx * 15) / 30.0) * 15.0 }
      val $opacity = $time.map { i => (Math.sin((i + idx * 15) / 30.0) / -2) + 1 }

      def activate(signal: Signal[Double], default: Double): Signal[Double] =
        activeVar.signal.flatMap { active =>
          if (active) signal
          else Val(default)
        }.spring

      div(
        cursor.pointer,
        display.inlineFlex,
        char.toString,
        position.relative,
        top <-- activate($top, 0).px,
        opacity <-- activate($opacity, 1)
      )
    }
  )
}
