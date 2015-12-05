package org.senkbeil.debugger.api.lowlevel.classes

import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument

/**
 * Represents information about a class prepare request.
 *
 * @param extraArguments The additional arguments provided to the
 *                       class prepare request
 */
case class ClassPrepareRequestInfo(
  extraArguments: Seq[JDIRequestArgument] = Nil
)

