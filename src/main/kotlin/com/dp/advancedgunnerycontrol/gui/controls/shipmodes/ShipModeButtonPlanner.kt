package com.dp.advancedgunnerycontrol.gui.controls.shipmodes

import com.dp.advancedgunnerycontrol.customlists.CustomShipModeListStore
import com.dp.advancedgunnerycontrol.gui.controls.scroll.computePinnedVerticalScrollLayout
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.dp.advancedgunnerycontrol.shipmodes.canonicalizeShipModeNames
import com.dp.advancedgunnerycontrol.shipmodes.isSupportedShipModeName
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import kotlin.math.max

internal object ShipModeButtonPlanner {
    private const val ACTIVE_MODE_SECTION = "Active Ship Modes"
    private const val INACTIVE_MODE_SECTION = "Inactive Ship Modes"
    private val collapsedCampaignModeSectionsByKey = mutableMapOf<String, MutableSet<String>>()

    fun prepareCampaignModeButtonGroup(
        ship: FleetMemberAPI,
        width: Float,
        availableHeight: Float,
        runtimeShip: ShipAPI? = null,
        rowOffset: Int = 0,
        configuredModesOverride: List<String>? = null,
        persistenceContext: ShipEditorPersistenceContext? = null,
        forceActiveSectionExpanded: Boolean = false,
    ): PreparedCampaignModeButtonGroup {
        val context = ShipModeSelectionStore.persistenceContext(ship, runtimeShip, persistenceContext)
        val persistedModeList = canonicalizeShipModeNames(ShipModeSelectionStore.loadModes(context))
        val configuredModes = configuredModesOverride
            ?: currentModeNamesForContext(context)
        return PreparedCampaignModeButtonGroup(
            persistedModeList = persistedModeList,
            plan = campaignModeButtonGroupPlan(
                sectionKey = context.shipId,
                configuredModes = configuredModes,
                width = width,
                containerHeight = availableHeight,
                rowOffset = rowOffset,
                persistedModes = persistedModeList.toSet(),
                forceActiveSectionExpanded = forceActiveSectionExpanded,
            )
        )
    }

    fun estimateCampaignModeButtonGroupTightHeight(
        ship: FleetMemberAPI,
        width: Float,
        availableHeight: Float,
        runtimeShip: ShipAPI? = null,
        rowOffset: Int = 0,
        preparedGroup: PreparedCampaignModeButtonGroup? = null,
    ): Float {
        val prepared = preparedGroup ?: prepareCampaignModeButtonGroup(
            ship = ship,
            width = width,
            availableHeight = availableHeight,
            runtimeShip = runtimeShip,
            rowOffset = rowOffset,
        )
        return prepared.tightHeight
            .coerceAtLeast(CampaignGuiStyle.SHIP_MODE_ITEM_HEIGHT)
            .coerceAtMost(availableHeight)
    }

    fun toggleSection(sectionKey: String, title: String, expanded: Boolean) {
        val collapsedSections = collapsedCampaignModeSectionsByKey.getOrPut(sectionKey) { mutableSetOf() }
        if (expanded) {
            collapsedSections.add(title)
        } else {
            collapsedSections.remove(title)
        }
    }

    private fun currentModeNamesForContext(
        context: ShipEditorPersistenceContext,
    ): List<String> =
        CustomShipModeListStore.effectiveModeNamesForShipOrDefault(
            context.shipId,
            Settings.getCurrentShipModeNames()
        )

    private fun campaignModeButtonGroupPlan(
        sectionKey: String,
        configuredModes: List<String>,
        width: Float,
        containerHeight: Float,
        rowOffset: Int,
        persistedModes: Set<String>,
        forceActiveSectionExpanded: Boolean,
    ): CampaignModeButtonGroupPlan {
        val activeOffListModes = persistedModes
            .filter(::isSupportedShipModeName)
            .filterNot { mode -> mode in configuredModes }
        val modes = configuredModes + activeOffListModes
        val columns = CampaignGuiStyle.SHIP_MODE_COLUMN_COUNT
        val itemWidth =
            (width - CampaignGuiStyle.SHIP_MODE_ITEM_HGAP * (columns - 1)) / columns
        val activeModes = modes.filter { it in persistedModes }
        val activeModeSet = activeModes.toSet()
        val inactiveModes = modes.filterNot { it in activeModeSet }
        val rows = campaignModeRows(
            sectionKey = sectionKey,
            activeModes = activeModes,
            inactiveModes = inactiveModes,
            columns = columns,
            forceActiveSectionExpanded = forceActiveSectionExpanded,
        )
        return CampaignModeButtonGroupPlan(
            sectionKey = sectionKey,
            columns = columns,
            itemWidth = itemWidth,
            activeModeSet = activeModeSet,
            layout = computePinnedVerticalScrollLayout(
                pinnedCandidates = emptyList(),
                scrollCandidates = rows,
                containerHeight = max(0f, containerHeight),
                currentOffset = rowOffset,
                itemHeight = CampaignGuiStyle.SHIP_MODE_ITEM_HEIGHT,
                verticalGap = CampaignGuiStyle.SHIP_MODE_ITEM_VGAP,
                indicatorHeight = CampaignGuiStyle.SHIP_MODE_ITEM_HEIGHT,
            )
        )
    }

    private fun campaignModeRows(
        sectionKey: String,
        activeModes: List<String>,
        inactiveModes: List<String>,
        columns: Int,
        forceActiveSectionExpanded: Boolean,
    ): List<CampaignModeListRow> {
        if (forceActiveSectionExpanded) {
            collapsedCampaignModeSectionsByKey[sectionKey]?.remove(ACTIVE_MODE_SECTION)
        }
        val collapsedSections = collapsedCampaignModeSectionsByKey[sectionKey].orEmpty()
        val activeExpanded = ACTIVE_MODE_SECTION !in collapsedSections
        val inactiveExpanded = INACTIVE_MODE_SECTION !in collapsedSections
        val rows = mutableListOf<CampaignModeListRow>()
        rows.add(CampaignModeListRow.Heading(ACTIVE_MODE_SECTION, activeExpanded, activeModes.isNotEmpty()))
        if (activeExpanded) {
            activeModes.chunked(columns).forEach { rows.add(CampaignModeListRow.Modes(it)) }
        }
        rows.add(CampaignModeListRow.Heading(INACTIVE_MODE_SECTION, inactiveExpanded, active = false))
        if (inactiveExpanded) {
            inactiveModes.chunked(columns).forEach { rows.add(CampaignModeListRow.Modes(it)) }
        }
        return rows
    }
}
