package org.senkbeil.debugger.api.profiles.traits.steps

import com.sun.jdi.event.StepEvent
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult

import scala.concurrent._

class StepProfileSpec extends FunSpec with Matchers with OneInstancePerTest
  with MockFactory with ScalaFutures
{

  describe("StepProfile") {
    describe("#stepInLine") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[StepEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val promiseWithData = Promise[StepProfile#StepEventAndData]()
        val futureWithData = promiseWithData.future

        val stepProfile = new Object with StepProfile {
          override def stepInLineWithData(
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = {
            futureWithData
          }

          override def stepOverLineWithData(
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOutLineWithData(
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepInMinWithData(
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOutMinWithData(
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOverMinWithData(
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???
        }

        val actual = stepProfile.stepInLine()

        // Funnel the data through the future that is mapped by the wrapper
        // method
        promiseWithData.success(data)

        whenReady(actual) { a =>
          a should be(expected)
        }
      }
    }

    describe("#stepOverLine") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[StepEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val promiseWithData = Promise[StepProfile#StepEventAndData]()
        val futureWithData = promiseWithData.future

        val stepProfile = new Object with StepProfile {
          override def stepInLineWithData(
            extraArguments: JDIArgument*
            ): Future[StepEventAndData] = ???

          override def stepOverLineWithData(
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = {
            futureWithData
          }

          override def stepOutLineWithData(
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepInMinWithData(
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOutMinWithData(
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOverMinWithData(
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???
        }

        val actual = stepProfile.stepOverLine()

        // Funnel the data through the future that is mapped by the wrapper
        // method
        promiseWithData.success(data)

        whenReady(actual) { a =>
          a should be(expected)
        }
      }
    }

    describe("#stepOutLine") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[StepEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val promiseWithData = Promise[StepProfile#StepEventAndData]()
        val futureWithData = promiseWithData.future

        val stepProfile = new Object with StepProfile {
          override def stepInLineWithData(
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOverLineWithData(
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOutLineWithData(
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = {
            futureWithData
          }

          override def stepInMinWithData(
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOutMinWithData(
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOverMinWithData(
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???
        }

        val actual = stepProfile.stepOutLine()

        // Funnel the data through the future that is mapped by the wrapper
        // method
        promiseWithData.success(data)

        whenReady(actual) { a =>
          a should be (expected)
        }
      }
    }

    describe("#stepInMin") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[StepEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val promiseWithData = Promise[StepProfile#StepEventAndData]()
        val futureWithData = promiseWithData.future

        val stepProfile = new Object with StepProfile {
          override def stepInLineWithData(
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOverLineWithData(
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOutLineWithData(
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepInMinWithData(
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = {
            futureWithData
          }

          override def stepOutMinWithData(
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOverMinWithData(
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???
        }

        val actual = stepProfile.stepInMin()

        // Funnel the data through the future that is mapped by the wrapper
        // method
        promiseWithData.success(data)

        whenReady(actual) { a =>
          a should be(expected)
        }
      }
    }

    describe("#stepOverMin") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[StepEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val promiseWithData = Promise[StepProfile#StepEventAndData]()
        val futureWithData = promiseWithData.future

        val stepProfile = new Object with StepProfile {
          override def stepInLineWithData(
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOverLineWithData(
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOutLineWithData(
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepInMinWithData(
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOutMinWithData(
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOverMinWithData(
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = {
            futureWithData
          }
        }

        val actual = stepProfile.stepOverMin()

        // Funnel the data through the future that is mapped by the wrapper
        // method
        promiseWithData.success(data)

        whenReady(actual) { a =>
          a should be(expected)
        }
      }
    }

    describe("#stepOutMin") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[StepEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val promiseWithData = Promise[StepProfile#StepEventAndData]()
        val futureWithData = promiseWithData.future

        val stepProfile = new Object with StepProfile {
          override def stepInLineWithData(
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOverLineWithData(
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOutLineWithData(
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepInMinWithData(
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOutMinWithData(
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = {
            futureWithData
          }

          override def stepOverMinWithData(
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???
        }

        val actual = stepProfile.stepOutMin()

        // Funnel the data through the future that is mapped by the wrapper
        // method
        promiseWithData.success(data)

        whenReady(actual) { a =>
          a should be (expected)
        }
      }
    }
  }
}
