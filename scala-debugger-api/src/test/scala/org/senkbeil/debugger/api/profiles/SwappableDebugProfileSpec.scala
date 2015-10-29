package org.senkbeil.debugger.api.profiles

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.EventType.EventType
import org.senkbeil.debugger.api.profiles.traits.DebugProfile

class SwappableDebugProfileSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val mockDebugProfile = mock[DebugProfile]
  private val mockProfileManager = mock[ProfileManager]

  private val swappableDebugProfile = new Object with SwappableDebugProfile {
    override protected val profileManager: ProfileManager = mockProfileManager
  }

  describe("SwappableDebugProfile") {
    describe("#use") {
      it("should set the current underlying profile") {
        val expected = mockDebugProfile
        val name = "some name"

        (mockProfileManager.retrieve _).expects(name)
          .returning(Some(expected)).once()

        swappableDebugProfile.use(name)

        val actual = swappableDebugProfile.withCurrentProfile

        actual should be (expected)
      }
    }

    describe("#withCurrentProfile") {
      it("should return the currently-active profile") {
        val expected = mockDebugProfile
        val name = "some name"

        (mockProfileManager.retrieve _).expects(name)
          .returning(Some(expected)).once()

        swappableDebugProfile.use(name)

        val actual = swappableDebugProfile.withCurrentProfile

        actual should be (expected)
      }

      it("should throw an exception if the profile is not found") {
        val name = "some name"

        (mockProfileManager.retrieve _).expects(name).returning(None).once()

        swappableDebugProfile.use(name)

        intercept[AssertionError] {
          swappableDebugProfile.withCurrentProfile
        }
      }
    }

    describe("#withProfile") {
      it("should return the profile with the specified name") {
        val expected = mockDebugProfile
        val name = "some name"

        (mockProfileManager.retrieve _).expects(name)
          .returning(Some(expected)).once()

        val actual = swappableDebugProfile.withProfile(name)

        actual should be (expected)
      }

      it("should throw an exception if the profile is not found") {
        val name = "some name"

        (mockProfileManager.retrieve _).expects(name).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.withProfile(name)
        }
      }
    }

    describe("#onThreadStartWithData") {
      it("should invoke the method on the underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.onThreadStartWithData _).expects(arguments).once()

        swappableDebugProfile.onThreadStartWithData(arguments: _*)
      }

      it("should throw an exception if there is no underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.onThreadStartWithData(arguments: _*)
        }
      }
    }

    describe("#stepInWithData") {
      it("should invoke the method on the underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.stepInWithData _).expects(arguments).once()

        swappableDebugProfile.stepInWithData(arguments: _*)
      }

      it("should throw an exception if there is no underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.stepInWithData(arguments: _*)
        }
      }
    }

    describe("#stepOverWithData") {
      it("should invoke the method on the underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.stepOverWithData _).expects(arguments).once()

        swappableDebugProfile.stepOverWithData(arguments: _*)
      }

      it("should throw an exception if there is no underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.stepOverWithData(arguments: _*)
        }
      }
    }

    describe("#stepOutWithData") {
      it("should invoke the method on the underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.stepOutWithData _).expects(arguments).once()

        swappableDebugProfile.stepOutWithData(arguments: _*)
      }

      it("should throw an exception if there is no underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.stepOutWithData(arguments: _*)
        }
      }
    }

    describe("#onClassUnloadWithData") {
      it("should invoke the method on the underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.onClassUnloadWithData _).expects(arguments).once()

        swappableDebugProfile.onClassUnloadWithData(arguments: _*)
      }

      it("should throw an exception if there is no underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.onClassUnloadWithData(arguments: _*)
        }
      }
    }

    describe("#onMonitorContendedEnteredWithData") {
      it("should invoke the method on the underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.onMonitorContendedEnteredWithData _)
          .expects(arguments).once()

        swappableDebugProfile.onMonitorContendedEnteredWithData(arguments: _*)
      }

      it("should throw an exception if there is no underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.onMonitorContendedEnteredWithData(arguments: _*)
        }
      }
    }

    describe("#onMonitorContendedEnterWithData") {
      it("should invoke the method on the underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.onMonitorContendedEnterWithData _)
          .expects(arguments).once()

        swappableDebugProfile.onMonitorContendedEnterWithData(arguments: _*)
      }

      it("should throw an exception if there is no underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.onMonitorContendedEnterWithData(arguments: _*)
        }
      }
    }

    describe("#onClassPrepareWithData") {
      it("should invoke the method on the underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.onClassPrepareWithData _).expects(arguments).once()

        swappableDebugProfile.onClassPrepareWithData(arguments: _*)
      }

      it("should throw an exception if there is no underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.onClassPrepareWithData(arguments: _*)
        }
      }
    }

    describe("#onEventWithData") {
      // TODO: ScalaMock is causing a stack overflow exception
      ignore("should invoke the method on the underlying profile") {
        val eventType = mock[EventType]
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.onEventWithData _)
          .expects(eventType, arguments).once()

        swappableDebugProfile.onEventWithData(eventType, arguments: _*)
      }

      it("should throw an exception if there is no underlying profile") {
        val eventType = mock[EventType]
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.onEventWithData(eventType, arguments: _*)
        }
      }
    }

    describe("#onMethodExitWithData") {
      it("should invoke the method on the underlying profile") {
        val className = "some class"
        val methodName = "some method"
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.onMethodExitWithData _)
          .expects(className, methodName, arguments).once()

        swappableDebugProfile.onMethodExitWithData(
          className,
          methodName,
          arguments: _*
        )
      }

      it("should throw an exception if there is no underlying profile") {
        val className = "some class"
        val methodName = "some method"
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.onMethodExitWithData(
            className,
            methodName,
            arguments: _*
          )
        }
      }
    }

    describe("#onThreadDeathWithData") {
      it("should invoke the method on the underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.onThreadDeathWithData _).expects(arguments).once()

        swappableDebugProfile.onThreadDeathWithData(arguments: _*)
      }

      it("should throw an exception if there is no underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.onThreadDeathWithData(arguments: _*)
        }
      }
    }

    describe("#onVMDeathWithData") {
      it("should invoke the method on the underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.onVMDeathWithData _).expects(arguments).once()

        swappableDebugProfile.onVMDeathWithData(arguments: _*)
      }

      it("should throw an exception if there is no underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.onVMDeathWithData(arguments: _*)
        }
      }
    }

    describe("#onMethodEntryWithData") {
      it("should invoke the method on the underlying profile") {
        val className = "some class"
        val methodName = "some method"
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.onMethodEntryWithData _)
          .expects(className, methodName, arguments).once()

        swappableDebugProfile.onMethodEntryWithData(
          className,
          methodName,
          arguments: _*
        )
      }

      it("should throw an exception if there is no underlying profile") {
        val className = "some class"
        val methodName = "some method"
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.onMethodEntryWithData(
            className,
            methodName,
            arguments: _*
          )
        }
      }
    }

    describe("#onExceptionWithData") {
      it("should invoke the method on the underlying profile") {
        val exceptionName = "some exception"
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.onExceptionWithData _).expects(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          arguments
        ).once()

        swappableDebugProfile.onExceptionWithData(
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
          swappableDebugProfile.onExceptionWithData(
            exceptionName,
            notifyCaught,
            notifyUncaught,
            arguments: _*
          )
        }
      }
    }

    describe("#onAllExceptionsWithData") {
      it("should invoke the method on the underlying profile") {
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.onAllExceptionsWithData _).expects(
          notifyCaught,
          notifyUncaught,
          arguments
        ).once()

        swappableDebugProfile.onAllExceptionsWithData(
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
          swappableDebugProfile.onAllExceptionsWithData(
            notifyCaught,
            notifyUncaught,
            arguments: _*
          )
        }
      }
    }

    describe("#onMonitorWaitedWithData") {
      it("should invoke the method on the underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.onMonitorWaitedWithData _).expects(arguments).once()

        swappableDebugProfile.onMonitorWaitedWithData(arguments: _*)
      }

      it("should throw an exception if there is no underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.onMonitorWaitedWithData(arguments: _*)
        }
      }
    }

    describe("#onMonitorWaitWithData") {
      it("should invoke the method on the underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.onMonitorWaitWithData _).expects(arguments).once()

        swappableDebugProfile.onMonitorWaitWithData(arguments: _*)
      }

      it("should throw an exception if there is no underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.onMonitorWaitWithData(arguments: _*)
        }
      }
    }

    describe("#onAccessFieldWatchpointWithData") {
      it("should invoke the method on the underlying profile") {
        val className = "some class"
        val fieldName = "some field"
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        // NOTE: Forced to use onCall with product due to issues with ScalaMock
        //       casting and inability to work with varargs directly
        (mockDebugProfile.onAccessFieldWatchpointWithData(
          _: String,
          _: String,
          _: JDIArgument)
        ).expects(className, fieldName, *).onCall(t => {
          val args = t.productElement(2).asInstanceOf[Seq[JDIArgument]]
          args should be (arguments)
          null
        })

        swappableDebugProfile.onAccessFieldWatchpointWithData(
          className,
          fieldName,
          arguments: _*
        )
      }

      it("should throw an exception if there is no underlying profile") {
        val className = "some class"
        val fieldName = "some field"
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.onAccessFieldWatchpointWithData(
            className,
            fieldName,
            arguments: _*
          )
        }
      }
    }

    describe("#onAccessInstanceWatchpointWithData") {
      it("should invoke the method on the underlying profile") {
        val instanceName = "some instance"
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        // NOTE: Forced to use onCall with product due to issues with ScalaMock
        //       casting and inability to work with varargs directly
        (mockDebugProfile.onAccessInstanceWatchpointWithData(
          _: String,
          _: JDIArgument)
        ).expects(instanceName, *).onCall(t => {
          val args = t.productElement(1).asInstanceOf[Seq[JDIArgument]]
          args should be (arguments)
          null
        })

        swappableDebugProfile.onAccessInstanceWatchpointWithData(
          instanceName,
          arguments: _*
        )
      }

      it("should throw an exception if there is no underlying profile") {
        val instanceName = "some instance"
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.onAccessInstanceWatchpointWithData(
            instanceName,
            arguments: _*
          )
        }
      }
    }

    describe("#onModificationFieldWatchpointWithData") {
      it("should invoke the method on the underlying profile") {
        val className = "some class"
        val fieldName = "some field"
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        // NOTE: Forced to use onCall with product due to issues with ScalaMock
        //       casting and inability to work with varargs directly
        (mockDebugProfile.onModificationFieldWatchpointWithData(
          _: String,
          _: String,
          _: JDIArgument)
        ).expects(className, fieldName, *).onCall(t => {
          val args = t.productElement(2).asInstanceOf[Seq[JDIArgument]]
          args should be (arguments)
          null
        })

        swappableDebugProfile.onModificationFieldWatchpointWithData(
          className,
          fieldName,
          arguments: _*
        )
      }

      it("should throw an exception if there is no underlying profile") {
        val className = "some class"
        val fieldName = "some field"
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.onModificationFieldWatchpointWithData(
            className,
            fieldName,
            arguments: _*
          )
        }
      }
    }

    describe("#onModificationInstanceWatchpointWithData") {
      it("should invoke the method on the underlying profile") {
        val instanceName = "some instance"
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        // NOTE: Forced to use onCall with product due to issues with ScalaMock
        //       casting and inability to work with varargs directly
        (mockDebugProfile.onModificationInstanceWatchpointWithData(
          _: String,
          _: JDIArgument)
        ).expects(instanceName, *).onCall(t => {
          val args = t.productElement(1).asInstanceOf[Seq[JDIArgument]]
          args should be (arguments)
          null
        })

        swappableDebugProfile.onModificationInstanceWatchpointWithData(
          instanceName,
          arguments: _*
        )
      }

      it("should throw an exception if there is no underlying profile") {
        val instanceName = "some instance"
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.onModificationInstanceWatchpointWithData(
            instanceName,
            arguments: _*
          )
        }
      }
    }
  }
}
