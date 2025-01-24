package animus

object TransitionStatus:
  case object Inserting extends TransitionStatus
  case object Active    extends TransitionStatus
  case object Removing  extends TransitionStatus

sealed trait TransitionStatus:
  self =>
  def isActive: Boolean = self == TransitionStatus.Active
