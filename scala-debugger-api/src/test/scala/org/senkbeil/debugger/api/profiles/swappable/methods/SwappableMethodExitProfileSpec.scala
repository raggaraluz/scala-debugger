package org.senkbeil.debugger.api.profiles.swappable.methods

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.profiles.ProfileManager
import org.senkbeil.debugger.api.profiles.swappable.SwappableDebugProfile
import org.senkbeil.debugger.api.profiles.traits.DebugProfile

class SwappableMethodExitProfileSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val mockDebugProfile = mock[DebugProfile]
  private val mockProfileManager = mock[ProfileManager]

  private val swappableDebugProfile = new Object with SwappableDebugProfile {
    override protected val profileManager: ProfileManager = mockProfileManager
  }

  describe("SwappableMethodExitProfile") {
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
  }
}
