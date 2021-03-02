import com.raquo.airstream.core.{EventStream, Observable, Signal}

package object animus {

  def spring[A, Output]($value: Signal[A])(implicit animatable: Animatable[A]): Signal[A] = {
    val runner = new SpringRunner[A]()
    $value.flatMap(runner.animateTo(_))
  }
  implicit final class ObservableOps[A](private val value: Observable[A]) extends AnyVal {
    def percent: Observable[String] = value.map(x => s"${x}%")
  }

  implicit final class SignalOps[A](private val value: Signal[A]) extends AnyVal {
    def px: Signal[String]                                    = value.map(x => s"${x}px")
    def spring(implicit animatable: Animatable[A]): Signal[A] = animus.spring(value)
  }

  implicit final class EventStreamOps[A](private val value: EventStream[A]) extends AnyVal {
    def px: EventStream[String]                                              = value.map(x => s"${x}px")
    def spring(initial: => A)(implicit animatable: Animatable[A]): Signal[A] = animus.spring(value.startWith(initial))
  }

}
