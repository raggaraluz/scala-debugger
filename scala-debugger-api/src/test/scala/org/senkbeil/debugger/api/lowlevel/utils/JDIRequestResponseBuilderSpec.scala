package org.senkbeil.debugger.api.lowlevel.utils

import com.sun.jdi.event.{EventQueue, Event, BreakpointEvent}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.events.{JDIEventArgument, EventManager}
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.utils.LoopingTaskRunner

import scala.reflect.ClassTag

class JDIRequestResponseBuilderSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val TestId = java.util.UUID.randomUUID().toString

  // Workaround - see https://github.com/paulbutcher/ScalaMock/issues/33
  private class ZeroArgEventManager extends EventManager(
    stub[EventQueue],
    stub[LoopingTaskRunner],
    autoStart = false
  )
  private val mockEventManager = mock[ZeroArgEventManager]

  /** Uses a mocked event manager and a specific test id for new requests. */
  private class TestJDIRequestResponseBuilder extends JDIRequestResponseBuilder(
    mockEventManager
  ) {
    /** Override to return our test id. */
    override protected def newRequestId(): String = TestId
  }

  private val jdiRequestResponseBuilder = new TestJDIRequestResponseBuilder

  // Helper method to get type erasures
  import scala.language.existentials
  def getErasure[A, B](
    pipeline: Pipeline[A, B]
  )(implicit aClassTag: ClassTag[A], bClassTag: ClassTag[B]) = {
    (aClassTag.runtimeClass, bClassTag.runtimeClass)
  }

  describe("JDIRequestResponseBuilder") {
    describe("#buildRequestResponse") {
      it("should provide a unique id property and any other request arguments to the request builder func") {
        val arguments = Seq(mock[JDIRequestArgument], mock[JDIRequestArgument])
        val expected = arguments :+ UniqueIdProperty(TestId)
        var actual: Seq[JDIRequestArgument] = Nil

        // Invoked at the end, but we don't care about the arguments
        (mockEventManager.addEventDataStream _).expects(*, *).returning(
          Pipeline.newPipeline(classOf[(Event, Seq[JDIEventDataResult])])
        )

        // NOTE: Must provide a valid event type (not just Event), otherwise
        //       an exception will be thrown
        jdiRequestResponseBuilder.buildRequestResponse[BreakpointEvent](
          (args) => { actual ++= args },
          arguments: _*
        )

        actual should contain theSameElementsAs (expected)
      }

      it("should provide a unique id filter and any other event arguments to the event manager") {
        val arguments = Seq(mock[JDIEventArgument], mock[JDIEventArgument])
        val expected = arguments :+ UniqueIdPropertyFilter(TestId)

        (mockEventManager.addEventDataStream _).expects(*, expected).returning(
          Pipeline.newPipeline(classOf[(Event, Seq[JDIEventDataResult])])
        )

        // NOTE: Must provide a valid event type (not just Event), otherwise
        //       an exception will be thrown
        jdiRequestResponseBuilder.buildRequestResponse[BreakpointEvent](
          (_) => {},
          arguments: _*
        )
      }

      it("should report a failure if an invalid event type is provided") {
        jdiRequestResponseBuilder.buildRequestResponse[Event](_ => {})
          .failed.get shouldBe an [AssertionError]
      }

      it("should report a failure if no event type is provided") {
        jdiRequestResponseBuilder.buildRequestResponse(_ => {})
          .failed.get shouldBe an [AssertionError]
      }

      it("should report a failure if the request function fails") {
        jdiRequestResponseBuilder.buildRequestResponse(_ => throw new Throwable)
          .failed.get shouldBe a [Throwable]
      }

      it("should map the stream of events to the type parameter provided") {
        // Invoked at the end, but we don't care about the arguments
        (mockEventManager.addEventDataStream _).expects(*, *).returning(
          Pipeline.newPipeline(classOf[(Event, Seq[JDIEventDataResult])])
        )

        val (inputClass, outputClass) = getErasure(
          jdiRequestResponseBuilder.buildRequestResponse[BreakpointEvent](
            (_) => {}
          ).get
        )

        inputClass should be (classOf[(BreakpointEvent, Seq[JDIEventDataResult])])
        outputClass should be (classOf[(BreakpointEvent, Seq[JDIEventDataResult])])
      }
    }
  }
}
