package com.dp.advancedgunnerycontrol.gui.panels.weapongroups

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.foundation.*

import com.dp.advancedgunnerycontrol.gui.*


import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.WeaponGroupAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.loading.WeaponGroupSpec
import com.fs.starfarer.api.loading.WeaponSpecAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import kotlin.math.max
import kotlin.math.min

data class CampaignWeaponPanelEntry(
    val label: String,
    val sprite: String,
)

/**
 * Weapon-group Weapons panel component.
 * Renders the compact list of weapons above each group's tag list in ShipView.
 */
object CampaignWeaponPanelRenderer {
    private val ENTRY_LABEL_COMPARATOR = Comparator<CampaignWeaponPanelEntry> { left, right ->
        left.label.compareTo(right.label)
    }

    private const val WEAPON_ENTRY_HEIGHT = 40f
    private const val WEAPON_IMAGE_WIDTH = 27f
    private const val WEAPON_ENTRY_CONTENT_LEFT_OFFSET = -4f
    private const val WEAPON_ENTRY_TEXT_GAP = 1f
    private const val WEAPON_TEXT_MAX_VISIBLE_CHARS = 29
    private const val WEAPON_TEXT_CHARS_PER_LINE = 17
    private const val WEAPON_TEXT_VERTICAL_INSET = 1f
    private const val WEAPON_PANEL_TOP_INSET = 3f
    private const val WEAPON_ROWS_VISIBLE = 4
    private const val WEAPON_OVERFLOW_ROW_HEIGHT = WEAPON_ENTRY_HEIGHT

    const val WEAPON_TO_TAG_GAP = 2f
    const val PANEL_HEIGHT = WEAPON_PANEL_TOP_INSET + WEAPON_ROWS_VISIBLE * WEAPON_ENTRY_HEIGHT + WEAPON_OVERFLOW_ROW_HEIGHT

    fun aggregateWeapons(group: WeaponGroupSpec, ship: FleetMemberAPI): List<CampaignWeaponPanelEntry> {
        val counts = linkedMapOf<String, Int>()
        group.slots.mapNotNull { ship.variant.getWeaponId(it) }.forEach { weaponId ->
            counts[weaponId] = (counts[weaponId] ?: 0) + 1
        }
        return counts.entries.map { (weaponId, count) ->
            val spec: WeaponSpecAPI = Global.getSettings().getWeaponSpec(weaponId)
            CampaignWeaponPanelEntry(
                label = "$count x ${spec.weaponName}",
                sprite = spec.turretSpriteName
            )
        }.sortedWith(ENTRY_LABEL_COMPARATOR)
    }

    fun aggregateWeapons(group: WeaponGroupAPI): List<CampaignWeaponPanelEntry> {
        val counts = linkedMapOf<String, Int>()
        group.weaponsCopy.mapNotNull { it?.id }.forEach { weaponId ->
            counts[weaponId] = (counts[weaponId] ?: 0) + 1
        }
        return counts.entries.map { (weaponId, count) ->
            val spec: WeaponSpecAPI = Global.getSettings().getWeaponSpec(weaponId)
            CampaignWeaponPanelEntry(
                label = "$count x ${spec.weaponName}",
                sprite = spec.turretSpriteName
            )
        }.sortedWith(ENTRY_LABEL_COMPARATOR)
    }

    fun render(panel: CustomPanelAPI, entries: List<CampaignWeaponPanelEntry>) {
        repeat(WEAPON_ROWS_VISIBLE) { index ->
            renderWeaponEntryPanel(
                panel,
                entries.getOrNull(index),
                WEAPON_PANEL_TOP_INSET + index * WEAPON_ENTRY_HEIGHT
            )
        }

        if (entries.size == WEAPON_ROWS_VISIBLE + 1) {
            renderWeaponEntryPanel(
                panel,
                entries.getOrNull(WEAPON_ROWS_VISIBLE),
                WEAPON_PANEL_TOP_INSET + WEAPON_ROWS_VISIBLE * WEAPON_ENTRY_HEIGHT
            )
        } else if (entries.size > WEAPON_ROWS_VISIBLE + 1) {
            renderWeaponEntryPanel(
                panel,
                CampaignWeaponPanelEntry("Hover for full weapon list", ""),
                WEAPON_PANEL_TOP_INSET + WEAPON_ROWS_VISIBLE * WEAPON_ENTRY_HEIGHT,
                imageText = "...",
            )
        } else {
            renderWeaponEntryPanel(
                panel,
                null,
                WEAPON_PANEL_TOP_INSET + WEAPON_ROWS_VISIBLE * WEAPON_ENTRY_HEIGHT
            )
        }
    }

