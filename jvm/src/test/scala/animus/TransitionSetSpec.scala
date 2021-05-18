package animus

import animus.TransitionStatus.{Active, Inserting, Removing}
import zio.test.Assertion.equalTo
import zio.test.{DefaultRunnableSpec, assert}

object TransitionSetSpec extends DefaultRunnableSpec {
  override def spec = suite("TransitionSpec")(
    test("add values") {
      val set      = TransitionSet.empty[Int]
      val updated  = set.updated(1, 2, 3)
      val statuses = updated.statuses
      assert(statuses)(equalTo(Vector((1, Inserting), (2, Inserting), (3, Inserting))))
    },
    test("add more values") {
      val set      = TransitionSet.empty[Int]
      val updated  = set.updated(1, 2, 3).updated(1, 2, 3, 4)
      val statuses = updated.statuses
      assert(statuses)(equalTo(Vector((1, Active), (2, Active), (3, Active), (4, Inserting))))
    },
    test("remove then add back values") {
      val set      = TransitionSet.empty[Int]
      val updated  = set.updated(1, 2, 3).updated(1, 2, 4).updated(2, 3, 4)
      val statuses = updated.statuses
      assert(statuses)(equalTo(Vector((1, Removing), (2, Active), (3, Active), (4, Active))))
    }
  )
}
