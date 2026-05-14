package com.dp.advancedgunnerycontrol.gui.customlists.edit

import com.dp.advancedgunnerycontrol.gui.controls.buttons.CampaignButtonControls
import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.gui.controls.rows.CampaignActionRows
import com.dp.advancedgunnerycontrol.gui.controls.suppression.CampaignButtonSuppression
import com.dp.advancedgunnerycontrol.gui.controls.text.CampaignControlLabels

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


import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipLocation
import java.awt.Color

/**
 * Label/value row component used by edit tag, edit ship mode, Save/Load, and
 * debug-color modals. The left side is the label, the right side is a value,
 * button, preview, or increment controls.
 */
internal object CustomTagEditRowRenderer {
    fun labelWidth(widthMultiplier: Float = 1f): Float {
        return CampaignGuiStyle.EDIT_TAG_LABEL_WIDTH * widthMultiplier
    }

    fun valueGap(widthMultiplier: Float = 1f): Float {
        return CampaignGuiStyle.EDIT_TAG_VALUE_GAP * widthMultiplier
    }

    fun valueWidth(widthMultiplier: Float = 1f): Float {
        return CampaignGuiStyle.EDIT_TAG_VALUE_WIDTH * widthMultiplier
    }

    fun componentLabelWidth(widthMultiplier: Float = 1f): Float {
        return labelWidth(widthMultiplier) - CampaignGuiStyle.MODAL_COMPONENT_HORIZONTAL_INSET
    }

    fun componentValueWidth(widthMultiplier: Float = 1f): Float {
        return valueWidth(widthMultiplier) - CampaignGuiStyle.MODAL_COMPONENT_HORIZONTAL_INSET
    }

    fun valueStartX(widthMultiplier: Float = 1f): Float {
        return componentLabelWidth(widthMultiplier) + valueGap(widthMultiplier)
    }

    fun rowWidth(widthMultiplier: Float = 1f): Float {
        return labelWidth(widthMultiplier) + valueGap(widthMultiplier) + valueWidth(widthMultiplier)
    }

    fun widthMultiplierForDialogWidth(dialogWidth: Float): Float {
        val availableContentWidth = dialogWidth - 2f * CampaignGuiStyle.MODAL_PADDING
        return (availableContentWidth / rowWidth()).coerceIn(0.5f, 1f)
    }

    private fun componentRowWidth(widthMultiplier: Float = 1f): Float {
        return componentLabelWidth(widthMultiplier) + valueGap(widthMultiplier) + componentValueWidth(widthMultiplier)
    }

    fun createRow(
        dialog: CustomPanelAPI,
        y: Float,
        label: String,
        widthMultiplier: Float = 1f,
        textColor: Color? = null,
        tooltip: String? = null,
    ): CustomPanelAPI {
        val labelWidth = componentLabelWidth(widthMultiplier)
        val valueGap = valueGap(widthMultiplier)
        val row = dialog.createCustomPanel(
            componentRowWidth(widthMultiplier),
            CampaignGuiStyle.MODAL_ROW_HEIGHT,
            CampaignPanelPlugin(
                CampaignPanelType.CONTROL_PANEL,
                fillColor = CampaignGuiStyle.MODAL_COMPONENT_ROW_FILL_COLOR,
                borderColor = CampaignGuiStyle.MODAL_COMPONENT_BORDER_COLOR,
            )
        )
        dialog.addComponent(row)
        row.position.inTL(CampaignGuiStyle.MODAL_PADDING + CampaignGuiStyle.MODAL_COMPONENT_HORIZONTAL_INSET, y)
        addRowTooltip(row, tooltip)
        CampaignControlLabels.renderTagLabel(
            panel = row,
            text = fittedText(label, labelWidth),
            width = labelWidth - 2f * CampaignGuiStyle.ITEM_TEXT_HORIZONTAL_PADDING,
            height = CampaignGuiStyle.MODAL_ROW_HEIGHT - CampaignGuiStyle.ITEM_TEXT_TOP_PADDING,
            x = CampaignGuiStyle.ITEM_TEXT_HORIZONTAL_PADDING,
            y = CampaignGuiStyle.ITEM_TEXT_TOP_PADDING,
            textColor = textColor,
        )
        return row
    }