    fun addWeaponEntriesTooltip(anchorPanel: CustomPanelAPI, entries: List<CampaignWeaponPanelEntry>) {
        val binder = anchorPanel.createUIElement(0f, 0f, false)
        binder.addTooltip(anchorPanel, TooltipMakerAPI.TooltipLocation.BELOW, CampaignGuiStyle.WEAPON_ENTRY_TOOLTIP_WIDTH) { tooltip ->
            tooltip.applyAgcTooltipTextStyle()
            entries.forEach { entry ->
                if (entry.sprite.isNotBlank()) {
                    val imageText = tooltip.beginImageWithText(entry.sprite, 16f)
                    imageText.applyAgcTooltipTextStyle()
                    imageText.addAgcText(entry.label, 0f)
                    tooltip.addImageWithText(2f)
                } else {
                    tooltip.addAgcText(entry.label, 0f)
                }
            }
        }
        anchorPanel.addUIElement(binder).inTL(0f, 0f)
    }

    private fun wrapWeaponLabel(text: String, allowFullTwoLine: Boolean = false): String {
        val fitted = fitAgcTextByChars(text, WEAPON_TEXT_CHARS_PER_LINE, maxLines = 2)
        if (allowFullTwoLine || fitted.length <= WEAPON_TEXT_MAX_VISIBLE_CHARS) {
            return fitted
        }
        val visible = fitted.split("\n").toMutableList()
        visible[visible.lastIndex] = truncateAgcTextByChars(visible.last(), WEAPON_TEXT_CHARS_PER_LINE)
        return visible.joinToString("\n")
    }

    private fun fitSprite(spriteName: String, maxWidth: Float, maxHeight: Float): Pair<Float, Float> {
        if (spriteName.isBlank()) return 0f to 0f
        return try {
            val sprite = Global.getSettings().getSprite(spriteName)
            val spriteWidth = sprite.width
            val spriteHeight = sprite.height
            if (spriteWidth <= 0f || spriteHeight <= 0f) {
                min(maxWidth, maxHeight) to min(maxWidth, maxHeight)
            } else {
                val scale = min(maxWidth / spriteWidth, maxHeight / spriteHeight)
                spriteWidth * scale to spriteHeight * scale
            }
        } catch (_: Throwable) {
            min(maxWidth, maxHeight) to min(maxWidth, maxHeight)
        }
    }

    private fun renderWeaponEntryPanel(
        panel: CustomPanelAPI,
        entry: CampaignWeaponPanelEntry?,
        top: Float,
        imageText: String? = null,
        forceSingleLine: Boolean = false,
    ) {
        val entryPanel = panel.createCustomPanel(
            panel.position.width,
            WEAPON_ENTRY_HEIGHT,
            CampaignPanelPlugin(CampaignPanelType.WEAPON_ENTRY_PANEL)
        )
        panel.addComponent(entryPanel)
        entryPanel.position.inTL(0f, top)

        if (entry == null && imageText == null) return

        if (imageText != null) {
            val imageTextPanel = entryPanel.createUIElement(
                WEAPON_IMAGE_WIDTH,
                WEAPON_ENTRY_HEIGHT - CampaignGuiStyle.ITEM_TEXT_TOP_PADDING,
                false
            )
            imageTextPanel.addAgcText(imageText, 0f)
            entryPanel.addUIElement(imageTextPanel).inTL(0f, CampaignGuiStyle.ITEM_TEXT_TOP_PADDING)
        } else if (entry?.sprite?.isNotBlank() == true) {
            val imageMaxHeight = WEAPON_ENTRY_HEIGHT - 6f
            val (imageWidth, imageHeight) = fitSprite(entry.sprite, WEAPON_IMAGE_WIDTH, imageMaxHeight)
            if (imageWidth > 0f && imageHeight > 0f) {
                val imagePanel = entryPanel.createUIElement(WEAPON_IMAGE_WIDTH, WEAPON_ENTRY_HEIGHT, false)
                imagePanel.addImage(entry.sprite, imageWidth, imageHeight, 0f)
                entryPanel.addUIElement(imagePanel).inTL(
                    WEAPON_ENTRY_CONTENT_LEFT_OFFSET,
                    max(0f, (WEAPON_ENTRY_HEIGHT - imageHeight) / 2f)
                )
            }
        }

        if (entry == null) return

        val textLeft = WEAPON_ENTRY_CONTENT_LEFT_OFFSET + WEAPON_IMAGE_WIDTH + WEAPON_ENTRY_TEXT_GAP
        val textWidth = entryPanel.position.width - textLeft - WEAPON_ENTRY_TEXT_GAP
        val textPanel = entryPanel.createUIElement(
            textWidth,
            WEAPON_ENTRY_HEIGHT - CampaignGuiStyle.ITEM_TEXT_TOP_PADDING - WEAPON_TEXT_VERTICAL_INSET,
            false
        )
        val renderedText = if (forceSingleLine) {
            truncateAgcTextByChars(entry.label, WEAPON_TEXT_MAX_VISIBLE_CHARS)
        } else {
            wrapWeaponLabel(entry.label, allowFullTwoLine = imageText != null)
        }
        textPanel.addAgcText(renderedText, 0f)
        entryPanel.addUIElement(textPanel).inTL(textLeft, CampaignGuiStyle.ITEM_TEXT_TOP_PADDING + WEAPON_TEXT_VERTICAL_INSET)
    }
}
