package org.senkbeil.debugger.api.profiles.swappable.watchpoints

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.profiles.ProfileManager
import org.senkbeil.debugger.api.profiles.swappable.SwappableDebugProfile
import org.senkbeil.debugger.api.profiles.traits.DebugProfile

class SwappableAccessWatchpointProfileSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val mockDebugProfile = mock[DebugProfile]
  private val mockProfileManager = mock[ProfileManager]

  private val swappableDebugProfile = new Object with SwappableDebugProfile {
    override protected val profileManager: ProfileManager = mockProfileManager
  }

  describe("SwappableAccessWatchpointProfile") {
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

  }
}
