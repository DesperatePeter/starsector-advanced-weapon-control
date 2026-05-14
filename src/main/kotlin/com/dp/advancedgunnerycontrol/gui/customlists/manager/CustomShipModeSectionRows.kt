package com.dp.advancedgunnerycontrol.gui.customlists.manager

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*


internal object CustomShipModeSectionRows {
    fun addRows(
        rows: MutableList<CustomTagManagerRow>,
        currentShipModes: List<String>,
        pendingAdditions: List<String>,
        pendingRemovals: Set<String>,
        pendingEdits: Map<String, String>,
        expandedListSections: Set<String>,
    ) {
        val stagedModes = stagedCustomShipModes(
            currentModes = currentShipModes,
            pendingAdditions = pendingAdditions,
            pendingRemovals = pendingRemovals,
            pendingEdits = pendingEdits,
        )
        val visibleModes = canonicalizeShipModeNames(stagedModes + pendingRemovals)
        val expanded = CustomListDraftKeys.ListSections.SHIP_MODES in expandedListSections
        rows += CustomTagManagerRow.ListSectionHeading(
            id = CustomListDraftKeys.ListSections.SHIP_MODES,
            label = CustomTagManagerSectionLabels.SHIP_MODES,
            expanded = expanded,
            active = visibleModes.isNotEmpty(),
            count = visibleModes.size,
        )
        if (!expanded) return

        val pendingAdditionsSet = pendingAdditions.toSet()
        val pendingEditsSet = pendingEdits.values.toSet()
        for (mode in sortedShipModes(visibleModes, pendingAdditionsSet, pendingRemovals, pendingEditsSet)) {
            rows += CustomTagManagerRow.ShipModeEntry(
                mode = mode,
                markedForRemoval = mode in pendingRemovals,
                pendingAddition = mode in pendingAdditionsSet,
                pendingEdit = mode in pendingEditsSet,
            )
        }

        val visibleSet = visibleModes.toSet()
        for (definition in EditableShipModeDefinitions.definitions) {
            if (!shouldOfferShipModeDefinitionAddRow(definition, visibleSet)) continue
            rows += CustomTagManagerRow.AddShipModeEntry(
                mode = definition.templateTag,
                definition = definition,
            )
        }
        for (modeName in fullShipModeNames()) {
            val mode = canonicalizeShipModeName(modeName)
            if (!shouldOfferDirectShipModeAddRow(mode, visibleSet)) continue
            rows += CustomTagManagerRow.AddShipModeEntry(mode)
        }
    }

    private fun shouldOfferDirectShipModeAddRow(mode: String, visibleModes: Set<String>): Boolean {
        return canonicalizeShipModeName(mode) !in visibleModes
    }

    private fun shouldOfferShipModeDefinitionAddRow(
        definition: EditableWeaponTagDefinition,
        visibleModes: Set<String>,
    ): Boolean {
        if (definition.parameters.isNotEmpty()) return true
        val canonicalDefault = definition.buildCanonicalTag(EditableWeaponTagDefinitions.defaultValuesFor(definition)).canonicalTag
            ?.let(::canonicalizeShipModeName)
            ?: return true
        return canonicalDefault !in visibleModes
    }

    private fun sortedShipModes(
        modes: List<String>,
        pendingAdditions: Set<String>,
        pendingRemovals: Set<String>,
        pendingEdits: Set<String>,
    ): List<String> {
        val sorted = modes.toMutableList()
        for (i in 1 until sorted.size) {
            val value = sorted[i]
            var j = i - 1
            while (j >= 0 && compareShipModes(sorted[j], value, pendingAdditions, pendingRemovals, pendingEdits) > 0) {
                sorted[j + 1] = sorted[j]
                j--
            }
            sorted[j + 1] = value
        }
        return sorted
    }

    private fun compareShipModes(
        left: String,
        right: String,
        pendingAdditions: Set<String>,
        pendingRemovals: Set<String>,
        pendingEdits: Set<String>,
    ): Int {
        fun rank(mode: String): Int = when {
            mode in pendingAdditions -> CustomTagManagerTagStatus.ADDING.sortRank
            mode in pendingEdits -> CustomTagManagerTagStatus.EDITING.sortRank
            mode in pendingRemovals -> CustomTagManagerTagStatus.REMOVING.sortRank
            else -> CustomTagManagerTagStatus.NORMAL.sortRank
        }
        val leftRank = rank(left)
        val rightRank = rank(right)
        if (leftRank != rightRank) return leftRank.compareTo(rightRank)
        return left.compareTo(right)
    }
}
