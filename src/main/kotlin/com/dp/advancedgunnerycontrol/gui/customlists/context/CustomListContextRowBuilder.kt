package com.dp.advancedgunnerycontrol.gui.customlists.context

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext

internal class CustomListContextRowBuilder(
    private val managerState: CustomListManagerState,
    private val changeReviewState: CustomListChangeReviewState,
    private val currentValues: CustomListCurrentValuesProvider,
) {
    private var archetypeCache: List<CustomTagArchetype>? = null

    fun managerRowsFor(context: ShipEditorPersistenceContext): List<CustomTagManagerRow> {
        if (context.shipId.isBlank()) return emptyList()
        val currentTags = currentValues.currentTagsFor(context)
        val (pendingAdditions, pendingRemovals, pendingEdits) = managerState.normalizeStagedState(currentTags)
        val currentShipModes = currentValues.currentShipModesFor(context)
        managerState.normalizeShipModeStagedState(currentShipModes)
        return CustomTagManagerRows.build(
            currentTags = currentTags,
            pendingAdditions = pendingAdditions,
            pendingRemovals = pendingRemovals,
            pendingEdits = pendingEdits,
            currentShipModes = currentShipModes,
            pendingShipModeAdditions = managerState.shipModeAdditions(),
            pendingShipModeRemovals = managerState.shipModeRemovals(),
            pendingShipModeEdits = managerState.shipModeEdits(),
            expandedListSections = managerState.expandedListSections(),
            expandedCategories = managerState.expandedCategories(),
            expandedArchetypes = managerState.expandedArchetypes(),
            archetypes = archetypes(),
        )
    }

    fun changeReviewRowsFor(context: ShipEditorPersistenceContext): List<CustomTagChangeReviewRow> {
        if (context.shipId.isBlank()) {
            return CustomTagChangeReviewSection.entries.map { section ->
                CustomTagChangeReviewRow.Heading(
                    id = section.idFor(CustomListDraftKeys.ListSections.TAGS),
                    section = section,
                    count = 0,
                    expanded = true,
                )
            }
        }
        val currentTags = currentValues.currentTagsFor(context)
        val currentShipModes = currentValues.currentShipModesFor(context)
        val (additions, removals, edits) = changeReviewState.normalizeStagedState(currentTags)
        val shipModeStagedState = changeReviewState.normalizeShipModeStagedState(currentShipModes)
        return CustomListChangeReviewListRenderer.buildRows(
            additions = additions,
            edits = edits,
            removals = removals.toList(),
            shipModeAdditions = shipModeStagedState.additions,
            shipModeEdits = shipModeStagedState.edits,
            shipModeRemovals = shipModeStagedState.removals.toList(),
            state = changeReviewState,
        )
    }

    fun hasPendingRemovals(context: ShipEditorPersistenceContext): Boolean {
        val currentTags = currentValues.currentTagsFor(context)
        val currentShipModes = currentValues.currentShipModesFor(context)
        val tagStagedState = changeReviewState.normalizeStagedState(currentTags)
        val shipModeStagedState = changeReviewState.normalizeShipModeStagedState(currentShipModes)
        return tagStagedState.removals.isNotEmpty() || shipModeStagedState.removals.isNotEmpty()
    }

    private fun archetypes(): List<CustomTagArchetype> =
        archetypeCache ?: CustomTagAddCandidateBuilder.completeTagListArchetypes()
            .also { archetypeCache = it }
}
