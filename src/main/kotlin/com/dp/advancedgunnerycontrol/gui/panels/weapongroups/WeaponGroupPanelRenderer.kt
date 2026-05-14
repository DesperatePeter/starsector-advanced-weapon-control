package com.dp.advancedgunnerycontrol.gui.panels.weapongroups

import com.dp.advancedgunnerycontrol.gui.controls.buttons.CampaignButtonControls
import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.gui.controls.scroll.computeVerticalItemsHeight
import com.dp.advancedgunnerycontrol.gui.controls.scroll.computeVerticalSlotCount
import com.dp.advancedgunnerycontrol.gui.controls.text.CampaignControlLabels

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.foundation.*

import com.dp.advancedgunnerycontrol.gui.*

import com.dp.advancedgunnerycontrol.gui.layout.WeaponGroupPanelLayout
import com.dp.advancedgunnerycontrol.gui.presets.*

import com.dp.advancedgunnerycontrol.shipdata.getVariantWeaponGroup
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import java.awt.Color
import kotlin.math.max

internal data class WeaponGroupPanelCallbacks(
    val isCollapsed: (String) -> Boolean,
    val toggleCollapsed: (String) -> Unit,
    val presetStateForGroup: (Int) -> PresetControlState,
    val updatePresetState: (Int, PresetControlState) -> Unit,
    val openPresetActionModal: (Int, PresetControlState) -> Unit,
    val isPresetDirty: (FleetMemberAPI, Int) -> Boolean,
    val recordGroupPanel: (Int, CustomPanelAPI) -> Unit,
    val buildTagList: (
        panel: CustomPanelAPI,
        ship: FleetMemberAPI,
        groupIndex: Int,
        relativeScrollLeft: Float,
        relativeScrollRight: Float,
        relativeScrollBottom: Float,
        relativeScrollTop: Float,
    ) -> Unit,
)

internal object WeaponGroupPanelRenderer {
    fun buildGroups(
        panel: CustomPanelAPI,
        ship: FleetMemberAPI,
        contentHeight: Float,
        leftColumnWidth: Float,
        buttons: MutableList<ButtonBase<*>>,
        headingPanelsByIndex: MutableMap<Int, CustomPanelAPI>,
        callbacks: WeaponGroupPanelCallbacks,
    ) {
        val innerWidth = panel.position.width
        val innerHeight = panel.position.height
        val cardWidth = innerWidth / Values.MAX_WEAPON_GROUPS

        repeat(Values.MAX_WEAPON_GROUPS) { index ->
            val effectiveWidth = if (index == Values.MAX_WEAPON_GROUPS - 1) {
                innerWidth - cardWidth * (Values.MAX_WEAPON_GROUPS - 1)
            } else {
                cardWidth
            }
            val groupPanel = CampaignPanelFactory.addPanel(
                parent = panel,
                width = effectiveWidth,
                height = innerHeight,
                type = CampaignPanelType.WEAPON_GROUP_PANEL,
                x = index * cardWidth,
                y = 0f,
            )
            callbacks.recordGroupPanel(index, groupPanel)
            buildGroupPanel(
                panel = groupPanel,
                ship = ship,
                groupIndex = index,
                relativeGroupLeft = leftColumnWidth + index * cardWidth,
                relativeGroupTopOffset = 0f,
                contentHeight = contentHeight,
                buttons = buttons,
                headingPanelsByIndex = headingPanelsByIndex,
                callbacks = callbacks,
            )
        }
    }

    fun renderHeading(
        panel: CustomPanelAPI,
        groupIndex: Int,
        isDirty: Boolean,
        headingPanelsByIndex: MutableMap<Int, CustomPanelAPI>,
    ) {
        headingPanelsByIndex.remove(groupIndex)?.let(panel::removeComponent)
        val title = "Group ${groupIndex + 1}${if (isDirty) " (Unsaved)" else ""}"
        val heading = CampaignControlLabels.addCampaignPanelHeading(
            panel = panel,
            title = title,
            fillColor = if (isDirty) {
                CampaignGuiStyle.STALE_WEAPON_GROUP_PANEL_HEADING_COLOUR
            } else {
                CampaignGuiStyle.PANEL_HEADING_COLOUR
            },
        )
        headingPanelsByIndex[groupIndex] = heading
    }

