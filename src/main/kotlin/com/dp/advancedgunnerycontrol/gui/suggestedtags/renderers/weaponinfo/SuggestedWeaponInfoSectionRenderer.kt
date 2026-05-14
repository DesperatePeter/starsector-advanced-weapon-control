package com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers.weaponinfo

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.gui.controls.rows.CampaignActionRows
import com.dp.advancedgunnerycontrol.gui.controls.text.CampaignTooltipCopy
import com.dp.advancedgunnerycontrol.gui.controls.toggles.CampaignToggleHeading
import com.dp.advancedgunnerycontrol.gui.foundation.addAgcText
import com.dp.advancedgunnerycontrol.gui.style.CampaignActionButtonKind
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.fs.starfarer.api.ui.CustomPanelAPI

internal class SuggestedWeaponInfoSectionRenderer(
    private val buttons: MutableList<ButtonBase<*>>,
    private val sectionState: SuggestedWeaponInfoSectionState,
) {
    fun renderWeaponInfoSection(
        panel: CustomPanelAPI,
        weaponId: String,
        sectionId: String,
        title: String,
        rows: List<String>,
        y: Float,
        reserveBottomHeight: Float = 0f,
    ): Float {
        val key = sectionState.key(weaponId, sectionId)
        val collapsed = sectionState.weaponInfoSectionCollapsed(key, sectionId)
        if (y + CampaignGuiStyle.TAG_ITEM_HEIGHT > panel.position.height) return panel.position.height
        val heading = CampaignToggleHeading(
            title = title,
            expanded = !collapsed,
            active = false,
            subject = "weapon info",
            inactiveKind = CampaignActionButtonKind.TAG_TOP_LEVEL_SECTION,
            activeKind = CampaignActionButtonKind.TAG_TOP_LEVEL_SECTION,
        )
        val button = CampaignActionRows.addTemplatedCampaignMomentaryActionButton(
            parent = panel,
            data = "suggested_weapon_info:$key",
            x = 0f,
            y = y,
            width = panel.position.width,
            height = CampaignGuiStyle.TAG_ITEM_HEIGHT,
            template = CampaignGuiStyle.actionButtonTemplate(heading.kind),
            labelText = heading.label,
            tooltip = CampaignTooltipCopy.weaponInfoSection(title, !collapsed),
            centerText = true,
        ) {
            sectionState.toggleWeaponInfoSection(sectionId, key)
        }
        buttons.add(button)
        var nextY = y + CampaignGuiStyle.TAG_ITEM_HEIGHT
        if (!collapsed) {
            nextY += SuggestedWeaponInfoLayout.WEAPON_INFO_TEXT_COMPONENT_GAP
            val rowWidth = panel.position.width - 2f * CampaignGuiStyle.PANEL_PADDING
            rows.forEachIndexed { rowIndex, line ->
                val wrappedLines = SuggestedWeaponInfoTextLayout.wrappedInfoLines(line, rowWidth)
                wrappedLines.forEachIndexed { index, wrappedLine ->
                    val rowBottomLimit = panel.position.height - reserveBottomHeight
                    if (nextY + SuggestedWeaponInfoLayout.WEAPON_INFO_ROW_HEIGHT > rowBottomLimit) return nextY
                    val hasMoreRows = index < wrappedLines.lastIndex || rowIndex < rows.lastIndex
                    val isLastAvailableRow = hasMoreRows &&
                        nextY + 2f * SuggestedWeaponInfoLayout.WEAPON_INFO_ROW_HEIGHT > rowBottomLimit
                    val renderedLine = if (isLastAvailableRow) "..." else wrappedLine
                    val row = panel.createUIElement(
                        rowWidth,
                        SuggestedWeaponInfoLayout.WEAPON_INFO_ROW_HEIGHT,
                        false
                    )
                    row.addAgcText(renderedLine, 0f)
                    panel.addUIElement(row).inTL(CampaignGuiStyle.PANEL_PADDING, nextY)
                    nextY += SuggestedWeaponInfoLayout.WEAPON_INFO_ROW_HEIGHT
                    if (isLastAvailableRow) return nextY
                }
            }
            nextY += SuggestedWeaponInfoLayout.WEAPON_INFO_TEXT_COMPONENT_GAP
        }
        return nextY
    }
}
