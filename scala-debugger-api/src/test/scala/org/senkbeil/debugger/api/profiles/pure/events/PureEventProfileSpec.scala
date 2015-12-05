package org.senkbeil.debugger.api.profiles.pure.events

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.events.EventType.EventType
import org.senkbeil.debugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.pipelines.Pipeline
import test.JDIMockHelpers

import scala.util.Success

class PureEventProfileSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory with JDIMockHelpers
{
  private val mockEventManager = mock[EventManager]
  private val pureEventProfile = new Object with PureEventProfile {
    override protected val eventManager = mockEventManager
  }

  describe("PureEventProfile") {
    describe("#onEventWithData") {
      it("should set a low-level event and stream its events") {
        val expected = Success(Pipeline.newPipeline(
          classOf[PureEventProfile#EventAndData]
        ))
        val eventType = stub[EventType] // Using mock throws stack overflow
        val requestArguments = Seq(mock[JDIRequestArgument])
        val eventArguments = Seq(mock[JDIEventArgument])
        val arguments = requestArguments ++ eventArguments

        (mockEventManager.addEventDataStream _)
          .expects(eventType, eventArguments)
          .returning(expected.get).once()

        val actual = pureEventProfile.onEventWithData(
          eventType,
          arguments: _*
        )

        actual should be (expected)
      }
    }
  }
}
