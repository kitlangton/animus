package com.raquo.airstream.core

import animus.{OrderedSet, Transition, TransitionSet, TransitionStatus}
import com.raquo.airstream.common.SingleParentSignal
import com.raquo.airstream.state.Var

import scala.collection.mutable
import scala.scalajs.js.timers.{SetTimeoutHandle, clearTimeout, setTimeout}
import scala.util.Try

class TransitioningSignal[Input, Output, Key](
    override protected[this] val parent: Signal[Seq[Input]],
    getKey: Input => Key,
    project: (Key, Input, Signal[Input], Transition) => Output
) extends Signal[Seq[Output]]
    with WritableSignal[Seq[Output]]
    with SingleParentSignal[Seq[Input], Seq[Output]]:

  override protected def currentValueFromParent(): Try[Seq[Output]] =
    parent.tryNow().map(memoizedProject(_, true))

  override def onTry(nextParentValue: Try[Seq[Input]], transaction: Transaction): Unit =
    nextParentValue match
      case scala.util.Success(nextInputs) =>
        fireValue(memoizedProject(nextInputs), transaction)
      case scala.util.Failure(nextError) =>
        fireError(nextError, transaction)

  override protected[airstream] val topoRank: Int = Protected.topoRank(parent) + 1

  private[this] val timeoutHandles: mutable.Map[Key, SetTimeoutHandle]                      = mutable.Map.empty
  private[this] val memoized: mutable.Map[Key, (Output, Var[Input], Var[TransitionStatus])] = mutable.Map.empty
  private[this] val activeKeys: mutable.Set[Key]                                            = mutable.Set.empty

  // Used to track the order of the values.
  private[this] val ordered = new OrderedSet[Key](Vector.empty)

  protected override def onStop(): Unit =
    memoized.clear()
    super.onStop()

  def refireMemoized(): Unit = fireValue(ordered.toList.map(memoized(_)._1), null)

  private[this] def memoizedProject(nextInputs: Seq[Input], first: Boolean = false): Seq[Output] =
    val nextKeys = mutable.HashSet.empty[Key] // HashSet has desirable performance tradeoffs

    val nextOutputs = nextInputs.map { input =>
      val key = getKey(input)
      activeKeys.add(key)
      nextKeys.add(key)

      memoized.get(key) match
        case Some((output, inputVar, statusVar)) =>
          // If it was being removed, clear the timeout and make it active
          if statusVar.now() == TransitionStatus.Removing then
            clearTimeout(timeoutHandles(key))
            statusVar.set(TransitionStatus.Active)
          // Update the input if it has changed for this key
          if inputVar.now() != input then inputVar.set(input)
          key -> output

        case None =>
          val inputVar                        = Var(input)
          val initialStatus: TransitionStatus = if first then TransitionStatus.Active else TransitionStatus.Inserting
          val transitionStatusVar             = Var(initialStatus)
          val output                          = project(key, input, inputVar.signal, Transition(transitionStatusVar.signal))
          transitionStatusVar.set(TransitionStatus.Active)
          memoized(key) = (output, inputVar, transitionStatusVar)
          key -> output
    }

    ordered.addValues(nextOutputs.map(_._1))

    val removing = activeKeys.toSet -- nextKeys

    removing.foreach { key =>
      activeKeys.remove(key)
      memoized.get(key).map(_._3).foreach(_.set(TransitionStatus.Removing))
      val handle = setTimeout(950) {
        memoized.remove(key)
        ordered.remove(key)
        refireMemoized()
      }
      timeoutHandles(key) = handle
    }

    ordered.toList.map(memoized(_)._1)
  end memoizedProject
end TransitioningSignal
