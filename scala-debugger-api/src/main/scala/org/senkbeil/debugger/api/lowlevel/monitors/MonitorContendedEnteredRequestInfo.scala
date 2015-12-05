package org.senkbeil.debugger.api.lowlevel.monitors

import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument

/**
 * Represents information about a monitor contended entered request.
 *
 * @param extraArguments The additional arguments provided to the
 *                       monitor contended entered request
 */
case class MonitorContendedEnteredRequestInfo(
  extraArguments: Seq[JDIRequestArgument] = Nil
)

