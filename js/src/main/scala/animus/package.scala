import com.raquo.airstream.animus.SpringSignal
import com.raquo.airstream.core.{EventStream, Observable, Signal}

package object animus {

  def spring[A, Output]($value: Signal[A])(implicit animatable: Animatable[A]): Signal[A] = {
    val runner = new SpringRunner[A]()
    $value.flatMap(runner.animateTo(_))
  }

  implicit final class ObservableOps[A](private val self: Observable[A]) extends AnyVal {
    def percent: Observable[String] = self.map(x => s"${x}%")
  }

  implicit final class SignalOps[A](private val self: Signal[A]) extends AnyVal {
    def px: Signal[String]                                       = self.map(x => s"${x}px")
    def oldSpring(implicit animatable: Animatable[A]): Signal[A] = animus.spring(self)
    def spring(implicit animatable: Animatable[A]): Signal[A]    = new SpringSignal[A](self)
  }

  implicit final class EventStreamOps[A](private val self: EventStream[A]) extends AnyVal {
    def px: EventStream[String]                                              = self.map(x => s"${x}px")
    def spring(initial: => A)(implicit animatable: Animatable[A]): Signal[A] = self.startWith(initial).spring
  }

}
