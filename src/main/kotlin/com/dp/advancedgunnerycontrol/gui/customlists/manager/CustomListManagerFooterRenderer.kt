package com.dp.advancedgunnerycontrol.gui.customlists.manager

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.gui.session.CustomListModalMode
import com.dp.advancedgunnerycontrol.gui.style.CampaignActionButtonKind
import com.fs.starfarer.api.ui.CustomPanelAPI

internal object CustomListManagerFooterRenderer {
    fun render(
        dialog: CustomPanelAPI,
        dialogWidth: Float,
        dialogHeight: Float,
        currentTags: List<String>,
        currentShipModes: List<String>,
        state: CustomListModalStateController,
        buttons: MutableList<ButtonBase<*>>,
        callbacks: CustomListManagerModalCallbacks,
    ) {
        val stagedState = state.normalizeStagedState(currentTags)
        val shipModeStagedState = state.normalizeShipModeStagedState(currentShipModes)
        val hasPendingListChanges = stagedState.additions.isNotEmpty() ||
            stagedState.removals.isNotEmpty() ||
            stagedState.edits.isNotEmpty() ||
            shipModeStagedState.additions.isNotEmpty() ||
            shipModeStagedState.removals.isNotEmpty() ||
            shipModeStagedState.edits.isNotEmpty()

        CustomListModalFooterRenderer.addEdgeButtons(
            dialog = dialog,
            dialogWidth = dialogWidth,
            dialogHeight = dialogHeight,
            buttons = buttons,
            left = ModalFooterButtonSpec(
                data = "custom_tag_manager_confirm_changes",
                kind = CampaignActionButtonKind.CONFIRM,
                enabled = hasPendingListChanges,
                labelText = "Confirm",
                tooltip = if (!hasPendingListChanges) {
                    "Add or remove at least one custom tag or ship mode before confirming."
                } else {
                    "Review the pending custom list changes before applying them."
                },
                showTooltipWhileInactive = true,
            ) {
                callbacks.switchMode(CustomListModalMode.CONFIRM_TAG_CHANGES)
                callbacks.refresh()
            },
            right = ModalFooterButtonSpec(
                data = "custom_tag_add_cancel",
                kind = CampaignActionButtonKind.CANCEL,
                labelText = "Cancel",
            ) {
                callbacks.close()
            },
        )
    }
}
