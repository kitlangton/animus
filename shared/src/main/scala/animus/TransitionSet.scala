package animus

import animus.TransitionStatus._

object TransitionSet {
  def empty[A]: TransitionSet[A, A] =
    TransitionSet.empty[A, A](identity)

  def empty[A, Key](getKey: A => Key): TransitionSet[A, Key] =
    TransitionSet[A, Key](getKey = getKey)
}

case class TransitionSet[A, Key](
  ordered: Vector[(Key, A)] = Vector.empty[(Key, A)],
  contains: Set[Key] = Set.empty[Key],
  inserting: Set[Key] = Set.empty[Key],
  removing: Set[Key] = Set.empty[Key],
  getKey: A => Key
) {

  def updated(values: A*)(implicit dummy: DummyImplicit): TransitionSet[A, Key] =
    updated(values)

  def updated(values: Seq[A]): TransitionSet[A, Key] = {
    val valuesWithKeys: Seq[(Key, A)]  = values.map(a => (getKey(a), a))
    val valuesWithKeysMap: Map[Key, A] = valuesWithKeys.toMap
    val newOrderedValues               = valuesWithKeys.filterNot(kv => contains(kv._1))

    val nextKeys    = valuesWithKeys.map(_._1).toSet
    val newKeys     = nextKeys -- contains
    val removedKeys = contains -- nextKeys

    val updatedOrdered: Vector[(Key, A)] =
      ordered.map { case (key, a) =>
        key -> valuesWithKeysMap.getOrElse(key, a)
      } ++ newOrderedValues

    copy(
      ordered = updatedOrdered,
      inserting = newKeys,
      contains = contains ++ newKeys,
      removing = removing ++ removedKeys -- nextKeys
    )
  }

  def remove(keys: Key*): TransitionSet[A, Key] = {
    val keySet = keys.toSet
    copy(
      ordered = ordered.filter(kv => keys.contains(kv._1)),
      contains = contains -- keySet,
      inserting = inserting -- keySet,
      removing = removing -- keySet
    )
  }

  def status(key: Key): TransitionStatus =
    if (inserting(key)) Inserting
    else if (removing(key)) Removing
    else Active

  def statuses: Vector[(A, TransitionStatus)] =
    ordered.map { case (key, a) => a -> status(key) }

}
