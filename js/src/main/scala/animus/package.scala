import com.raquo.airstream.core._

package object animus {
  implicit final class ObservableOps[A](private val self: Observable[A]) extends AnyVal {
    def percent: Observable[String] = self.map(x => s"${x}%")
  }

  implicit final class SignalOps[A](private val self: Signal[A]) extends AnyVal {
    def px: Signal[String]                                    = self.map(x => s"${x}px")
    def spring(implicit animatable: Animatable[A]): Signal[A] = new SpringSignal[A](self)

    def splitOneTransition[Key, Out](key: A => Key)(project: (Key, A, Signal[A], Transition) => Out): Signal[Seq[Out]] =
      Transitions.transitionList(self.map(List(_)))(key)(project)
  }

  implicit final class SignalSeqOps[A](private val self: Signal[Seq[A]]) extends AnyVal {
    def splitTransition[Key, Out](key: A => Key)(project: (Key, A, Signal[A], Transition) => Out): Signal[Seq[Out]] =
      new TransitioningSignal[A, Out, Key](self, key, project)
  }

  implicit final class EventStreamOps[A](private val self: EventStream[A]) extends AnyVal {
    def px: EventStream[String]                                              = self.map(x => s"${x}px")
    def spring(initial: => A)(implicit animatable: Animatable[A]): Signal[A] = self.startWith(initial).spring
  }
}
