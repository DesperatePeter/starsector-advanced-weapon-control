package com.dp.advancedgunnerycontrol.gui.controls.rows

import com.dp.advancedgunnerycontrol.gui.controls.buttons.CampaignMomentaryButton
import com.dp.advancedgunnerycontrol.gui.controls.suppression.CampaignButtonSuppression
import com.dp.advancedgunnerycontrol.gui.controls.text.CampaignControlLabels

import com.dp.advancedgunnerycontrol.gui.foundation.CampaignPanelPlugin
import com.dp.advancedgunnerycontrol.gui.entrypoints.AGCGUI
import com.dp.advancedgunnerycontrol.gui.presets.PresetControlState
import com.dp.advancedgunnerycontrol.gui.style.CampaignActionButtonKind
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.dp.advancedgunnerycontrol.gui.style.CampaignPanelType
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipLocation
import java.awt.Color

data class StyledCampaignButtonShell(
    val panel: CustomPanelAPI,
    val button: ButtonAPI,
)

internal object CampaignActionRows {
    private fun addRegisteredControlPanel(
        parent: CustomPanelAPI,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        panelType: CampaignPanelType = CampaignPanelType.CONTROL_PANEL,
        fillColor: Color? = null,
        borderColor: Color? = null,
    ): CustomPanelAPI {
        val itemPanel = parent.createCustomPanel(
            width,
            height,
            CampaignPanelPlugin(
                panelType,
                fillColor = fillColor,
                borderColor = borderColor
            )
        )
        parent.addComponent(itemPanel)
        itemPanel.position.inTL(x, y)
        return itemPanel
    }

    fun addStyledCampaignButtonShell(
        parent: CustomPanelAPI,
        data: Any,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        colors: CampaignGuiStyle.ButtonStateColors,
        tooltip: String? = null,
        fillIdle: Boolean = true,
        panelType: CampaignPanelType = CampaignPanelType.CONTROL_PANEL,
        fillColorOverride: Color? = null,
        borderColor: Color? = null,
    ): StyledCampaignButtonShell {
        val itemPanel = addRegisteredControlPanel(
            parent = parent,
            x = x,
            y = y,
            width = width,
            height = height,
            panelType = panelType,
            fillColor = fillColorOverride ?: if (fillIdle) colors.idle else null,
            borderColor = borderColor,
        )

        val inner = itemPanel.createUIElement(width, height, false)
        val checkboxColors = CampaignGuiStyle.checkboxColorsForButton(colors)
        val button = inner.addAreaCheckbox(
            "",
            data,
            checkboxColors.base,
            checkboxColors.bg,
            checkboxColors.bright,
            width,
            height,
            0f
        )
        CampaignButtonSuppression.registerCampaignButton(button)
        if (!tooltip.isNullOrBlank()) {
            inner.addTooltipToPrevious(
                AGCGUI.makeTooltip(tooltip),
                TooltipLocation.BELOW
            )
        }
        itemPanel.addUIElement(inner).inTL(CampaignGuiStyle.ITEM_HIGHLIGHT_X_OFFSET, 0f)
        return StyledCampaignButtonShell(itemPanel, button)
    }

    fun addStyledCampaignActionRow(
        parent: CustomPanelAPI,
        data: Any,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        kind: CampaignActionButtonKind,
        labelText: String,
        highlightTokens: List<String>,
        tooltip: String? = null,
        textPadding: Float = CampaignGuiStyle.ACTION_ROW_PADDING,
        textColor: Color = CampaignGuiStyle.DEFAULT_TEXT_COLOUR,
        centerConfirmCancelText: Boolean = true,
        centerText: Boolean = false,
    ): StyledCampaignButtonShell {
        val shell = addStyledCampaignButtonShell(
            parent = parent,
            data = data,
            x = x,
            y = y,
            width = width,
            height = height,
            colors = CampaignGuiStyle.colorsForActionButton(kind),
            tooltip = tooltip,
            fillIdle = CampaignGuiStyle.shouldFillActionButtonIdle(kind)
        )
        if (centerText || (centerConfirmCancelText && (kind == CampaignActionButtonKind.CONFIRM || kind == CampaignActionButtonKind.CANCEL))) {
            CampaignControlLabels.renderCenteredControlLabel(
                panel = shell.panel,
                text = labelText,
                width = width,
                height = height - CampaignGuiStyle.ITEM_TEXT_TOP_PADDING,
                centerRegionOffsetX = CampaignGuiStyle.ITEM_HIGHLIGHT_X_OFFSET,
                centerRegionWidth = width,
                textColor = textColor,
            )
        } else {
            val textPanel = shell.panel.createUIElement(
                width - 2f * textPadding,
                height - CampaignGuiStyle.ITEM_TEXT_TOP_PADDING,
                false
            )
            CampaignControlLabels.addCampaignActionLabel(textPanel, labelText, highlightTokens, textColor)
            shell.panel.addUIElement(textPanel).inTL(textPadding, CampaignGuiStyle.ITEM_TEXT_TOP_PADDING)
        }
        return shell
    }

    fun addTemplatedCampaignActionRow(
        parent: CustomPanelAPI,
        data: Any,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        template: CampaignGuiStyle.ActionButtonTemplate,
        labelText: String,
        highlightTokens: List<String>,
        tooltip: String? = null,
        textPadding: Float = CampaignGuiStyle.ACTION_ROW_PADDING,
        centerConfirmCancelText: Boolean = true,
        centerText: Boolean = false,
    ): StyledCampaignButtonShell {
        return addStyledCampaignActionRow(
            parent = parent,
            data = data,
            x = x,
            y = y,
            width = width,
            height = height,
            kind = template.kind,
            labelText = labelText,
            highlightTokens = highlightTokens,
            tooltip = tooltip,
            textPadding = textPadding,
            textColor = template.textColor,
            centerConfirmCancelText = centerConfirmCancelText,
            centerText = centerText,
        )
    }

    fun addTemplatedCampaignMomentaryActionButton(
        parent: CustomPanelAPI,
        data: Any,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        template: CampaignGuiStyle.ActionButtonTemplate,
        labelText: String,
        highlightTokens: List<String> = emptyList(),
        tooltip: String? = null,
        textPadding: Float = CampaignGuiStyle.ACTION_ROW_PADDING,
        showTooltipWhileInactive: Boolean = false,
        centerConfirmCancelText: Boolean = true,
        centerText: Boolean = false,
        callback: () -> Unit,
    ): CampaignMomentaryButton {
        val shell = addTemplatedCampaignActionRow(
            parent = parent,
            data = data,
            x = x,
            y = y,
            width = width,
            height = height,
            template = template,
            labelText = labelText,
            highlightTokens = highlightTokens,
            tooltip = tooltip,
            textPadding = textPadding,
            centerConfirmCancelText = centerConfirmCancelText,
            centerText = centerText,
        )
        val button = CampaignMomentaryButton(shell.button) {
            if (template.enabled) {
                callback()
            }
        }
        if (!template.enabled) {
            CampaignButtonSuppression.applyDisabledCampaignButtonTemplate(button, showTooltipWhileInactive)
        }
        return button
    }

    fun updatePresetControlStateMap(
        currentStates: Map<Int, PresetControlState>,
        groupIndex: Int,
        state: PresetControlState,
    ): Map<Int, PresetControlState> {
        val updated = currentStates.toMutableMap()
        if (state == PresetControlState()) {
            updated.remove(groupIndex)
        } else {
            updated[groupIndex] = state
        }
        return updated
    }
}
