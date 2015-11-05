package org.senkbeil.debugger.api.profiles.pure.vm

import com.sun.jdi.event.VMDeathEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.utils.JDIRequestResponseBuilder
import org.senkbeil.debugger.api.lowlevel.vm.VMDeathManager
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.traits.vm.VMDeathProfile

import scala.util.Try

/**
 * Represents a pure profile for vm death events that adds no
 * extra logic on top of the standard JDI.
 */
trait PureVMDeathProfile extends VMDeathProfile {
  protected val vmDeathManager: VMDeathManager
  protected val requestResponseBuilder: JDIRequestResponseBuilder

  /**
   * Constructs a stream of vm death events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of vm death events and any retrieved data based on
   *         requests from extra arguments
   */
  override def onVMDeathWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[VMDeathEventAndData]] = {
    /** Creates a new request using arguments. */
    def newRequest(args: Seq[JDIRequestArgument]): Unit = {
      // Ignore the id and propagate up any errors
      vmDeathManager.createVMDeathRequest(args: _*).get
    }

    requestResponseBuilder.buildRequestResponse[VMDeathEvent](
      newRequest,
      extraArguments: _*
    )
  }
}
