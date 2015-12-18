package org.senkbeil.debugger.api.lowlevel.monitors

import org.senkbeil.debugger.api.lowlevel.RequestInfo
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument

/**
 * Represents information about a monitor wait request.
 *
 * @param requestId The id of the request
 * @param extraArguments The additional arguments provided to the
 *                       monitor wait request
 */
case class MonitorWaitRequestInfo(
  requestId: String,
  extraArguments: Seq[JDIRequestArgument] = Nil
) extends RequestInfo

