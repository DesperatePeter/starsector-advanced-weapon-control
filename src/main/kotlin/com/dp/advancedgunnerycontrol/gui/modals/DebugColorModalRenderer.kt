package com.dp.advancedgunnerycontrol.gui.modals

import com.dp.advancedgunnerycontrol.gui.controls.buttons.CampaignButtonControls
import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.*


import java.awt.Color
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI

/**
 * Debug Colors popup component.
 * Used from ShipView's Ctrl+Shift debug menu to preview, apply, restore, and
 * optionally persist AGC GUI color variables.
 */
internal object DebugColorModalRenderer {
    fun render(
        dialog: CustomPanelAPI,
        dialogWidth: Float,
        dialogHeight: Float,
        buttons: MutableList<ButtonBase<*>>,
        target: CampaignGuiStyle.DebugColorTarget,
        targetIndex: Int,
        targetCount: Int,
        draft: Color,
        persistent: Boolean,
        renderTitle: (CustomPanelAPI, Float, String) -> Unit,
        onCycleTarget: (Int) -> Unit,
        onDraftChanged: (Color) -> Unit,
        onPersistenceChanged: (Boolean) -> Unit,
        onApply: (CampaignGuiStyle.DebugColorTarget) -> Unit,
        onClose: () -> Unit,
    ) {
        renderTitle(dialog, dialogWidth, "Debug Colors")

        var y = CampaignGuiStyle.MODAL_PADDING +
            CampaignGuiStyle.MODAL_HEADING_HEIGHT +
            CampaignGuiStyle.MODAL_TITLE_BODY_GAP
        renderSampleRow(dialog, y, buttons, draft)
        y += CampaignGuiStyle.MODAL_ROW_HEIGHT + CampaignGuiStyle.MODAL_ROW_GAP

        val positionToken = "[${targetIndex + 1}/$targetCount]"
        CustomTagEditRowRenderer.addBidirectionalMomentaryRow(
            dialog = dialog,
            y = y,
            leftLabel = "Variable",
            buttonText = "${target.label} $positionToken",
            kind = CampaignActionButtonKind.UNCOLOURED,
            highlightTokens = listOf(positionToken),
            tooltip = "Cycle colour variables. Left-click for next; right-click for previous.",
            widthMultiplier = CustomListModalLayout.DEBUG_COLOR_MODAL_WIDTH_MULTIPLIER,
            buttons = buttons,
            onLeftClick = { onCycleTarget(1) },
            onRightClick = { onCycleTarget(-1) },
        )
        y += CampaignGuiStyle.MODAL_ROW_HEIGHT + CampaignGuiStyle.MODAL_ROW_GAP

        CustomTagEditRowRenderer.addMomentaryRow(
            dialog = dialog,
            y = y,
            leftLabel = "Mode",
            buttonText = if (persistent) "Permanent" else "Temporary",
            kind = if (persistent) CampaignActionButtonKind.CONFIRM else CampaignActionButtonKind.UNCOLOURED,
            buttons = buttons,
            tooltip = if (persistent) {
                "Apply or Confirm will apply this colour and save it to cross-campaign debug colour storage."
            } else {
                "Apply or Confirm will apply this colour until Starsector is restarted."
            },
            widthMultiplier = CustomListModalLayout.DEBUG_COLOR_MODAL_WIDTH_MULTIPLIER,
        ) {
            onPersistenceChanged(!persistent)
        }
        y += CampaignGuiStyle.MODAL_ROW_HEIGHT + CampaignGuiStyle.MODAL_ROW_GAP

        renderPreview(dialog, y, draft)
        y += CampaignGuiStyle.MODAL_ROW_HEIGHT + CampaignGuiStyle.MODAL_ROW_GAP

        renderIncrementor(dialog, y, "Red", draft.red, buttons) { delta ->
            onDraftChanged(Color((draft.red + delta).coerceIn(0, 255), draft.green, draft.blue, draft.alpha))
        }
        y += CampaignGuiStyle.MODAL_ROW_HEIGHT + CampaignGuiStyle.MODAL_ROW_GAP
        renderIncrementor(dialog, y, "Green", draft.green, buttons) { delta ->
            onDraftChanged(Color(draft.red, (draft.green + delta).coerceIn(0, 255), draft.blue, draft.alpha))
        }
        y += CampaignGuiStyle.MODAL_ROW_HEIGHT + CampaignGuiStyle.MODAL_ROW_GAP
        renderIncrementor(dialog, y, "Blue", draft.blue, buttons) { delta ->
            onDraftChanged(Color(draft.red, draft.green, (draft.blue + delta).coerceIn(0, 255), draft.alpha))
        }

        CustomListModalFooterRenderer.addEqualWidthButtons(
            dialog = dialog,
            dialogWidth = dialogWidth,
            dialogHeight = dialogHeight,
            buttons = buttons,
            specs = listOf(
                ModalFooterButtonSpec(
                    data = "debug_color_confirm",
                    kind = CampaignActionButtonKind.CONFIRM,
                    labelText = "Confirm",
                    tooltip = if (persistent) {
                        "Apply this colour override and keep it for future sessions."
                    } else {
                        "Apply this runtime colour override until the game is restarted."
                    },
                ) {
                    onApply(target)
                    onClose()
                },
                ModalFooterButtonSpec(
                    data = "debug_color_apply",
                    kind = CampaignActionButtonKind.SAVE,
                    labelText = "Apply",
                    tooltip = if (persistent) {
                        "Apply this colour override now and save it for future sessions without closing the debug menu."
                    } else {
                        "Apply this runtime colour override without closing the debug menu."
                    },
                ) {
                    onApply(target)
                },
                ModalFooterButtonSpec(
                    data = "debug_color_restore",
                    kind = CampaignActionButtonKind.LOAD,
                    labelText = "Restore",
                    tooltip = "Restore this draft to the compiled in-code colour. Use Apply or Confirm to apply it.",
                ) {
                    onDraftChanged(target.defaultColor)
                },
                ModalFooterButtonSpec(
                    data = "debug_color_cancel",
                    kind = CampaignActionButtonKind.CANCEL,
                    labelText = "Cancel",
                ) { onClose() }
            )
        )
    }

