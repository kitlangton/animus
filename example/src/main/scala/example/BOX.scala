package example

sealed trait BOX[-R, +E, +A] { self =>
  def provide(env: R): BOX[Any, E, A] = BOX.Provide(env, self)

  def orElseSucceed[R1 <: R, A1 >: A](that: BOX[R1, Nothing, A1]): BOX[R1, Nothing, A1] =
    orElse(that)

  def orElse[R1 <: R, E1, A1 >: A](that: BOX[R1, E1, A1]): BOX[R1, E1, A1] =
    BOX.Fold(self, (_: E) => that, (a: A) => BOX.succeed(a))

  def fold[B](failure: E => B, success: A => B): BOX[R, Nothing, B] =
    BOX.Fold(
      self,
      (e: E) => BOX.succeed(failure(e)),
      (a: A) => BOX.succeed(success(a))
    )

  def *>[R1 <: R, E1 >: E, B](that: BOX[R1, E1, B]): BOX[R1, E1, B] = self.flatMap(_ => that)

  def map[B](f: A => B): BOX[R, E, B] = self.flatMap(f andThen BOX.succeed)

  def flatMap[R1 <: R, E1 >: E, B](f: A => BOX[R1, E1, B]): BOX[R1, E1, B] = BOX.FlatMap(self, f)

  def zip[R1 <: R, E1 >: E, B](that: BOX[R1, E1, B]): BOX[R1, E1, (A, B)] =
    self.flatMap(a => that.map(b => (a, b)))
}

object BOX {
  def environment[R]: BOX[R, Nothing, R] = BOX.Require[R]()

  def fail[E](error: E): BOX[Any, E, Nothing] = BOX.Fail(error)

  def succeed[A](value: A): BOX[Any, Nothing, A] = BOX.Succeed(value)

  def effect[A](effect: => A): BOX[Any, Throwable, A] = BOX.EffectTotal(() => effect)

  def effectAsync[E, A](callback: (BOX[Any, E, A] => Unit) => Unit): BOX[Any, E, A] =
    BOX.EffectAsync(callback)

  def apply[A](effect: => A): BOX[Any, Throwable, A] = BOX.EffectTotal(() => effect)

  case class FlatMap[R, E, A, B](box: BOX[R, E, A], f: A => BOX[R, E, B]) extends BOX[R, E, B]

  case class Succeed[A](value: A) extends BOX[Any, Nothing, A]

  case class EffectTotal[A](effect: () => A) extends BOX[Any, Nothing, A]

  case class EffectAsync[E, A](callback: (BOX[Any, E, A] => Unit) => Unit) extends BOX[Any, E, A]

  case class Fail[E](error: E) extends BOX[Any, E, Nothing]

  case class Require[R]() extends BOX[R, Nothing, R]

  case class Provide[R, E, A](provided: R, next: BOX[R, E, A]) extends BOX[Any, E, A]

  case class Fold[E, R, E2, A, B](box: BOX[R, E, A], failure: E => BOX[R, E2, B], success: A => BOX[R, E2, B])
      extends BOX[R, E2, B]
      with (A => BOX[R, E2, B]) {
    override def apply(value: A): BOX[R, E2, B] = success(value)
  }
}
