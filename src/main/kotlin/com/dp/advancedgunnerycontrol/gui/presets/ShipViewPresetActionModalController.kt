package com.dp.advancedgunnerycontrol.gui.presets

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.gui.controls.scroll.VerticalScrollRegion

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.*

import com.dp.advancedgunnerycontrol.gui.modals.*

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI

private object ShipViewPresetActionModalControllerLog

internal class ShipViewPresetActionModalController(
    private val buttons: MutableList<ButtonBase<*>>,
    private val suppressNonModalButtonHover: (Int) -> Unit,
    private val restoreSuppressedNonModalButtonHover: () -> Unit,
    private val setConfirmationDialogPosition: (PositionAPI?) -> Unit,
    private val markDirty: () -> Unit,
) {
    var scopeWheelRegion: VerticalScrollRegion<Unit>? = null
        private set

    private var rootPanel: CustomPanelAPI? = null
    private var shell: ShipViewModalShell? = null
    private var buttonStartIndex: Int = -1
    private var buttonEndIndex: Int = -1

    fun hasOpenModal(): Boolean = shell != null

    fun render(
        panel: CustomPanelAPI,
        state: PresetControlState,
        request: CampaignConfirmationModalRequest,
        reviewBody: PresetActionReviewBody?,
        updateAndRefresh: (PresetControlState) -> Unit,
        canConfirm: () -> Boolean,
    ) {
        val firstModalButtonIndex = buttons.size
        suppressNonModalButtonHover(firstModalButtonIndex)
        val result = PresetActionModalRenderer.render(
            panel = panel,
            state = state,
            request = request,
            reviewBody = reviewBody,
            buttons = buttons,
            callbacks = PresetActionModalCallbacks(
                setScopeWheelRegion = ::setScopeWheelRegion,
                updateAndRefresh = updateAndRefresh,
                canConfirm = canConfirm,
            ),
        )
        setConfirmationDialogPosition(result.shell.dialog.position)
        rootPanel = panel
        shell = result.shell
        buttonStartIndex = firstModalButtonIndex
        buttonEndIndex = result.buttonEndIndex
    }

    fun refresh(
        state: PresetControlState,
        request: CampaignConfirmationModalRequest,
        reviewBody: PresetActionReviewBody?,
        updateAndRefresh: (PresetControlState) -> Unit,
        canConfirm: () -> Boolean,
    ) {
        val panel = rootPanel ?: run {
            markDirty()
            return
        }
        val currentShell = shell ?: run {
            markDirty()
            return
        }
        try {
            runCatching {
                detachShipViewModal(panel, currentShell, buttons, buttonStartIndex, buttonEndIndex)
            }.onFailure { ex ->
                Global.getLogger(ShipViewPresetActionModalControllerLog::class.java)
                    .warn("[AGC_PRESET_MODAL] Failed to detach preset modal during refresh", ex)
            }
        } finally {
            clearRefs()
        }
        render(
            panel = panel,
            state = state,
            request = request,
            reviewBody = reviewBody,
            updateAndRefresh = updateAndRefresh,
            canConfirm = canConfirm,
        )
    }

    fun closeTargeted() {
        val panel = rootPanel
        val currentShell = shell
        try {
            if (panel != null && currentShell != null) {
                runCatching {
                    detachShipViewModal(panel, currentShell, buttons, buttonStartIndex, buttonEndIndex)
                }.onFailure { ex ->
                    Global.getLogger(ShipViewPresetActionModalControllerLog::class.java)
                        .warn("[AGC_PRESET_MODAL] Failed to detach preset modal during close", ex)
                }
            } else {
                markDirty()
            }
        } finally {
            clearRefs()
            setConfirmationDialogPosition(null)
            restoreSuppressedNonModalButtonHover()
        }
    }

    fun reset() {
        clearRefs()
        setConfirmationDialogPosition(null)
    }

    private fun setScopeWheelRegion(rowTopFromDialogTop: Float) {
        val dialogHeight = shell?.dialog?.position?.height ?: run {
            clearScopeWheelRegion()
            return
        }
        val rowHeight = CampaignGuiStyle.MODAL_ROW_HEIGHT
        val rowBottom = dialogHeight - rowTopFromDialogTop - rowHeight
        scopeWheelRegion = VerticalScrollRegion(
            key = Unit,
            left = CampaignGuiStyle.MODAL_PADDING,
            right = CampaignGuiStyle.MODAL_PADDING + CustomTagEditRowRenderer.rowWidth(),
            bottom = rowBottom,
            top = rowBottom + rowHeight,
            maxOffset = 1,
        )
    }

    private fun clearScopeWheelRegion() {
        scopeWheelRegion = null
    }

    private fun clearRefs() {
        rootPanel = null
        shell = null
        buttonStartIndex = -1
        buttonEndIndex = -1
        clearScopeWheelRegion()
    }
}
