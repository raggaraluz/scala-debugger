package org.senkbeil.debugger.api.lowlevel.watchpoints

import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument

/**
 * Represents information about a modification watchpoint request.
 *
 * @param className The full name of the class whose field to watch
 * @param fieldName The name of the field to watch
 * @param extraArguments The additional arguments provided to the
 *                       modification watchpoint request
 */
case class ModificationWatchpointRequestInfo(
  className: String,
  fieldName: String,
  extraArguments: Seq[JDIRequestArgument] = Nil
)

