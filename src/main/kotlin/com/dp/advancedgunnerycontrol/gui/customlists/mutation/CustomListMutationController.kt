package com.dp.advancedgunnerycontrol.gui.customlists.mutation

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.gui.*


import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.fs.starfarer.api.fleet.FleetMemberAPI

internal class CustomListMutationController(
    private val state: CustomListModalStateController,
    private val contextController: CustomListContextController,
    private val editSource: () -> Pair<Int, String>?,
    private val activeShip: () -> FleetMemberAPI?,
    private val onMutationFinished: (FleetMemberAPI) -> Unit,
) {
    fun applyManagerChanges(
        ship: FleetMemberAPI,
        context: ShipEditorPersistenceContext,
    ) {
        if (context.shipId.isBlank()) return
        val currentTags = contextController.currentTagsFor(context)
        val currentShipModes = contextController.currentShipModesFor(context)
        val stagedState = state.normalizeStagedState(currentTags)
        val shipModeStagedState = state.normalizeShipModeStagedState(currentShipModes)
        if (CustomTagMutations.applyManagerChanges(ship, context, stagedState, shipModeStagedState)) {
            onMutationFinished(ship)
        }
    }

    fun addCustomTag(
        ship: FleetMemberAPI,
        context: ShipEditorPersistenceContext,
        tag: String,
        replaceEditSourceIfActive: Boolean = true,
    ) {
        val source = editSource().takeIf { replaceEditSourceIfActive }
        if (CustomTagMutations.addCustomTag(ship, context, tag, source)) {
            onMutationFinished(ship)
        }
    }

    fun replaceCustomTag(
        ship: FleetMemberAPI,
        context: ShipEditorPersistenceContext,
        sourceTag: String,
        editedTag: String,
    ) {
        if (CustomTagMutations.replaceCustomTag(ship, context, sourceTag, editedTag)) {
            onMutationFinished(ship)
        }
    }

    fun removeCustomTags(
        ship: FleetMemberAPI,
        context: ShipEditorPersistenceContext,
        tags: Set<String>,
    ) {
        if (CustomTagMutations.removeCustomTags(context, tags)) {
            onMutationFinished(ship)
        }
    }

    fun addCustomShipMode(
        context: ShipEditorPersistenceContext,
        mode: String,
    ) {
        val ship = activeShip() ?: return
        if (CustomTagMutations.addCustomShipMode(context, mode)) {
            onMutationFinished(ship)
        }
    }

    fun replaceCustomShipMode(
        context: ShipEditorPersistenceContext,
        sourceMode: String,
        editedMode: String,
    ) {
        val ship = activeShip() ?: return
        if (CustomTagMutations.replaceCustomShipMode(context, sourceMode, editedMode)) {
            onMutationFinished(ship)
        }
    }

    fun removeCustomShipModes(
        context: ShipEditorPersistenceContext,
        modes: Set<String>,
    ) {
        val ship = activeShip() ?: return
        if (CustomTagMutations.removeCustomShipModes(context, modes)) {
            onMutationFinished(ship)
        }
    }
}
