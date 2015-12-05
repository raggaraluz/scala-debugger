package org.senkbeil.debugger.api.lowlevel.exceptions

import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument

/**
 * Represents information about a exception.
 *
 * @param className The full name of the exception class
 * @param notifyCaught Whether or not caught exceptions are reported
 * @param notifyUncaught Whether or not uncaught exceptions are reported
 * @param extraArguments The additional arguments provided to the exception
 */
case class ExceptionRequestInfo(
  className: String,
  notifyCaught: Boolean,
  notifyUncaught: Boolean,
  extraArguments: Seq[JDIRequestArgument] = Nil
) {
  /**
   * Indicates whether or not this exception request was to report all
   * exceptions or a specific exception.
   *
   * @return True if all exceptions are intended to be reported with this
   *         request, otherwise false
   */
  def isCatchall: Boolean = className == null
}

