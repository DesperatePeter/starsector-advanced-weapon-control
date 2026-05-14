package com.dp.advancedgunnerycontrol.gui.customlists.modal

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.gui.controls.rows.CampaignActionRows

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.*


import com.fs.starfarer.api.ui.CustomPanelAPI

/**
 * Modal footer button component.
 * Used by edit tag/mode, Manage Tags and Ship Modes, Custom List Changes,
 * Save/Load, debug-color, and rename-loadout popups.
 */
internal object CustomListModalFooterRenderer {
    fun addEqualWidthButtons(
        dialog: CustomPanelAPI,
        dialogWidth: Float,
        dialogHeight: Float,
        buttons: MutableList<ButtonBase<*>>,
        specs: List<ModalFooterButtonSpec>,
    ) {
        val buttonY = footerButtonY(dialogHeight)
        val buttonGap = CampaignGuiStyle.MODAL_ROW_GAP
        val buttonWidth = (
            dialogWidth -
                2f * CampaignGuiStyle.MODAL_PADDING -
                2f * CampaignGuiStyle.MODAL_FOOTER_HORIZONTAL_INSET -
                (specs.size - 1).toFloat() * buttonGap
            ) / specs.size.toFloat()
        val startX = CampaignGuiStyle.MODAL_PADDING + CampaignGuiStyle.MODAL_FOOTER_HORIZONTAL_INSET
        specs.forEachIndexed { index, spec ->
            addButton(
                dialog = dialog,
                buttons = buttons,
                spec = spec,
                x = startX + index * (buttonWidth + buttonGap),
                y = buttonY,
                width = buttonWidth,
            )
        }
    }

    fun addEdgeButtons(
        dialog: CustomPanelAPI,
        dialogWidth: Float,
        dialogHeight: Float,
        buttons: MutableList<ButtonBase<*>>,
        left: ModalFooterButtonSpec,
        right: ModalFooterButtonSpec,
    ) {
        val buttonY = footerButtonY(dialogHeight)
        addButton(
            dialog = dialog,
            buttons = buttons,
            spec = left,
            x = CampaignGuiStyle.MODAL_PADDING + CampaignGuiStyle.MODAL_FOOTER_HORIZONTAL_INSET,
            y = buttonY,
            width = CampaignGuiStyle.MODAL_BUTTON_WIDTH,
        )
        addButton(
            dialog = dialog,
            buttons = buttons,
            spec = right,
            x = dialogWidth -
                CampaignGuiStyle.MODAL_PADDING -
                CampaignGuiStyle.MODAL_FOOTER_HORIZONTAL_INSET -
                CampaignGuiStyle.MODAL_BUTTON_WIDTH,
            y = buttonY,
            width = CampaignGuiStyle.MODAL_BUTTON_WIDTH,
        )
    }

    private fun footerButtonY(dialogHeight: Float): Float {
        return dialogHeight - CampaignGuiStyle.MODAL_PADDING - CampaignGuiStyle.MODAL_ROW_HEIGHT
    }

    private fun addButton(
        dialog: CustomPanelAPI,
        buttons: MutableList<ButtonBase<*>>,
        spec: ModalFooterButtonSpec,
        x: Float,
        y: Float,
        width: Float,
    ) {
        buttons.add(CampaignActionRows.addTemplatedCampaignMomentaryActionButton(
            parent = dialog,
            data = spec.data,
            x = x,
            y = y,
            width = width,
            height = CampaignGuiStyle.MODAL_ROW_HEIGHT,
            template = CampaignGuiStyle.actionButtonTemplate(spec.kind, spec.enabled),
            labelText = spec.labelText,
            tooltip = spec.tooltip,
            showTooltipWhileInactive = spec.showTooltipWhileInactive,
            centerText = true,
        ) {
            spec.onClick()
        })
    }
}
