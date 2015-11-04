package org.senkbeil.debugger.api.profiles.pure.events

import com.sun.jdi.VirtualMachine
import com.sun.jdi.event.Event
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.events.{JDIEventArgument, EventManager}
import org.senkbeil.debugger.api.lowlevel.classes.ClassManager
import org.senkbeil.debugger.api.lowlevel.events.EventType.EventType
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.utils.JDIRequestResponseBuilder
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.utils.LoopingTaskRunner
import test.JDIMockHelpers

import scala.util.Success

class PureEventProfileSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory with JDIMockHelpers
{
  // NOTE: Cannot mock the actual class and test the function using
  //       ScalaMock, so have to override the method we want to test and
  //       inject a mock function instead
  private val mockAddEventDataStreamFunc = mockFunction[
    EventType,
    Seq[JDIEventArgument],
    IdentityPipeline[PureEventProfile#EventAndData]
  ]
  private val testEventManager = new EventManager(
    stub[VirtualMachine],
    stub[LoopingTaskRunner]
  ) {
    override def addEventDataStream(
      eventType: EventType,
      eventArguments: JDIEventArgument*
    ): IdentityPipeline[PureEventProfile#EventAndData] = {
      mockAddEventDataStreamFunc(eventType, eventArguments)
    }
  }

  private val pureEventProfile = new Object with PureEventProfile {
    override protected val eventManager = testEventManager
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

        mockAddEventDataStreamFunc.expects(eventType, eventArguments)
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
