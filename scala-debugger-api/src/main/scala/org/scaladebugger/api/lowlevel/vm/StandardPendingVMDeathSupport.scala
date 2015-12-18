package org.senkbeil.debugger.api.lowlevel.vm

import org.senkbeil.debugger.api.utils.PendingActionManager

/**
 * Provides pending vm death capabilities to an existing vm death
 * manager.
 *
 * Contains an internal pending action manager.
 */
trait StandardPendingVMDeathSupport extends PendingVMDeathSupport {
  override protected val pendingActionManager: PendingActionManager[VMDeathRequestInfo] =
    new PendingActionManager[VMDeathRequestInfo]
}
