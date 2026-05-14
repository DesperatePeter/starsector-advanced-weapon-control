package com.dp.advancedgunnerycontrol.gui.modals

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase

import com.dp.advancedgunnerycontrol.gui.*


import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.CustomPanelAPI

private object ShipViewModalLifecycleLog

internal fun detachShipViewModal(
    panel: CustomPanelAPI,
    shell: ShipViewModalShell,
    buttons: MutableList<ButtonBase<*>>,
    buttonStartIndex: Int,
    buttonEndIndex: Int,
) {
    try {
        runCatching { panel.removeComponent(shell.backdrop) }
            .onFailure { ex ->
                Global.getLogger(ShipViewModalLifecycleLog::class.java)
                    .warn("[AGC_MODAL] Failed to remove modal backdrop", ex)
            }
        runCatching { panel.removeComponent(shell.dialog) }
            .onFailure { ex ->
                Global.getLogger(ShipViewModalLifecycleLog::class.java)
                    .warn("[AGC_MODAL] Failed to remove modal dialog", ex)
            }
    } finally {
        buttons.clearShipViewModalButtonRange(buttonStartIndex, buttonEndIndex)
    }
}

private fun MutableList<ButtonBase<*>>.clearShipViewModalButtonRange(
    buttonStartIndex: Int,
    buttonEndIndex: Int,
) {
    val start = buttonStartIndex.coerceIn(0, size)
    val end = buttonEndIndex.coerceIn(start, size)
    subList(start, end).clear()
}
