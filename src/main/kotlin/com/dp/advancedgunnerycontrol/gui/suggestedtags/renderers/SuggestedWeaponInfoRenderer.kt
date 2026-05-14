package com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.gui.controls.text.CampaignControlLabels
import com.dp.advancedgunnerycontrol.gui.foundation.CampaignPanelPlugin
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.dp.advancedgunnerycontrol.gui.style.CampaignPanelType
import com.dp.advancedgunnerycontrol.gui.suggestedtags.filters.WeaponFilter
import com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers.weaponinfo.SuggestedWeaponInfoHeightCalculator
import com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers.weaponinfo.SuggestedWeaponInfoImageSizer
import com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers.weaponinfo.SuggestedWeaponInfoLayout
import com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers.weaponinfo.SuggestedWeaponInfoRows
import com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers.weaponinfo.SuggestedWeaponInfoSectionRenderer
import com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers.weaponinfo.SuggestedWeaponInfoSectionState
import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.ui.CustomPanelAPI
import kotlin.math.min

/**
 * Renders the weapon-image and metadata panel above each suggested-tag list.
 * The owning view keeps the section state so it can persist across rebuilds.
 */
class SuggestedWeaponInfoRenderer(
    private val buttons: MutableList<ButtonBase<*>>,
    private val collapsedWeaponInfoSections: MutableSet<String>,
    private val expandedAdvancedWeaponInfoSections: MutableSet<String>,
    private val onStateChanged: () -> Unit,
) {
    companion object {
        const val MIN_RENDERABLE_TAG_LIST_HEIGHT = SuggestedWeaponInfoLayout.MIN_RENDERABLE_TAG_LIST_HEIGHT
        const val WEAPON_INFO_TO_TAG_GAP = SuggestedWeaponInfoLayout.WEAPON_INFO_TO_TAG_GAP
    }

    private val sectionState = SuggestedWeaponInfoSectionState(
        collapsedWeaponInfoSections = collapsedWeaponInfoSections,
        expandedAdvancedWeaponInfoSections = expandedAdvancedWeaponInfoSections,
        onStateChanged = onStateChanged,
    )
    private val sectionRenderer = SuggestedWeaponInfoSectionRenderer(buttons, sectionState)
    private val heightCalculator = SuggestedWeaponInfoHeightCalculator(sectionState)

    fun buildWeaponInfo(panel: CustomPanelAPI, weaponId: String?) {
        CampaignControlLabels.addCampaignPanelHeading(panel, "Weapon", headingHeight = CampaignGuiStyle.CONTAINER_HEADING_HEIGHT)
        if (weaponId == null) return

        val showAdvancedInfo = Settings.showSuggestedTagAdvancedInfo()
        val spec = WeaponFilter.specForOrNull(weaponId) ?: return
        val innerWidth = panel.position.width - 2f * CampaignGuiStyle.PANEL_PADDING
        val imageMax = min(SuggestedWeaponInfoLayout.WEAPON_IMAGE_MAX, innerWidth - 8f)
        val bodyTop = CampaignGuiStyle.PANEL_PADDING +
            CampaignGuiStyle.CONTAINER_HEADING_HEIGHT +
            SuggestedWeaponInfoLayout.WEAPON_IMAGE_TOP_GAP
        val spriteName = spec.turretSpriteName
        val (imageWidth, imageHeight) = SuggestedWeaponInfoImageSizer.fitSprite(spriteName, imageMax, imageMax)
        if (imageWidth > 0f && imageHeight > 0f) {
            val imagePanel = panel.createUIElement(imageWidth, imageHeight, false)
            imagePanel.addImage(spriteName, imageWidth, imageHeight, 0f)
            panel.addUIElement(imagePanel).inTL((panel.position.width - imageWidth) / 2f, bodyTop)
        }

        val statsTop = bodyTop + imageMax + SuggestedWeaponInfoLayout.WEAPON_IMAGE_BOTTOM_GAP
        val statsHeight = (panel.position.height - statsTop).coerceAtLeast(0f)
        if (statsHeight <= 0f) return
        val statsPanel = panel.createCustomPanel(
            panel.position.width,
            statsHeight,
            CampaignPanelPlugin(CampaignPanelType.SUGGESTED_WEAPON_PANEL)
        )
        panel.addComponent(statsPanel)
        statsPanel.position.inTL(0f, statsTop)
        var y = 0f
        y = sectionRenderer.renderWeaponInfoSection(
            panel = statsPanel,
            weaponId = weaponId,
            sectionId = "basic",
            title = "Basic Info",
            rows = SuggestedWeaponInfoRows.basicInfoRows(spec),
            y = y,
            reserveBottomHeight = if (showAdvancedInfo) CampaignGuiStyle.TAG_ITEM_HEIGHT else 0f,
        )
        if (showAdvancedInfo) {
            sectionRenderer.renderWeaponInfoSection(
                panel = statsPanel,
                weaponId = weaponId,
                sectionId = "advanced",
                title = "Advanced Info",
                rows = SuggestedWeaponInfoRows.advancedInfoRows(spec),
                y = y,
            )
        }
    }

    fun suggestedWeaponInfoHeight(columnWidth: Float, columnHeight: Float, weaponId: String?): Float {
        return heightCalculator.suggestedWeaponInfoHeight(columnWidth, columnHeight, weaponId)
    }
}