    private fun buildGroupPanel(
        panel: CustomPanelAPI,
        ship: FleetMemberAPI,
        groupIndex: Int,
        relativeGroupLeft: Float,
        relativeGroupTopOffset: Float,
        contentHeight: Float,
        buttons: MutableList<ButtonBase<*>>,
        headingPanelsByIndex: MutableMap<Int, CustomPanelAPI>,
        callbacks: WeaponGroupPanelCallbacks,
    ) {
        val entries = weaponEntriesForGroup(ship, groupIndex)
        val isDirty = entries.isNotEmpty() && callbacks.isPresetDirty(ship, groupIndex)
        renderHeading(panel, groupIndex, isDirty, headingPanelsByIndex)
        if (entries.isEmpty()) {
            renderEmptyBody(panel)
            return
        }

        val layout = computeLayout(panel, groupIndex, callbacks)
        renderPresetControls(panel, groupIndex, layout, buttons, callbacks)
        addSectionHeading(
            panel = panel,
            title = "Weapons",
            id = weaponGroupWeaponsPanelId(groupIndex),
            top = layout.weaponsHeadingTop,
            buttons = buttons,
            callbacks = callbacks,
        )
        renderWeaponPanel(panel, entries, layout)
        renderTagListPanel(
            panel = panel,
            ship = ship,
            groupIndex = groupIndex,
            layout = layout,
            relativeGroupLeft = relativeGroupLeft,
            relativeGroupTopOffset = relativeGroupTopOffset,
            contentHeight = contentHeight,
            callbacks = callbacks,
        )
    }

    private fun addSectionHeading(
        panel: CustomPanelAPI,
        title: String,
        id: String,
        top: Float,
        buttons: MutableList<ButtonBase<*>>,
        callbacks: WeaponGroupPanelCallbacks,
        fillColor: Color = CampaignGuiStyle.COLLAPSIBLE_HEADING_COLOUR,
        statusSuffix: String = "",
    ): Pair<CustomPanelAPI, ButtonBase<*>> {
        val shell = CampaignControlLabels.addCollapsibleCampaignPanelHeading(
            panel = panel,
            title = title,
            collapsed = callbacks.isCollapsed(id),
            data = "collapse_panel:$id",
            top = top,
            fillColor = fillColor,
            statusSuffix = statusSuffix,
        )
        val control = CampaignButtonControls.addControl(buttons, button = shell.button) {
            callbacks.toggleCollapsed(id)
        }
        return shell.panel to control
    }

    private fun weaponEntriesForGroup(ship: FleetMemberAPI, groupIndex: Int): List<CampaignWeaponPanelEntry> {
        return getVariantWeaponGroup(ship, groupIndex)
            ?.let { CampaignWeaponPanelRenderer.aggregateWeapons(it, ship) }
            ?: emptyList()
    }

    private fun computeLayout(
        panel: CustomPanelAPI,
        groupIndex: Int,
        callbacks: WeaponGroupPanelCallbacks,
    ): WeaponGroupPanelLayout {
        val sectionHeadingHeight = CampaignGuiStyle.CONTAINER_HEADING_HEIGHT
        val innerWidth = panel.position.width - 2f * CampaignGuiStyle.PANEL_PADDING
        val saveLoadPanelTop = CampaignGuiStyle.PANEL_PADDING + CampaignGuiStyle.CONTAINER_HEADING_HEIGHT
        val weaponPanelVisible = !callbacks.isCollapsed(weaponGroupWeaponsPanelId(groupIndex))
        val weaponsHeadingTop = saveLoadPanelTop +
            CampaignSaveLoadPanelRenderer.COMPACT_PANEL_HEIGHT +
            CampaignWeaponPanelRenderer.WEAPON_TO_TAG_GAP
        val weaponPanelTop = weaponsHeadingTop + sectionHeadingHeight
        val baseWeaponPanelHeight = if (weaponPanelVisible) CampaignWeaponPanelRenderer.PANEL_HEIGHT else 0f
        val baseTagListPanelTop = weaponPanelTop + baseWeaponPanelHeight + CampaignWeaponPanelRenderer.WEAPON_TO_TAG_GAP
        val rawTagListPanelHeight = (panel.position.height - baseTagListPanelTop).coerceAtLeast(0f)
        val alignedTagListPanelHeight = if (rawTagListPanelHeight >= CampaignGuiStyle.TAG_ITEM_HEIGHT) {
            val tagSlotCount = computeVerticalSlotCount(rawTagListPanelHeight)
            computeVerticalItemsHeight(tagSlotCount)
                .coerceAtMost(rawTagListPanelHeight)
        } else {
            0f
        }
        val reclaimedHeight = if (weaponPanelVisible) max(0f, rawTagListPanelHeight - alignedTagListPanelHeight) else 0f
        val weaponPanelHeight = if (weaponPanelVisible) baseWeaponPanelHeight + reclaimedHeight else 0f
        val tagListPanelTop = baseTagListPanelTop + reclaimedHeight
        val rawTagListPanelHeightAfterReclaim = (panel.position.height - tagListPanelTop).coerceAtLeast(0f)
        val tagListPanelHeight = if (rawTagListPanelHeightAfterReclaim >= CampaignGuiStyle.TAG_ITEM_HEIGHT) {
            rawTagListPanelHeightAfterReclaim
        } else {
            0f
        }
        return WeaponGroupPanelLayout(
            innerWidth = innerWidth,
            saveLoadPanelTop = saveLoadPanelTop,
            weaponsHeadingTop = weaponsHeadingTop,
            weaponPanelTop = weaponPanelTop,
            weaponPanelHeight = weaponPanelHeight,
            weaponPanelVisible = weaponPanelVisible,
            tagListPanelTop = tagListPanelTop,
            tagListPanelHeight = tagListPanelHeight,
        )
    }

