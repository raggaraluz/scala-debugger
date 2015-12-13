package org.senkbeil.debugger.api.lowlevel.classes

import org.senkbeil.debugger.api.lowlevel.RequestInfo
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument

/**
 * Represents information about a class unload request.
 *
 * @param requestId The id of the request
 * @param extraArguments The additional arguments provided to the
 *                       class unload request
 */
case class ClassUnloadRequestInfo(
  requestId: String,
  extraArguments: Seq[JDIRequestArgument] = Nil
) extends RequestInfo

