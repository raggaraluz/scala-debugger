package org.senkbeil.debugger.api.lowlevel.vm

import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument

/**
 * Represents information about a vm death request.
 *
 * @param extraArguments The additional arguments provided to the
 *                       vm death request
 */
case class VMDeathRequestInfo(
  extraArguments: Seq[JDIRequestArgument] = Nil
)