    fun renderDisplay(
        dialog: CustomPanelAPI,
        y: Float,
        leftLabel: String,
        value: String,
        valueColor: Color? = null,
        widthMultiplier: Float = 1f,
    ) {
        val row = createRow(dialog, y, leftLabel, widthMultiplier)
        val valueX = valueStartX(widthMultiplier)
        val valueWidth = componentValueWidth(widthMultiplier)
        val valueRegion = addNeutralValueRegion(
            row = row,
            x = valueX,
            width = valueWidth,
        )
        CampaignControlLabels.renderTagLabel(
            panel = valueRegion,
            text = fittedText(value, valueWidth),
            width = valueWidth - 2f * CampaignGuiStyle.ITEM_TEXT_HORIZONTAL_PADDING,
            height = CampaignGuiStyle.MODAL_ROW_HEIGHT - CampaignGuiStyle.ITEM_TEXT_TOP_PADDING,
            x = CampaignGuiStyle.ITEM_TEXT_HORIZONTAL_PADDING,
            y = CampaignGuiStyle.ITEM_TEXT_TOP_PADDING,
            textColor = valueColor,
            alignment = Alignment.MID,
        )
    }

    fun renderButton(
        dialog: CustomPanelAPI,
        y: Float,
        leftLabel: String,
        buttonText: String,
        kind: CampaignActionButtonKind,
        highlightTokens: List<String> = emptyList(),
        tooltip: String? = null,
        widthMultiplier: Float = 1f,
        textColor: Color? = null,
        enabled: Boolean = true,
        disabledKind: CampaignActionButtonKind = CampaignActionButtonKind.DISABLED,
    ): ButtonAPI {
        val template = CampaignGuiStyle.actionButtonTemplate(kind, enabled, disabledKind)
        val effectiveTextColor = textColor ?: template.textColor
        val row = createRow(dialog, y, leftLabel, widthMultiplier, effectiveTextColor, tooltip)
        val valueX = valueStartX(widthMultiplier)
        val valueWidth = componentValueWidth(widthMultiplier)
        return addButtonRegion(
            row = row,
            data = "custom_tag_component:${leftLabel}:${buttonText}",
            x = valueX,
            width = valueWidth,
            kind = template.kind,
            labelText = fittedText(buttonText, valueWidth),
            highlightTokens = highlightTokens,
            tooltip = tooltip,
            textColor = effectiveTextColor,
        )
    }

    fun addMomentaryRow(
        dialog: CustomPanelAPI,
        y: Float,
        leftLabel: String,
        buttonText: String,
        kind: CampaignActionButtonKind,
        buttons: MutableList<ButtonBase<*>>,
        tooltip: String? = null,
        widthMultiplier: Float = 1f,
        textColor: Color? = null,
        enabled: Boolean = true,
        disabledKind: CampaignActionButtonKind = CampaignActionButtonKind.DISABLED,
        onClick: () -> Unit,
    ) {
        val button = renderButton(
            dialog = dialog,
            y = y,
            leftLabel = leftLabel,
            buttonText = buttonText,
            kind = kind,
            tooltip = tooltip,
            widthMultiplier = widthMultiplier,
            textColor = textColor,
            enabled = enabled,
            disabledKind = disabledKind,
        )
        val control = CampaignButtonControls.addControl(buttons, button = button) {
            if (enabled) {
                onClick()
            }
        }
        if (!enabled) {
            CampaignButtonSuppression.applyDisabledCampaignButtonTemplate(control, showTooltipWhileInactive = true)
        }
    }

    fun addBidirectionalMomentaryRow(
        dialog: CustomPanelAPI,
        y: Float,
        leftLabel: String,
        buttonText: String,
        kind: CampaignActionButtonKind,
        buttons: MutableList<ButtonBase<*>>,
        highlightTokens: List<String> = emptyList(),
        tooltip: String? = null,
        widthMultiplier: Float = 1f,
        textColor: Color? = null,
        enabled: Boolean = true,
        disabledKind: CampaignActionButtonKind = CampaignActionButtonKind.DISABLED,
        onLeftClick: () -> Unit,
        onRightClick: () -> Unit,
    ) {
        val button = renderButton(
            dialog = dialog,
            y = y,
            leftLabel = leftLabel,
            buttonText = buttonText,
            kind = kind,
            highlightTokens = highlightTokens,
            tooltip = tooltip,
            widthMultiplier = widthMultiplier,
            textColor = textColor,
            enabled = enabled,
            disabledKind = disabledKind,
        )
        val control = CampaignButtonControls.addBidirectionalMomentaryButton(buttons,
            button = button,
            onLeftClick = {
                if (enabled) {
                    onLeftClick()
                }
            },
            onRightClick = {
                if (enabled) {
                    onRightClick()
                }
            },
        )
        if (!enabled) {
            CampaignButtonSuppression.applyDisabledCampaignButtonTemplate(control, showTooltipWhileInactive = true)
        }
    }

