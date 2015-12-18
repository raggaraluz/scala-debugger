package org.senkbeil.debugger.api.lowlevel.methods

import org.senkbeil.debugger.api.utils.PendingActionManager

/**
 * Provides pending method entry capabilities to an existing method entry
 * manager.
 *
 * Contains an internal pending action manager.
 */
trait StandardPendingMethodEntrySupport extends PendingMethodEntrySupport {
  override protected val pendingActionManager: PendingActionManager[MethodEntryRequestInfo] =
    new PendingActionManager[MethodEntryRequestInfo]
}
