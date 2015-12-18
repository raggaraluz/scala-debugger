package org.senkbeil.debugger.api.profiles.pure.watchpoints

import com.sun.jdi.VirtualMachine
import com.sun.jdi.event.{Event, EventQueue}
import com.sun.jdi.request.EventRequestManager
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.lowlevel.classes.ClassManager
import org.senkbeil.debugger.api.lowlevel.events.EventManager
import org.senkbeil.debugger.api.lowlevel.events.EventType.AccessWatchpointEventType
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.senkbeil.debugger.api.lowlevel.watchpoints.{AccessWatchpointManager, StandardAccessWatchpointManager}
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.utils.LoopingTaskRunner
import test.JDIMockHelpers

import scala.util.{Failure, Success}

class PureAccessWatchpointProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val stubClassManager = stub[ClassManager]
  private val mockAccessWatchpointManager = mock[AccessWatchpointManager]
  private val mockEventManager = mock[EventManager]

  private val pureAccessWatchpointProfile = new Object with PureAccessWatchpointProfile {
    private var requestId: String = _
    def setRequestId(requestId: String): Unit = this.requestId = requestId

    // NOTE: If we set a specific request id, return that, otherwise use the
    //       default behavior
    override protected def newAccessWatchpointRequestId(): String =
      if (requestId != null) requestId else super.newAccessWatchpointRequestId()

    override protected val accessWatchpointManager = mockAccessWatchpointManager
    override protected val eventManager: EventManager = mockEventManager
  }

  describe("PureAccessWatchpointProfile") {
    describe("#onAccessWatchpointWithData") {
      it("should create a new request if one has not be made yet") {
        val className = "full.class.name"
        val fieldName = "fieldName"
        val arguments = Seq(mock[JDIRequestArgument])

        val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
        val uniqueIdPropertyFilter = UniqueIdPropertyFilter(id = TestRequestId)

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureAccessWatchpointProfile.setRequestId(TestRequestId)

        inSequence {
          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockAccessWatchpointManager.hasAccessWatchpointRequest _)
            .expects(className, fieldName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockAccessWatchpointManager.createAccessWatchpointRequestWithId _).expects(
            TestRequestId,
            className,
            fieldName,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(AccessWatchpointEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureAccessWatchpointProfile.onAccessWatchpointWithData(
          className,
          fieldName,
          arguments: _*
        )
      }

      it("should capture exceptions thrown when creating the request") {
        val expected = Failure(new Throwable)
        val className = "full.class.name"
        val fieldName = "fieldName"
        val arguments = Seq(mock[JDIRequestArgument])

        val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureAccessWatchpointProfile.setRequestId(TestRequestId)

        inSequence {
          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockAccessWatchpointManager.hasAccessWatchpointRequest _)
            .expects(className, fieldName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockAccessWatchpointManager.createAccessWatchpointRequestWithId _).expects(
            TestRequestId,
            className,
            fieldName,
            uniqueIdProperty +: arguments
          ).throwing(expected.failed.get).once()
        }

        val actual = pureAccessWatchpointProfile.onAccessWatchpointWithData(
          className,
          fieldName,
          arguments: _*
        )

        actual should be (expected)
      }

      it("should create a new request if the previous one was removed") {
        val className = "full.class.name"
        val fieldName = "fieldName"
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureAccessWatchpointProfile.setRequestId(TestRequestId)

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureAccessWatchpointProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockAccessWatchpointManager.hasAccessWatchpointRequest _)
            .expects(className, fieldName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockAccessWatchpointManager.createAccessWatchpointRequestWithId _).expects(
            TestRequestId,
            className,
            fieldName,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(AccessWatchpointEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureAccessWatchpointProfile.onAccessWatchpointWithData(
          className,
          fieldName,
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureAccessWatchpointProfile.setRequestId(TestRequestId + "other")

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId + "other")
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId + "other")

          // Return false this time to indicate that the accessWatchpoint request
          // was removed some time between the two calls
          (mockAccessWatchpointManager.hasAccessWatchpointRequest _)
            .expects(className, fieldName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockAccessWatchpointManager.createAccessWatchpointRequestWithId _).expects(
            TestRequestId + "other",
            className,
            fieldName,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(AccessWatchpointEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureAccessWatchpointProfile.onAccessWatchpointWithData(
          className,
          fieldName,
          arguments: _*
        )
      }

      it("should not create a new request if the previous one still exists") {
        val className = "full.class.name"
        val fieldName = "fieldName"
        val arguments = Seq(mock[JDIRequestArgument])

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureAccessWatchpointProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockAccessWatchpointManager.hasAccessWatchpointRequest _)
            .expects(className, fieldName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockAccessWatchpointManager.createAccessWatchpointRequestWithId _).expects(
            TestRequestId,
            className,
            fieldName,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(AccessWatchpointEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureAccessWatchpointProfile.onAccessWatchpointWithData(
          className,
          fieldName,
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureAccessWatchpointProfile.setRequestId(TestRequestId + "other")

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Return true to indicate that we do still have the request
          (mockAccessWatchpointManager.hasAccessWatchpointRequest _)
            .expects(className, fieldName)
            .returning(true).once()

          (mockEventManager.addEventDataStream _)
            .expects(AccessWatchpointEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureAccessWatchpointProfile.onAccessWatchpointWithData(
          className,
          fieldName,
          arguments: _*
        )
      }

      it("should create a new request for different input") {
        val className = "full.class.name"
        val fieldName = "fieldName"
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureAccessWatchpointProfile.setRequestId(TestRequestId)

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureAccessWatchpointProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockAccessWatchpointManager.hasAccessWatchpointRequest _)
            .expects(className, fieldName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockAccessWatchpointManager.createAccessWatchpointRequestWithId _).expects(
            TestRequestId,
            className,
            fieldName,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(AccessWatchpointEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureAccessWatchpointProfile.onAccessWatchpointWithData(
          className,
          fieldName,
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureAccessWatchpointProfile.setRequestId(TestRequestId + "other")

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId + "other")
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId + "other")

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockAccessWatchpointManager.hasAccessWatchpointRequest _)
            .expects(className, fieldName + 1)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockAccessWatchpointManager.createAccessWatchpointRequestWithId _).expects(
            TestRequestId + "other",
            className,
            fieldName + 1,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(AccessWatchpointEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureAccessWatchpointProfile.onAccessWatchpointWithData(
          className,
          fieldName + 1,
          arguments: _*
        )
      }

      it("should remove the underlying request if all pipelines are closed") {
        val className = "full.class.name"
        val fieldName = "fieldName"
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureAccessWatchpointProfile.setRequestId(TestRequestId)

        inSequence {
          inAnyOrder {
            val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
            val uniqueIdPropertyFilter =
              UniqueIdPropertyFilter(id = TestRequestId)

            // Memoized request function first checks to make sure the cache
            // has not been invalidated underneath (first call will always be
            // empty since we have never created the request)
            (mockAccessWatchpointManager.hasAccessWatchpointRequest _)
              .expects(className, fieldName)
              .returning(false).once()
            (mockAccessWatchpointManager.hasAccessWatchpointRequest _)
              .expects(className, fieldName)
              .returning(true).once()

            // NOTE: Expect the request to be created with a unique id
            (mockAccessWatchpointManager.createAccessWatchpointRequestWithId _).expects(
              TestRequestId,
              className,
              fieldName,
              uniqueIdProperty +: arguments
            ).returning(Success("")).once()

            (mockEventManager.addEventDataStream _)
              .expects(AccessWatchpointEventType, Seq(uniqueIdPropertyFilter))
              .returning(Pipeline.newPipeline(
                classOf[(Event, Seq[JDIEventDataResult])]
              )).twice()
          }

          (mockAccessWatchpointManager.removeAccessWatchpointRequestWithId _)
            .expects(TestRequestId).once()
        }

        val p1 = pureAccessWatchpointProfile.onAccessWatchpointWithData(
          className,
          fieldName,
          arguments: _*
        )
        val p2 = pureAccessWatchpointProfile.onAccessWatchpointWithData(
          className,
          fieldName,
          arguments: _*
        )

        p1.foreach(_.close())
        p2.foreach(_.close())
      }
    }
  }
}