    fun renderDeltaButtons(
        dialog: CustomPanelAPI,
        y: Float,
        leftLabel: String,
        dataPrefix: String,
        tooltipLabel: String,
        buttons: MutableList<ButtonBase<*>>,
        widthMultiplier: Float = 1f,
        tooltip: String? = null,
        deltas: List<Int> = listOf(-10, -1, 1, 10),
        enabled: Boolean = true,
        onDelta: (Int) -> Unit,
    ) {
        val effectiveTooltip = tooltip ?: "Adjust $tooltipLabel."
        val row = createRow(
            dialog = dialog,
            y = y,
            label = leftLabel,
            widthMultiplier = widthMultiplier,
            textColor = CampaignGuiStyle.DISABLED_TAG_TEXT_COLOR.takeUnless { enabled },
            tooltip = effectiveTooltip,
        )
        val valueWidth = componentValueWidth(widthMultiplier)
        val buttonWidth = valueWidth / deltas.size.toFloat()
        val baseX = valueStartX(widthMultiplier)
        deltas.forEachIndexed { index, delta ->
            val x = baseX + index * buttonWidth
            val button = addButtonRegion(
                row = row,
                data = "$dataPrefix:$delta",
                x = x,
                width = buttonWidth,
                kind = if (enabled) CampaignActionButtonKind.UNCOLOURED else CampaignActionButtonKind.DISABLED,
                labelText = if (delta > 0) "+$delta" else delta.toString(),
                tooltip = effectiveTooltip,
                textColor = CampaignGuiStyle.DISABLED_TAG_TEXT_COLOR.takeUnless { enabled },
            )
            val control = CampaignButtonControls.addControl(buttons, button = button) {
                if (enabled) onDelta(delta)
            }
            if (!enabled) {
                CampaignButtonSuppression.applyDisabledCampaignButtonTemplate(control, showTooltipWhileInactive = true)
            }
        }
    }

    fun renderDecimalDeltaButtons(
        dialog: CustomPanelAPI,
        y: Float,
        leftLabel: String,
        dataPrefix: String,
        tooltipLabel: String,
        buttons: MutableList<ButtonBase<*>>,
        minorStepLabel: String,
        majorStepLabel: String,
        minorStep: Float,
        majorStep: Float,
        widthMultiplier: Float = 1f,
        tooltip: String? = null,
        enabled: Boolean = true,
        onDelta: (Float) -> Unit,
    ) {
        val effectiveTooltip = tooltip ?: "Adjust $tooltipLabel."
        val row = createRow(
            dialog = dialog,
            y = y,
            label = leftLabel,
            widthMultiplier = widthMultiplier,
            textColor = CampaignGuiStyle.DISABLED_TAG_TEXT_COLOR.takeUnless { enabled },
            tooltip = effectiveTooltip,
        )
        val valueWidth = componentValueWidth(widthMultiplier)
        val buttonWidth = valueWidth / 4f
        val baseX = valueStartX(widthMultiplier)
        listOf(
            -majorStep to "-$majorStepLabel",
            -minorStep to "-$minorStepLabel",
            minorStep to "+$minorStepLabel",
            majorStep to "+$majorStepLabel",
        ).forEachIndexed { index, (delta, label) ->
            val x = baseX + index * buttonWidth
            val button = addButtonRegion(
                row = row,
                data = "$dataPrefix:$label",
                x = x,
                width = buttonWidth,
                kind = if (enabled) CampaignActionButtonKind.UNCOLOURED else CampaignActionButtonKind.DISABLED,
                labelText = label,
                tooltip = effectiveTooltip,
                textColor = CampaignGuiStyle.DISABLED_TAG_TEXT_COLOR.takeUnless { enabled },
            )
            val control = CampaignButtonControls.addControl(buttons, button = button) {
                if (enabled) onDelta(delta)
            }
            if (!enabled) {
                CampaignButtonSuppression.applyDisabledCampaignButtonTemplate(control, showTooltipWhileInactive = true)
            }
        }
    }

    fun addColorRegion(
        row: CustomPanelAPI,
        x: Float,
        width: Float,
        color: Color,
        labelText: String,
        horizontalPadding: Float = CampaignGuiStyle.ITEM_TEXT_HORIZONTAL_PADDING,
    ) {
        val region = row.createCustomPanel(
            width,
            CampaignGuiStyle.MODAL_ROW_HEIGHT,
            CampaignPanelPlugin(CampaignPanelType.WEAPON_PANEL, fillColor = color)
        )
        row.addComponent(region)
        region.position.inTL(x, 0f)
        CampaignControlLabels.renderTagLabel(
            panel = region,
            text = labelText,
            width = width - 2f * horizontalPadding,
            height = CampaignGuiStyle.MODAL_ROW_HEIGHT - CampaignGuiStyle.ITEM_TEXT_TOP_PADDING,
            x = horizontalPadding,
            y = CampaignGuiStyle.ITEM_TEXT_TOP_PADDING,
            textColor = CampaignGuiStyle.DEFAULT_TEXT_COLOUR,
            alignment = Alignment.MID,
        )
    }

