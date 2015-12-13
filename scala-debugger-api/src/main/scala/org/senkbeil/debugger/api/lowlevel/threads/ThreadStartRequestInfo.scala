package org.senkbeil.debugger.api.lowlevel.threads

import org.senkbeil.debugger.api.lowlevel.RequestInfo
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument

/**
 * Represents information about a thread start request.
 *
 * @param requestId The id of the request
 * @param extraArguments The additional arguments provided to the
 *                       thread start request
 */
case class ThreadStartRequestInfo(
  requestId: String,
  extraArguments: Seq[JDIRequestArgument] = Nil
) extends RequestInfo

