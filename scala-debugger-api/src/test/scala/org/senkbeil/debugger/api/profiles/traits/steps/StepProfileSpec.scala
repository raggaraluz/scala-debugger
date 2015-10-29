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
    describe("#stepIn") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[StepEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val promiseWithData = Promise[StepProfile#StepEventAndData]()
        val futureWithData = promiseWithData.future

        val stepProfile = new Object with StepProfile {
          override def stepInWithData(
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = {
            futureWithData
          }

          override def stepOverWithData(
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOutWithData(
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???
        }

        val actual = stepProfile.stepIn()

        // Funnel the data through the future that is mapped by the wrapper
        // method
        promiseWithData.success(data)

        whenReady(actual) { a =>
          a should be(expected)
        }
      }
    }

    describe("#stepOver") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[StepEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val promiseWithData = Promise[StepProfile#StepEventAndData]()
        val futureWithData = promiseWithData.future

        val stepProfile = new Object with StepProfile {
          override def stepInWithData(
            extraArguments: JDIArgument*
            ): Future[StepEventAndData] = ???

          override def stepOverWithData(
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = {
            futureWithData
          }

          override def stepOutWithData(
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???
        }

        val actual = stepProfile.stepOver()

        // Funnel the data through the future that is mapped by the wrapper
        // method
        promiseWithData.success(data)

        whenReady(actual) { a =>
          a should be(expected)
        }
      }
    }

    describe("#stepOut") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[StepEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val promiseWithData = Promise[StepProfile#StepEventAndData]()
        val futureWithData = promiseWithData.future

        val stepProfile = new Object with StepProfile {
          override def stepInWithData(
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOverWithData(
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOutWithData(
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = {
            futureWithData
          }
        }

        val actual = stepProfile.stepOut()

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