    private fun addNeutralValueRegion(
        row: CustomPanelAPI,
        x: Float,
        width: Float,
    ): CustomPanelAPI {
        val region = row.createCustomPanel(
            width,
            CampaignGuiStyle.MODAL_ROW_HEIGHT,
            CampaignPanelPlugin(
                CampaignPanelType.CONTROL_PANEL,
                fillColor = CampaignGuiStyle.MODAL_COMPONENT_ROW_FILL_COLOR,
                borderColor = CampaignGuiStyle.MODAL_COMPONENT_BORDER_COLOR,
            )
        )
        row.addComponent(region)
        region.position.inTL(x, 0f)
        return region
    }

    fun addButtonRegion(
        row: CustomPanelAPI,
        data: Any,
        x: Float,
        width: Float,
        kind: CampaignActionButtonKind,
        labelText: String,
        highlightTokens: List<String> = emptyList(),
        tooltip: String? = null,
        textColor: Color? = null,
        buttonColors: CampaignGuiStyle.ButtonStateColors = CampaignGuiStyle.colorsForModalComponentButton(kind),
        fillColor: Color? = if (CampaignGuiStyle.shouldFillActionButtonIdle(kind)) buttonColors.idle else null,
        checked: Boolean = false,
        visualOverrideColor: Color? = null,
    ): ButtonAPI {
        val shell = CampaignActionRows.addStyledCampaignButtonShell(
            parent = row,
            data = data,
            x = x,
            y = 0f,
            width = width,
            height = CampaignGuiStyle.MODAL_ROW_HEIGHT,
            colors = buttonColors,
            tooltip = tooltip,
            fillIdle = fillColor != null,
            panelType = CampaignPanelType.CONTROL_PANEL,
            fillColorOverride = fillColor,
            borderColor = CampaignGuiStyle.MODAL_COMPONENT_BORDER_COLOR,
        )
        val button = shell.button
        button.isChecked = checked
        visualOverrideColor?.let { color ->
            CampaignGuiStyle.applyCheckboxVisualOverrides(
                button = button,
                glowColor = color,
                borderColor = color,
                borderThickness = -1f
            )
        }

        val textPanel = shell.panel.createUIElement(
            width - 2f * CampaignGuiStyle.ACTION_ROW_PADDING,
            CampaignGuiStyle.MODAL_ROW_HEIGHT - CampaignGuiStyle.ITEM_TEXT_TOP_PADDING,
            false
        )
        val label = textPanel.addAgcText(labelText, 0f, textColor ?: CampaignGuiStyle.DEFAULT_TEXT_COLOUR)
        val highlights = highlightTokens.filter { labelText.contains(it) }
        if (highlights.isNotEmpty()) {
            label.setHighlight(*highlights.toTypedArray())
            label.setHighlightColors(*Array(highlights.size) { CampaignGuiStyle.MODIFIER_TEXT_COLOUR })
        }
        label.setAlignment(Alignment.MID)
        shell.panel.addUIElement(textPanel).inTL(CampaignGuiStyle.ACTION_ROW_PADDING, CampaignGuiStyle.ITEM_TEXT_TOP_PADDING)
        return button
    }

    private fun addRowTooltip(row: CustomPanelAPI, tooltip: String?) {
        if (tooltip.isNullOrBlank()) return
        val binder = row.createUIElement(0f, 0f, false)
        binder.addTooltip(row, TooltipLocation.BELOW, CampaignGuiStyle.STANDARD_TOOLTIP_WIDTH) { tooltipPanel ->
            tooltipPanel.applyAgcTooltipTextStyle()
            tooltipPanel.addAgcText(tooltip, 5f)
        }
        row.addUIElement(binder).inTL(0f, 0f)
    }

    private fun fittedText(text: String, width: Float): String {
        return computeTextFitLayout(
            text = text,
            availableWidth = width,
            availableHeight = CampaignGuiStyle.MODAL_ROW_HEIGHT,
            minRowHeight = CampaignGuiStyle.MODAL_ROW_HEIGHT,
            horizontalPadding = 2f * CampaignGuiStyle.ITEM_TEXT_HORIZONTAL_PADDING,
            verticalPadding = 0f,
            maxLines = 1,
            canGrowHeight = false
        ).wrappedText
    }
}
