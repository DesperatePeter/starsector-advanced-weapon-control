package com.dp.advancedgunnerycontrol.gui.customlists.manager

import com.dp.advancedgunnerycontrol.gui.controls.text.CampaignControlLabels

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


internal object CustomTagManagerRows {
    fun status(row: CustomTagManagerRow.TagEntry): CustomTagManagerTagStatus {
        return when {
            row.pendingAddition -> CustomTagManagerTagStatus.ADDING
            row.pendingEdit && !row.markedForRemoval -> CustomTagManagerTagStatus.EDITING
            row.markedForRemoval -> CustomTagManagerTagStatus.REMOVING
            else -> CustomTagManagerTagStatus.NORMAL
        }
    }

    fun shipModeStatus(row: CustomTagManagerRow.ShipModeEntry): CustomTagManagerTagStatus {
        return when {
            row.pendingAddition -> CustomTagManagerTagStatus.ADDING
            row.pendingEdit && !row.markedForRemoval -> CustomTagManagerTagStatus.EDITING
            row.markedForRemoval -> CustomTagManagerTagStatus.REMOVING
            else -> CustomTagManagerTagStatus.NORMAL
        }
    }

    fun tagLabel(tag: String, status: CustomTagManagerTagStatus): String {
        val displayTag = EditableWeaponTagDefinitions.displayName(tag)
        return if (status.suffix.isBlank()) displayTag else "$displayTag ${status.suffix}"
    }

    fun shipModeLabel(mode: String, status: CustomTagManagerTagStatus): String {
        val displayMode = shipModeDisplayName(mode)
        return if (status.suffix.isBlank()) displayMode else "$displayMode ${status.suffix}"
    }

    fun highlightTokens(status: CustomTagManagerTagStatus): List<String> {
        return status.suffix.takeIf { it.isNotBlank() }?.let(::listOf).orEmpty()
    }

    fun highlightTokens(label: String, status: CustomTagManagerTagStatus): List<String> {
        return CampaignControlLabels.hiddenChangeHighlightTokens(label, highlightTokens(status))
    }

    fun tagTooltip(
        row: CustomTagManagerRow.TagEntry,
        status: CustomTagManagerTagStatus,
        editSourceTag: String?,
    ): String {
        return when (status) {
            CustomTagManagerTagStatus.ADDING -> "Remove pending addition ${row.tag}."
            CustomTagManagerTagStatus.EDITING -> "Revert edit to ${editSourceTag ?: row.tag}."
            CustomTagManagerTagStatus.REMOVING -> "Unmark ${row.tag} for removal."
            CustomTagManagerTagStatus.NORMAL -> "Mark ${row.tag} for removal."
        }
    }

    fun shipModeTooltip(
        row: CustomTagManagerRow.ShipModeEntry,
        status: CustomTagManagerTagStatus,
    ): String {
        val displayMode = shipModeDisplayName(row.mode)
        return when (status) {
            CustomTagManagerTagStatus.ADDING -> "Remove pending ship-mode addition $displayMode."
            CustomTagManagerTagStatus.EDITING -> "Revert edit to $displayMode."
            CustomTagManagerTagStatus.REMOVING -> "Unmark $displayMode for removal."
            CustomTagManagerTagStatus.NORMAL -> "Mark $displayMode for removal."
        }
    }

    fun build(
        currentTags: List<String>,
        pendingAdditions: List<String>,
        pendingRemovals: Set<String>,
        pendingEdits: Map<String, String>,
        currentShipModes: List<String> = emptyList(),
        pendingShipModeAdditions: List<String> = emptyList(),
        pendingShipModeRemovals: Set<String> = emptySet(),
        pendingShipModeEdits: Map<String, String> = emptyMap(),
        expandedListSections: Set<String> = setOf(CustomListDraftKeys.ListSections.TAGS),
        expandedCategories: Set<String>,
        expandedArchetypes: Set<String>,
        archetypes: List<CustomTagArchetype>,
    ): List<CustomTagManagerRow> {
        val rows = mutableListOf<CustomTagManagerRow>()
        CustomTagSectionRows.addRows(
            rows = rows,
            currentTags = currentTags,
            pendingAdditions = pendingAdditions,
            pendingRemovals = pendingRemovals,
            pendingEdits = pendingEdits,
            expandedListSections = expandedListSections,
            expandedCategories = expandedCategories,
            expandedArchetypes = expandedArchetypes,
            archetypes = archetypes,
        )
        CustomShipModeSectionRows.addRows(
            rows = rows,
            currentShipModes = currentShipModes,
            pendingAdditions = pendingShipModeAdditions,
            pendingRemovals = pendingShipModeRemovals,
            pendingEdits = pendingShipModeEdits,
            expandedListSections = expandedListSections,
        )
        return rows
    }
}
