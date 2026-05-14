package com.dp.advancedgunnerycontrol.gui.modals

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.foundation.*

import com.dp.advancedgunnerycontrol.gui.*


import com.fs.starfarer.api.ui.CustomPanelAPI

internal object LoadoutRenameModalRenderer {
    fun render(
        dialog: CustomPanelAPI,
        dialogWidth: Float,
        dialogHeight: Float,
        buttons: MutableList<ButtonBase<*>>,
        loadoutIndex: Int,
        loadoutCount: Int,
        draft: String,
        validation: String?,
        renderTitle: (CustomPanelAPI, Float, String) -> Unit,
        onConfirm: () -> Unit,
        onClose: () -> Unit,
    ) {
        renderTitle(dialog, dialogWidth, "Rename Loadout")

        var y = CampaignGuiStyle.MODAL_PADDING +
            CampaignGuiStyle.MODAL_HEADING_HEIGHT +
            CampaignGuiStyle.MODAL_TITLE_BODY_GAP
        CustomTagEditRowRenderer.renderDisplay(
            dialog = dialog,
            y = y,
            leftLabel = "Loadout",
            value = "${loadoutIndex + 1} / ${loadoutCount.coerceAtLeast(1)}",
        )
        y += CampaignGuiStyle.MODAL_ROW_HEIGHT + CampaignGuiStyle.MODAL_ROW_GAP
        CustomTagEditRowRenderer.renderDisplay(
            dialog = dialog,
            y = y,
            leftLabel = "Name",
            value = draft.ifBlank { "<empty>" },
            valueColor = if (validation == null) {
                CampaignGuiStyle.DEFAULT_TEXT_COLOUR
            } else {
                CampaignGuiStyle.DISABLED_TAG_TEXT_COLOR
            },
        )
        y += CampaignGuiStyle.MODAL_ROW_HEIGHT + CampaignGuiStyle.MODAL_ROW_GAP
        val hint = validation ?: "Alt+right-click the loadout button to rename this value again later."
        val hintPanel = dialog.createUIElement(
            dialogWidth - 2f * CampaignGuiStyle.MODAL_PADDING,
            CampaignGuiStyle.MODAL_ROW_HEIGHT,
            false,
        )
        hintPanel.addAgcText(
            hint,
            0f,
            if (validation == null) CampaignGuiStyle.DEFAULT_TEXT_COLOUR else CampaignGuiStyle.ALERT_RED_COLOR,
        )
        dialog.addUIElement(hintPanel).inTL(CampaignGuiStyle.MODAL_PADDING, y)

        CustomListModalFooterRenderer.addEqualWidthButtons(
            dialog = dialog,
            dialogWidth = dialogWidth,
            dialogHeight = dialogHeight,
            buttons = buttons,
            specs = listOf(
                ModalFooterButtonSpec(
                    data = "loadout_rename_confirm",
                    kind = CampaignActionButtonKind.CONFIRM,
                    enabled = validation == null,
                    labelText = "Confirm",
                    tooltip = validation ?: "Rename the current loadout.",
                    showTooltipWhileInactive = true,
                ) { onConfirm() },
                ModalFooterButtonSpec(
                    data = "loadout_rename_cancel",
                    kind = CampaignActionButtonKind.CANCEL,
                    labelText = "Cancel",
                    tooltip = "Close without renaming this loadout.",
                ) { onClose() },
            )
        )
    }
}
