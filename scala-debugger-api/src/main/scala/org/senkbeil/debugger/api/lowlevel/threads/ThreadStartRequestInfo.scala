package org.senkbeil.debugger.api.lowlevel.threads

import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument

/**
 * Represents information about a thread start request.
 *
 * @param extraArguments The additional arguments provided to the
 *                       thread start request
 */
case class ThreadStartRequestInfo(
  extraArguments: Seq[JDIRequestArgument] = Nil
)

