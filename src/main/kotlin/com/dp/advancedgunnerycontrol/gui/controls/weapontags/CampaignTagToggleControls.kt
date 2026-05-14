package com.dp.advancedgunnerycontrol.gui.controls.weapontags

import com.dp.advancedgunnerycontrol.gui.controls.rows.CampaignActionRows
import com.dp.advancedgunnerycontrol.gui.controls.rows.StyledCampaignButtonShell
import com.dp.advancedgunnerycontrol.gui.controls.text.CampaignControlLabels

import com.dp.advancedgunnerycontrol.gui.foundation.WrapGridMetrics
import com.dp.advancedgunnerycontrol.gui.foundation.computeTextFitLayout
import com.dp.advancedgunnerycontrol.gui.modals.muteCampaignButtonSounds
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions
import com.fs.starfarer.api.ui.CustomPanelAPI

internal object CampaignTagToggleControls {
    fun addTagModeToggleShell(
        parent: CustomPanelAPI,
        data: Any,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        tooltip: String,
        unavailable: Boolean = false,
        fillIdle: Boolean = false,
        fillSelected: Boolean = false,
    ): StyledCampaignButtonShell {
        val colors = when {
            unavailable -> CampaignGuiStyle.DISABLED_BUTTON_COLORS
            fillSelected -> CampaignGuiStyle.ACTIVE_WEAPON_TAG_SHIP_MODE_BUTTON_COLORS
            else -> CampaignGuiStyle.UNCOLOURED_BUTTON_COLORS
        }
        val shell = CampaignActionRows.addStyledCampaignButtonShell(
            parent = parent,
            data = data,
            x = x,
            y = y,
            width = width,
            height = height,
            colors = colors,
            tooltip = tooltip,
            fillIdle = fillIdle || fillSelected || unavailable,
            fillColorOverride = CampaignGuiStyle.DISABLED_TAG_BACKGROUND_COLOR.takeIf { unavailable },
        )
        if (unavailable) {
            shell.button.isEnabled = false
            shell.button.setShowTooltipWhileInactive(true)
            muteCampaignButtonSounds(shell.button)
            CampaignGuiStyle.applyUnavailableCheckboxVisualState(shell.button)
        }
        return shell
    }

    fun addTagToggleButton(
        parent: CustomPanelAPI,
        data: Any,
        tag: String,
        index: Int,
        metrics: WrapGridMetrics,
        tooltip: String,
        unavailable: Boolean = false,
        selected: Boolean = false,
    ): StyledCampaignButtonShell {
        val labelWidth = metrics.itemWidth - 2f * CampaignGuiStyle.ITEM_TEXT_HORIZONTAL_PADDING
        val displayTag = EditableWeaponTagDefinitions.displayName(tag)
        val labelText = computeTextFitLayout(
            text = displayTag,
            availableWidth = labelWidth,
            availableHeight = metrics.itemHeight - CampaignGuiStyle.ITEM_TEXT_TOP_PADDING,
            minRowHeight = metrics.itemHeight,
            horizontalPadding = 0f,
            verticalPadding = 0f,
            maxLines = 1,
            canGrowHeight = false
        ).wrappedText
        val shell = addTagModeToggleShell(
            parent = parent,
            data = data,
            x = metrics.xFor(index),
            y = metrics.yFor(index),
            width = metrics.itemWidth,
            height = metrics.itemHeight,
            tooltip = tooltip,
            unavailable = unavailable,
            fillIdle = !selected && !unavailable,
            fillSelected = selected
        )
        CampaignControlLabels.renderCenteredControlLabel(
            panel = shell.panel,
            text = labelText,
            width = metrics.itemWidth,
            height = metrics.itemHeight - CampaignGuiStyle.ITEM_TEXT_TOP_PADDING,
            centerRegionOffsetX = CampaignGuiStyle.ITEM_HIGHLIGHT_X_OFFSET,
            centerRegionWidth = metrics.itemWidth,
            textColor = if (unavailable) CampaignGuiStyle.DISABLED_TAG_TEXT_COLOR else null,
            highlightTokens = CampaignControlLabels.hiddenChangeHighlightTokens(labelText),
        )
        return shell
    }
}
