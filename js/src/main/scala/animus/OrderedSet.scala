package animus

import scala.collection.mutable.ListBuffer

class OrderedSet[A](var underlying: Vector[A]) {
  def addValues(as: Seq[A]): Unit = {
    val newKeys     = as.toSet
    val prevKeys    = underlying.toSet
    val removedKeys = prevKeys -- newKeys
    val addedKeys   = newKeys -- prevKeys

    val builder = ListBuffer.empty[A]

    val positions: Map[Int, A] = underlying.zipWithIndex.map(_.swap).toMap

    var removed: List[(Int, A)] = positions.filter(removedKeys contains _._2).toList.sortBy(_._1)
    var relativeCount           = 0

    var lastIdx = -9
    as.zipWithIndex.foreach { case (a, idx) =>
      val toAdd =
        removed.takeWhile { case (idx0, _) =>
          val result = idx0 <= relativeCount + idx || lastIdx + 1 == idx0
          if (result)
            lastIdx = idx0
          result
        }
          .map(_._2)

      if (addedKeys(a))
        relativeCount -= 1
      relativeCount += toAdd.length
      removed = removed.drop(toAdd.length)
      builder.addAll(toAdd)
      builder.addOne(a)
    }
    builder.addAll(removed.map(_._2))

    underlying = builder.toVector
  }

  def remove(value: A): Unit =
    underlying = underlying.filterNot(_ == value)

  def toList: List[A] = underlying.toList
}
