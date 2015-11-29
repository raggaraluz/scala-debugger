package org.senkbeil.debugger.api.utils

import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

class MultiMapSpec extends FunSpec with Matchers with OneInstancePerTest {

  private val TestId = java.util.UUID.randomUUID().toString
  private val TestKey = (java.util.UUID.randomUUID().toString, 999)
  private val TestValue = java.util.UUID.randomUUID().toString

  private val multiMap = new MultiMap[(String, Int), String]

  describe("MultiMap") {
    describe("#put") {
      it("should store the value to be accessible via the key") {
        val expected = TestValue

        multiMap.put(TestKey, TestValue)

        val actual = multiMap.get(TestKey).get.head

        actual should be (expected)
      }

      it("should generate a new id to use for mapping the key to the value") {
        val expected = TestValue

        val id = multiMap.put(TestKey, TestValue)

        val actual = multiMap.getWithId(id).get

        actual should be (expected)
      }

      it("should add the value to the collection for an existing key") {
        val expected = Seq(TestValue, TestValue + 1, TestValue + 2)

        expected.foreach(multiMap.put(TestKey, _: String))

        val actual = multiMap.get(TestKey).get

        actual should be (expected)
      }
    }

    describe("#putWithId") {
      it("should store the value to be accessible via the key") {
        val expected = TestValue

        multiMap.putWithId(TestId, TestKey, TestValue)

        val actual = multiMap.get(TestKey).get.head

        actual should be (expected)
      }

      it("should use the id provided for mapping the key to the value") {
        val expected = TestValue

        multiMap.putWithId(TestId, TestKey, TestValue)

        val actual = multiMap.getWithId(TestId).get

        actual should be (expected)
      }

      it("should throw an exception if the id has already been used") {
        multiMap.putWithId(TestId, TestKey, TestValue)

        intercept[IllegalArgumentException] {
          multiMap.putWithId(TestId, TestKey, TestValue)
        }
      }

      it("should return the id provided") {
        val expected = TestId

        val actual = multiMap.putWithId(TestId, TestKey, TestValue)

        actual should be (expected)
      }

      it("should add the value to the collection for an existing key") {
        val expected = Seq(TestValue, TestValue + 1, TestValue + 2)

        expected.foreach(multiMap.putWithId(
          java.util.UUID.randomUUID().toString,
          TestKey,
          _: String
        ))

        val actual = multiMap.get(TestKey).get

        actual should be (expected)
      }
    }

    describe("#has") {
      it("should return true if the key exists") {
        val expected = true

        multiMap.put(TestKey, TestValue)

        val actual = multiMap.has(TestKey)

        actual should be (expected)
      }

      it("should return false if the key does not exist") {
        val expected = false

        val actual = multiMap.has(TestKey)

        actual should be (expected)
      }
    }

    describe("#hasWithId") {
      it("should return true if the id exists") {
        val expected = true

        multiMap.putWithId(TestId, TestKey, TestValue)

        val actual = multiMap.hasWithId(TestId)

        actual should be (expected)
      }

      it("should return false if the id does not exist") {
        val expected = false

        val actual = multiMap.hasWithId(TestId)

        actual should be (expected)
      }
    }

    describe("#get") {
      it("should return Some(collection of values) if the key exists") {
        val expected = Some(Seq(TestValue, TestValue + 1, TestValue + 2))

        expected.get.foreach(multiMap.put(TestKey, _: String))

        val actual = multiMap.get(TestKey)

        actual should be (expected)
      }

      it("should return None if the key does not exist") {
        val expected = None

        val actual = multiMap.get(TestKey)

        actual should be (expected)
      }
    }

    describe("#getWithId") {
      it("should return Some(value) if the id exists") {
        val expected = Some(TestValue)

        multiMap.putWithId(TestId, TestKey, expected.get)

        val actual = multiMap.getWithId(TestId)

        actual should be (expected)
      }

      it("should return None if the id does not exist") {
        val expected = None

        val actual = multiMap.getWithId(TestId)

        actual should be (expected)
      }
    }

    describe("#remove") {
      it("should remove the key if it exists along with all associated ids") {
        val ids = Seq(TestId, TestId + 1, TestId + 2)

        ids.foreach(multiMap.putWithId(_: String, TestKey, TestValue))

        multiMap.remove(TestKey)

        // Key should no longer exist
        multiMap.get(TestKey) should be (None)

        // None of the ids should exist
        ids.foreach { id =>
          multiMap.getWithId(id) should be (None)
        }
      }

      it("should return Some(collection of removed values) if the key exists") {
        val expected = Some(Seq(TestValue, TestValue + 1, TestValue + 2))

        expected.get.foreach(multiMap.put(TestKey, _: String))

        val actual = multiMap.remove(TestKey)

        actual should be (expected)
      }

      it("should return None if the key does not exist") {
        val expected = None

        val actual = multiMap.remove(TestKey)

        actual should be (expected)
      }
    }

    describe("#removeWithId") {
      it("should remove the id from the map") {
        multiMap.putWithId(TestId, TestKey, TestValue)

        multiMap.removeWithId(TestId)

        multiMap.getWithId(TestId) should be (None)
      }

      it("should remove the association between a key and the id") {
        multiMap.putWithId(TestId, TestKey, TestValue)
        multiMap.putWithId(TestId + 1, TestKey, TestValue)

        multiMap.removeWithId(TestId)

        multiMap.getIdsWithKey(TestKey).get should not contain (TestId)
      }

      it("should remove the key if the removed id was the only associated id") {
        multiMap.putWithId(TestId, TestKey, TestValue)

        multiMap.removeWithId(TestId)

        multiMap.getIdsWithKey(TestKey) should be (None)
      }

      it("should return Some(value) if the id exists") {
        val expected = Some(TestValue)

        multiMap.putWithId(TestId, TestKey, TestValue)

        val actual = multiMap.removeWithId(TestId)

        actual should be (expected)
      }

      it("should return None if the id does not exist") {
        val expected = None

        val actual = multiMap.removeWithId(TestId)

        actual should be (expected)
      }
    }

    describe("#getIdsWithKey") {
      it("should return Some(collection of ids) if the key exists") {
        val expected = Some(Seq(TestId, TestId + 1, TestId + 2))

        expected.get.foreach(multiMap.putWithId(_: String, TestKey, TestValue))

        val actual = multiMap.getIdsWithKey(TestKey)

        actual should be (expected)
      }

      it("should return None if the key does not exist") {
        val expected = None

        val actual = multiMap.getIdsWithKey(TestKey)

        actual should be (expected)
      }
    }

    describe("#getKeyWithId") {
      it("should return Some(key) if the id exists") {
        val expected = Some(TestKey)

        multiMap.putWithId(TestId, TestKey, TestValue)

        val actual = multiMap.getKeyWithId(TestId)

        actual should be (expected)
      }

      it("should return None if the id does not exist") {
        val expected = None

        val actual = multiMap.getKeyWithId(TestId)

        actual should be (expected)
      }
    }

    describe("#keys") {
      it("should return a collection of all keys in the map") {
        val expected = Seq(TestKey, TestKey.copy(_2 = 0), TestKey.copy(_2 = 1))

        expected.foreach(multiMap.put(_: (String, Int), TestValue))

        val actual = multiMap.keys

        actual should contain theSameElementsAs (expected)
      }

      it("should be empty if the map is empty") {
        multiMap.keys should be (empty)
      }
    }

    describe("#ids") {
      it("should return a collection of all ids in the map") {
        val expected = Seq(TestId, TestId + 1, TestId + 2)

        expected.foreach(multiMap.putWithId(_: String, TestKey, TestValue))

        val actual = multiMap.ids

        actual should contain theSameElementsAs (expected)
      }

      it("should be empty if the map is empty") {
        multiMap.ids should be (empty)
      }
    }

    describe("#values") {
      it("should return a collection of all values in the map") {
        val expected = Seq(TestValue, TestValue + 1, TestValue + 2)

        expected.foreach(multiMap.put(TestKey, _: String))

        val actual = multiMap.values

        actual should contain theSameElementsAs (expected)
      }

      it("should be empty if the map is empty") {
        multiMap.values should be (empty)
      }
    }
  }
}
