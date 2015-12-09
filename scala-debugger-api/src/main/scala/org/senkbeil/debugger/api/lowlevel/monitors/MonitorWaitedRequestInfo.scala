package org.senkbeil.debugger.api.lowlevel.monitors

import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument

/**
 * Represents information about a monitor waited request.
 *
 * @param extraArguments The additional arguments provided to the
 *                       monitor waited request
 */
case class MonitorWaitedRequestInfo(
  extraArguments: Seq[JDIRequestArgument] = Nil
)

