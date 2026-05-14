package com.dp.advancedgunnerycontrol.gui.controls.confirmations

/**
 * Keeps confirm/cancel flows bound to an exact action key. This avoids the
 * modifier-key bug where a visible confirm row could drift into a different
 * action variant after Shift/Ctrl changed.
 */
class PendingConfirmationState<K : Any> {
    var key: K? = null
        private set

    fun arm(key: K) {
        this.key = key
    }

    fun clear() {
        key = null
    }

    fun clearIfUnavailable(validKeys: Iterable<K>) {
        val pending = key ?: return
        if (validKeys.none { it == pending }) clear()
    }

    fun isPending(candidate: K?): Boolean {
        return candidate != null && key == candidate
    }
}
