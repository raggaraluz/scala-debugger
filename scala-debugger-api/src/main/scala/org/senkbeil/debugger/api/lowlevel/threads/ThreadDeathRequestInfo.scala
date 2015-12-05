package org.senkbeil.debugger.api.lowlevel.threads

import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument

/**
 * Represents information about a thread death request.
 *
 * @param extraArguments The additional arguments provided to the
 *                       thread death request
 */
case class ThreadDeathRequestInfo(
  extraArguments: Seq[JDIRequestArgument] = Nil
)

