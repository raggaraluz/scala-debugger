package org.senkbeil.debugger.api.lowlevel.methods

import org.senkbeil.debugger.api.utils.PendingActionManager

/**
 * Provides pending method exit capabilities to an existing method exit
 * manager.
 *
 * Contains an internal pending action manager.
 */
trait StandardPendingMethodExitSupport extends PendingMethodExitSupport {
  override protected val pendingActionManager: PendingActionManager[MethodExitRequestInfo] =
    new PendingActionManager[MethodExitRequestInfo]
}