    private fun renderIncrementor(
        dialog: CustomPanelAPI,
        y: Float,
        channelLabel: String,
        currentValue: Int,
        buttons: MutableList<ButtonBase<*>>,
        onDelta: (Int) -> Unit,
    ) {
        CustomTagEditRowRenderer.renderDeltaButtons(
            dialog = dialog,
            y = y,
            leftLabel = "$channelLabel: $currentValue",
            dataPrefix = "debug_color:$channelLabel",
            tooltipLabel = channelLabel,
            buttons = buttons,
            widthMultiplier = CustomListModalLayout.DEBUG_COLOR_MODAL_WIDTH_MULTIPLIER,
            onDelta = onDelta,
        )
    }

    private fun renderSampleRow(
        dialog: CustomPanelAPI,
        y: Float,
        buttons: MutableList<ButtonBase<*>>,
        color: Color,
    ) {
        val widthMultiplier = CustomListModalLayout.DEBUG_COLOR_MODAL_WIDTH_MULTIPLIER
        val row = CustomTagEditRowRenderer.createRow(dialog, y, "Samples", widthMultiplier)
        val valueX = CustomTagEditRowRenderer.valueStartX(widthMultiplier)
        val valueWidth = CustomTagEditRowRenderer.componentValueWidth(widthMultiplier)
        val gap = CampaignGuiStyle.MODAL_ROW_GAP
        val sampleWidth = (valueWidth - 2f * gap) / 3f

        CustomTagEditRowRenderer.addColorRegion(
            row = row,
            x = valueX,
            width = sampleWidth,
            color = color,
            labelText = "Container",
            horizontalPadding = CampaignGuiStyle.ACTION_ROW_PADDING,
        )

        val button = addSampleCheckbox(
            row = row,
            data = "debug_color_button_sample",
            x = valueX + sampleWidth + gap,
            width = sampleWidth,
            color = color,
            labelText = "Button",
            checked = false,
            useToggleOverrides = false,
        )
        CampaignButtonControls.addControl(buttons, button = button) {}

        val toggle = addSampleCheckbox(
            row = row,
            data = "debug_color_toggle_sample",
            x = valueX + 2f * (sampleWidth + gap),
            width = sampleWidth,
            color = color,
            labelText = "Toggle",
            checked = true,
            useToggleOverrides = true,
        )
        CampaignButtonControls.addControl(buttons, button = toggle, active = true, stateful = true) {}
    }

    private fun addSampleCheckbox(
        row: CustomPanelAPI,
        data: Any,
        x: Float,
        width: Float,
        color: Color,
        labelText: String,
        checked: Boolean,
        useToggleOverrides: Boolean,
    ): ButtonAPI {
        return CustomTagEditRowRenderer.addButtonRegion(
            row = row,
            data = data,
            x = x,
            width = width,
            kind = CampaignActionButtonKind.UNCOLOURED,
            labelText = labelText,
            buttonColors = CampaignGuiStyle.sameColorButtonState(color),
            fillColor = color,
            checked = checked,
            visualOverrideColor = color.takeIf { useToggleOverrides },
        )
    }

    private fun renderPreview(dialog: CustomPanelAPI, y: Float, color: Color) {
        val widthMultiplier = CustomListModalLayout.DEBUG_COLOR_MODAL_WIDTH_MULTIPLIER
        val row = CustomTagEditRowRenderer.createRow(dialog, y, "Preview", widthMultiplier)
        val valueX = CustomTagEditRowRenderer.valueStartX(widthMultiplier)
        val valueWidth = CustomTagEditRowRenderer.componentValueWidth(widthMultiplier)
        CustomTagEditRowRenderer.addColorRegion(
            row = row,
            x = valueX,
            width = valueWidth,
            color = color,
            labelText = "Color(${color.red}, ${color.green}, ${color.blue})",
        )
    }
}
