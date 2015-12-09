package org.senkbeil.debugger.api.lowlevel.classes

import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument

/**
 * Represents information about a class unload request.
 *
 * @param extraArguments The additional arguments provided to the
 *                       class unload request
 */
case class ClassUnloadRequestInfo(
  extraArguments: Seq[JDIRequestArgument] = Nil
)

