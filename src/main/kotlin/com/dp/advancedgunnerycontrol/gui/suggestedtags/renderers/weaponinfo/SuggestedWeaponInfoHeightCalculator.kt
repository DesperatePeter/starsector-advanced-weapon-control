package com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers.weaponinfo

import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.dp.advancedgunnerycontrol.gui.suggestedtags.filters.WeaponFilter
import com.dp.advancedgunnerycontrol.settings.Settings
import kotlin.math.max
import kotlin.math.min

internal class SuggestedWeaponInfoHeightCalculator(
    private val sectionState: SuggestedWeaponInfoSectionState,
) {
    fun suggestedWeaponInfoHeight(columnWidth: Float, columnHeight: Float, weaponId: String?): Float {
        val baseHeight = CampaignGuiStyle.PANEL_PADDING +
            CampaignGuiStyle.CONTAINER_HEADING_HEIGHT +
            SuggestedWeaponInfoLayout.WEAPON_IMAGE_TOP_GAP
        if (weaponId == null) return baseHeight

        val spec = WeaponFilter.specForOrNull(weaponId) ?: return baseHeight
        val innerWidth = columnWidth - 2f * CampaignGuiStyle.PANEL_PADDING
        val imageMax = min(SuggestedWeaponInfoLayout.WEAPON_IMAGE_MAX, innerWidth - 8f)
        val statsTop = baseHeight + imageMax + SuggestedWeaponInfoLayout.WEAPON_IMAGE_BOTTOM_GAP
        val rowWidth = columnWidth - 2f * CampaignGuiStyle.PANEL_PADDING
        val desiredHeight = statsTop +
            estimatedWeaponInfoSectionHeight(weaponId, "basic", SuggestedWeaponInfoRows.basicInfoRows(spec), rowWidth) +
            (if (Settings.showSuggestedTagAdvancedInfo()) {
                estimatedWeaponInfoSectionHeight(weaponId, "advanced", SuggestedWeaponInfoRows.advancedInfoRows(spec), rowWidth)
            } else {
                0f
            })
        val minInfoHeight = min(
            columnHeight.coerceAtLeast(0f),
            max(SuggestedWeaponInfoLayout.WEAPON_INFO_MIN_HEIGHT, statsTop + CampaignGuiStyle.SHIP_MODE_ITEM_HEIGHT)
        )
        val maxInfoHeight = (
            columnHeight -
                SuggestedWeaponInfoLayout.SUGGESTED_TAG_LIST_MIN_HEIGHT -
                SuggestedWeaponInfoLayout.WEAPON_INFO_TO_TAG_GAP
            )
            .coerceAtLeast(minInfoHeight)
            .coerceAtMost(columnHeight.coerceAtLeast(0f))
        return desiredHeight
            .coerceAtLeast(minInfoHeight)
            .coerceAtMost(maxInfoHeight)
    }

    private fun estimatedWeaponInfoSectionHeight(
        weaponId: String,
        sectionId: String,
        rows: List<String>,
        rowWidth: Float,
    ): Float {
        val key = sectionState.key(weaponId, sectionId)
        val collapsed = sectionState.weaponInfoSectionCollapsed(key, sectionId)
        if (sectionId == "basic" && !collapsed) return SuggestedWeaponInfoLayout.BASIC_INFO_SECTION_HEIGHT
        return CampaignGuiStyle.TAG_ITEM_HEIGHT +
            if (collapsed) {
                0f
            } else {
                2f * SuggestedWeaponInfoLayout.WEAPON_INFO_TEXT_COMPONENT_GAP +
                    rows.sumOf { SuggestedWeaponInfoTextLayout.wrappedLineCount(it, rowWidth) } *
                    SuggestedWeaponInfoLayout.WEAPON_INFO_ROW_HEIGHT
            }
    }
}
