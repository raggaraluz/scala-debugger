package org.senkbeil.debugger.api.lowlevel.events

import org.scalamock.scalatest.MockFactory
import org.scalatest.{OneInstancePerTest, Matchers, FunSpec}
import org.senkbeil.debugger.api.lowlevel.events.EventManager.EventHandler
import org.senkbeil.debugger.api.lowlevel.events.EventType.EventType

class DummyEventManagerSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val TestHandlerId = java.util.UUID.randomUUID().toString
  private val eventManager = new DummyEventManager

  describe("DummyEventManager") {
    describe("#addEventHandlerWithId") {
      it("should return the provided id") {
        val expected = TestHandlerId

        val actual = eventManager.addEventHandlerWithId(
          expected,
          mock[EventType],
          mock[EventHandler]
        )

        actual should be (expected)
      }
    }

    describe("#getHandlersForEventType") {
      it("should return an empty list") {
        val expected = Nil

        val actual = eventManager.getHandlersForEventType(mock[EventType])

        actual should be (expected)
      }
    }

    describe("#getHandlerIdsForEventType") {
      it("should return an empty list") {
        val expected = Nil

        val actual = eventManager.getHandlerIdsForEventType(mock[EventType])

        actual should be (expected)
      }
    }

    describe("#getEventHandler") {
      it("should return None") {
        val expected = None

        val actual = eventManager.getEventHandler(TestHandlerId)

        actual should be (expected)
      }
    }

    describe("#removeEventHandler") {
      it("should return None") {
        val expected = None

        val actual = eventManager.removeEventHandler(TestHandlerId)

        actual should be (expected)
      }
    }

    describe("#start") {
      it("should do nothing") {
        eventManager.start()
      }
    }

    describe("#stop") {
      it("should do nothing") {
        eventManager.stop()
      }
    }
  }
}
