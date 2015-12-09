package org.senkbeil.debugger.api.lowlevel.watchpoints

import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument

/**
 * Represents information about a access watchpoint request.
 *
 * @param className The full name of the class whose field to watch
 * @param fieldName The name of the field to watch
 * @param extraArguments The additional arguments provided to the
 *                       access watchpoint request
 */
case class AccessWatchpointRequestInfo(
  className: String,
  fieldName: String,
  extraArguments: Seq[JDIRequestArgument] = Nil
)