    private fun renderPresetControls(
        panel: CustomPanelAPI,
        groupIndex: Int,
        layout: WeaponGroupPanelLayout,
        buttons: MutableList<ButtonBase<*>>,
        callbacks: WeaponGroupPanelCallbacks,
    ) {
        val presetControlState = callbacks.presetStateForGroup(groupIndex)
        buttons.addAll(
            CampaignSaveLoadPanelRenderer.renderActionButtonsOnly(
                panel = panel,
                groupIndex = groupIndex,
                top = layout.saveLoadPanelTop,
                width = layout.innerWidth,
                state = presetControlState,
                onStateChanged = { state ->
                    callbacks.updatePresetState(groupIndex, state)
                },
                onRefreshRequested = {
                    callbacks.openPresetActionModal(groupIndex, callbacks.presetStateForGroup(groupIndex))
                },
            )
        )
    }

    private fun renderWeaponPanel(
        panel: CustomPanelAPI,
        entries: List<CampaignWeaponPanelEntry>,
        layout: WeaponGroupPanelLayout,
    ) {
        if (!layout.weaponPanelVisible || layout.weaponPanelHeight <= 0f) return
        val weaponPanel = CampaignPanelFactory.addPanel(
            parent = panel,
            width = layout.innerWidth,
            height = layout.weaponPanelHeight,
            type = CampaignPanelType.WEAPON_PANEL,
            x = CampaignGuiStyle.PANEL_PADDING,
            y = layout.weaponPanelTop,
        )
        CampaignWeaponPanelRenderer.render(weaponPanel, entries)
        if (entries.isNotEmpty()) {
            CampaignWeaponPanelRenderer.addWeaponEntriesTooltip(weaponPanel, entries)
        }
    }

    private fun renderEmptyBody(panel: CustomPanelAPI) {
        val bodyTop = CampaignGuiStyle.PANEL_PADDING + CampaignGuiStyle.CONTAINER_HEADING_HEIGHT
        val bodyHeight = max(0f, panel.position.height - bodyTop)
        if (bodyHeight <= 0f) return
        val body = panel.createCustomPanel(
            panel.position.width,
            bodyHeight,
            CampaignPanelPlugin(
                CampaignPanelType.WEAPON_PANEL,
                fillColor = Color(0, 0, 0, 235),
            )
        )
        panel.addComponent(body)
        body.position.inTL(0f, bodyTop)
    }

    private fun renderTagListPanel(
        panel: CustomPanelAPI,
        ship: FleetMemberAPI,
        groupIndex: Int,
        layout: WeaponGroupPanelLayout,
        relativeGroupLeft: Float,
        relativeGroupTopOffset: Float,
        contentHeight: Float,
        callbacks: WeaponGroupPanelCallbacks,
    ) {
        if (layout.tagListPanelHeight <= 0f) return
        val tagListPanel = CampaignPanelFactory.addPanel(
            parent = panel,
            width = layout.innerWidth,
            height = layout.tagListPanelHeight,
            type = CampaignPanelType.WEAPON_GROUP_TAG_LIST_PANEL,
            x = CampaignGuiStyle.PANEL_PADDING,
            y = layout.tagListPanelTop,
        )

        val relativeTopOffset = relativeGroupTopOffset + layout.tagListPanelTop
        val relativeBottom = contentHeight - relativeTopOffset - layout.tagListPanelHeight
        callbacks.buildTagList(
            tagListPanel,
            ship,
            groupIndex,
            relativeGroupLeft + CampaignGuiStyle.PANEL_PADDING,
            relativeGroupLeft + CampaignGuiStyle.PANEL_PADDING + layout.innerWidth,
            relativeBottom,
            relativeBottom + layout.tagListPanelHeight,
        )
    }

    private fun weaponGroupWeaponsPanelId(groupIndex: Int): String = "weapon_group:$groupIndex:weapons"
}
