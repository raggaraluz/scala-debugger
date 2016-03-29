package org.scaladebugger.api.profiles.swappable.exceptions
import acyclic.file

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfile
import org.scaladebugger.api.profiles.traits.DebugProfile

class SwappableExceptionProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  private val mockDebugProfile = mock[DebugProfile]
  private val mockProfileManager = mock[ProfileManager]

  private val swappableDebugProfile = new Object with SwappableDebugProfile {
    override protected val profileManager: ProfileManager = mockProfileManager
  }

  describe("SwappableExceptionProfile") {
    describe("#exceptionRequests") {
      it("should invoke the method on the underlying profile") {
        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.exceptionRequests _).expects().once()

        swappableDebugProfile.exceptionRequests
      }

      it("should throw an exception if there is no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.exceptionRequests
        }
      }
    }

    describe("#isExceptionRequestPending") {
      it("should invoke the method on the underlying profile") {
        val expected = true

        val exceptionName = "some exception"

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.isExceptionRequestPending _).expects(
          exceptionName
        ).returning(expected).once()

        val actual = swappableDebugProfile.isExceptionRequestPending(
          exceptionName
        )

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        val exceptionName = "some exception"

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.isExceptionRequestPending(exceptionName)
        }
      }
    }

    describe("#isExceptionRequestWithArgsPending") {
      it("should invoke the method on the underlying profile") {
        val expected = true

        val exceptionName = "some exception"
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.isExceptionRequestWithArgsPending _).expects(
          exceptionName, notifyCaught, notifyUncaught, arguments
        ).returning(expected).once()

        val actual = swappableDebugProfile.isExceptionRequestWithArgsPending(
          exceptionName, notifyCaught, notifyUncaught, arguments: _*
        )

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        val exceptionName = "some exception"
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.isExceptionRequestWithArgsPending(
            exceptionName, notifyCaught, notifyUncaught, arguments: _*
          )
        }
      }
    }

    describe("#isAllExceptionsRequestPending") {
      it("should invoke the method on the underlying profile") {
        val expected = true

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.isAllExceptionsRequestPending _).expects()
          .returning(expected).once()

        val actual = swappableDebugProfile.isAllExceptionsRequestPending

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.isAllExceptionsRequestPending
        }
      }
    }

    describe("#isAllExceptionsRequestWithArgsPending") {
      it("should invoke the method on the underlying profile") {
        val expected = true

        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.isAllExceptionsRequestWithArgsPending _).expects(
          notifyCaught, notifyUncaught, arguments
        ).returning(expected).once()

        val actual = swappableDebugProfile.isAllExceptionsRequestWithArgsPending(
          notifyCaught, notifyUncaught, arguments: _*
        )

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.isAllExceptionsRequestWithArgsPending(
            notifyCaught, notifyUncaught, arguments: _*
          )
        }
      }
    }

    describe("#tryGetOrCreateExceptionRequestWithData") {
      it("should invoke the method on the underlying profile") {
        val exceptionName = "some exception"
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.tryGetOrCreateExceptionRequestWithData _).expects(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          arguments
        ).once()

        swappableDebugProfile.tryGetOrCreateExceptionRequestWithData(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )
      }

      it("should throw an exception if there is no underlying profile") {
        val exceptionName = "some exception"
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.tryGetOrCreateExceptionRequestWithData(
            exceptionName,
            notifyCaught,
            notifyUncaught,
            arguments: _*
          )
        }
      }
    }

    describe("#tryGetOrCreateAllExceptionsRequestWithData") {
      it("should invoke the method on the underlying profile") {
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.tryGetOrCreateAllExceptionsRequestWithData _).expects(
          notifyCaught,
          notifyUncaught,
          arguments
        ).once()

        swappableDebugProfile.tryGetOrCreateAllExceptionsRequestWithData(
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )
      }

      it("should throw an exception if there is no underlying profile") {
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.tryGetOrCreateAllExceptionsRequestWithData(
            notifyCaught,
            notifyUncaught,
            arguments: _*
          )
        }
      }
    }
  }
}
