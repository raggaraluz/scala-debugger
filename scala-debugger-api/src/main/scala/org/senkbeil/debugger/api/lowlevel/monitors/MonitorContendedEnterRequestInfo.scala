package org.senkbeil.debugger.api.lowlevel.monitors

import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument

/**
 * Represents information about a monitor contended enter request.
 *
 * @param extraArguments The additional arguments provided to the
 *                       monitor contended enter request
 */
case class MonitorContendedEnterRequestInfo(
  extraArguments: Seq[JDIRequestArgument] = Nil
)

