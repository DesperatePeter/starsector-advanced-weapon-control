package com.dp.advancedgunnerycontrol.gui.customlists.mutation

import com.dp.advancedgunnerycontrol.gui.controls.shipmodes.ShipModeButton
import com.dp.advancedgunnerycontrol.gui.controls.weapontags.TagButton

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

import com.dp.advancedgunnerycontrol.gui.entrypoints.AGCGUI

import com.dp.advancedgunnerycontrol.gui.*


import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.fs.starfarer.api.fleet.FleetMemberAPI

internal object CustomTagMutations {
    fun applyManagerChanges(
        ship: FleetMemberAPI,
        context: ShipEditorPersistenceContext,
        stagedState: CustomTagManagerStagedState,
        shipModeStagedState: CustomShipModeManagerStagedState = CustomShipModeManagerStagedState(emptyList(), emptySet()),
    ): Boolean {
        val shipId = context.shipId.takeIf { it.isNotBlank() } ?: return false
        val (additions, removals, edits) = stagedState
        val (shipModeAdditions, shipModeRemovals, shipModeEdits) = shipModeStagedState
        edits.forEach { (source, edited) ->
            CustomWeaponTagListStore.removeCustomTags(shipId, listOf(source))
            CustomWeaponTagListStore.addCustomTags(shipId, listOf(edited))
            replaceTagAcrossAllShipLoadouts(ship, context, source, edited)
        }
        if (additions.isNotEmpty()) {
            CustomWeaponTagListStore.addCustomTags(shipId, additions)
            Settings.hotAddTags(additions)
        }
        if (removals.isNotEmpty()) {
            CustomWeaponTagListStore.removeCustomTags(shipId, removals.toList())
        }
        shipModeEdits.forEach { (source, edited) ->
            CustomShipModeListStore.removeCustomShipModesFromCustomList(shipId, listOf(source))
            CustomShipModeListStore.addCustomShipModesToCustomList(shipId, listOf(edited))
            replaceShipModeAcrossAllLoadouts(context, source, edited)
        }
        if (shipModeAdditions.isNotEmpty()) {
            CustomShipModeListStore.addCustomShipModesToCustomList(shipId, shipModeAdditions)
        }
        if (shipModeRemovals.isNotEmpty()) {
            CustomShipModeListStore.removeCustomShipModesFromCustomList(shipId, shipModeRemovals.toList())
        }
        if (additions.isNotEmpty() || removals.isNotEmpty() || edits.isNotEmpty() ||
            shipModeAdditions.isNotEmpty() || shipModeRemovals.isNotEmpty()
                || shipModeEdits.isNotEmpty()
        ) {
            ensureCustomModeActive(shipId)
        }
        Settings.hotAddTags(CustomWeaponTagListStore.getSupportedCustomTags(shipId))
        return true
    }

    fun addCustomTag(
        ship: FleetMemberAPI,
        context: ShipEditorPersistenceContext,
        tag: String,
        editSource: Pair<Int, String>? = null,
    ): Boolean {
        val shipId = context.shipId.takeIf { it.isNotBlank() } ?: return false
        CustomWeaponTagListStore.addCustomTags(shipId, listOf(tag))
        ensureCustomModeActive(shipId)
        Settings.hotAddTags(listOf(tag))
        if (editSource != null) {
            replaceRightClickEditSourceIfActive(ship, context, editSource, tag)
        }
        return true
    }

    fun replaceCustomTag(
        ship: FleetMemberAPI,
        context: ShipEditorPersistenceContext,
        sourceTag: String,
        editedTag: String,
    ): Boolean {
        val shipId = context.shipId.takeIf { it.isNotBlank() } ?: return false
        val canonicalSource = canonicalTag(sourceTag)
        val canonicalEdited = canonicalTag(editedTag)
        CustomWeaponTagListStore.removeCustomTags(shipId, listOf(canonicalSource))
        CustomWeaponTagListStore.addCustomTags(shipId, listOf(canonicalEdited))
        ensureCustomModeActive(shipId)
        replaceTagAcrossAllShipLoadouts(ship, context, canonicalSource, canonicalEdited)
        Settings.hotAddTags(CustomWeaponTagListStore.getSupportedCustomTags(shipId))
        return true
    }

    fun removeCustomTags(
        context: ShipEditorPersistenceContext,
        tags: Set<String>,
    ): Boolean {
        val shipId = context.shipId.takeIf { it.isNotBlank() } ?: return false
        val canonicalRemovals = canonicalizeWeaponTagNames(tags.toList()).toSet()
        val removesFromCustomList = canonicalizeWeaponTagNames(CustomWeaponTagListStore.getSupportedCustomTags(shipId))
            .any { it in canonicalRemovals }
        CustomWeaponTagListStore.removeCustomTags(shipId, canonicalRemovals.toList())
        if (removesFromCustomList) {
            ensureCustomModeActive(shipId)
        }
        Settings.hotAddTags(CustomWeaponTagListStore.getSupportedCustomTags(shipId))
        return true
    }

    fun addCustomShipMode(
        context: ShipEditorPersistenceContext,
        mode: String,
    ): Boolean {
        val shipId = context.shipId.takeIf { it.isNotBlank() } ?: return false
        CustomShipModeListStore.addCustomShipModesToCustomList(shipId, listOf(mode))
        ensureCustomModeActive(shipId)
        ShipModeButton.notifyCampaignShipModeSelectionChanged()
        return true
    }

    fun replaceCustomShipMode(
        context: ShipEditorPersistenceContext,
        sourceMode: String,
        editedMode: String,
    ): Boolean {
        val shipId = context.shipId.takeIf { it.isNotBlank() } ?: return false
        val canonicalSource = canonicalizeShipModeName(sourceMode)
        val canonicalEdited = canonicalizeShipModeName(editedMode)
        CustomShipModeListStore.removeCustomShipModesFromCustomList(shipId, listOf(canonicalSource))
        CustomShipModeListStore.addCustomShipModesToCustomList(shipId, listOf(canonicalEdited))
        ensureCustomModeActive(shipId)
        replaceShipModeAcrossAllLoadouts(context, canonicalSource, canonicalEdited)
        return true
    }

    fun removeCustomShipModes(
        context: ShipEditorPersistenceContext,
        modes: Set<String>,
    ): Boolean {
        val shipId = context.shipId.takeIf { it.isNotBlank() } ?: return false
        val canonicalModes = canonicalizeShipModeNames(modes.toList()).toSet()
        CustomShipModeListStore.removeCustomShipModesFromCustomList(shipId, canonicalModes.toList())
        if (canonicalModes.isNotEmpty()) {
            ensureCustomModeActive(shipId)
        }
        ShipModeButton.notifyCampaignShipModeSelectionChanged()
        return true
    }

    private fun replaceTagAcrossAllShipLoadouts(
        ship: FleetMemberAPI,
        context: ShipEditorPersistenceContext,
        sourceTag: String,
        editedTag: String,
    ) {
        for (loadoutIndex in 0 until Settings.maxLoadouts()) {
            for (groupIndex in 0 until Values.MAX_WEAPON_GROUPS) {
                val currentTags = canonicalizeWeaponTagNames(context.loadWeaponTags(groupIndex, loadoutIndex))
                if (sourceTag !in currentTags) continue
                val tagsWithoutSource = currentTags.filterNot { it == sourceTag }
                val updatedTags = if (
                    !shouldTagBeDisabled(groupIndex, ship, editedTag) &&
                    !isIncompatibleWithExistingTags(editedTag, tagsWithoutSource)
                ) {
                    canonicalizeWeaponTagNames(tagsWithoutSource + editedTag)
                } else {
                    tagsWithoutSource
                }
                if (updatedTags != currentTags) {
                    context.saveWeaponTags(groupIndex, loadoutIndex, updatedTags)
                }
            }
        }
        TagButton.notifyCampaignTagSelectionChanged()
    }

    private fun ensureCustomModeActive(shipId: String) {
        if (!CustomWeaponTagListStore.getActiveMode(shipId).isCustom) {
            CustomWeaponTagListStore.setActiveMode(shipId, WeaponTagListMode.CUSTOM)
        }
    }

    private fun replaceShipModeAcrossAllLoadouts(
        context: ShipEditorPersistenceContext,
        sourceMode: String,
        editedMode: String,
    ) {
        val canonicalSource = canonicalizeShipModeName(sourceMode)
        val canonicalEdited = canonicalizeShipModeName(editedMode)
        for (loadoutIndex in 0 until Settings.maxLoadouts()) {
            val currentModes = canonicalizeShipModeNames(context.loadModes(loadoutIndex))
            if (canonicalSource !in currentModes) continue
            val updatedModes = canonicalizeShipModeNames(
                currentModes.filterNot { it == canonicalSource } + canonicalEdited
            )
            if (updatedModes != currentModes) {
                context.saveModes(loadoutIndex, updatedModes)
            }
        }
        ShipModeButton.notifyCampaignShipModeSelectionChanged()
    }

    private fun replaceRightClickEditSourceIfActive(
        ship: FleetMemberAPI,
        context: ShipEditorPersistenceContext,
        editSource: Pair<Int, String>,
        editedTag: String,
    ) {
        val (groupIndex, sourceTag) = editSource
        val canonicalSource = canonicalTag(sourceTag)
        val canonicalEdited = canonicalTag(editedTag)
        val currentTags = canonicalizeWeaponTagNames(context.loadWeaponTags(groupIndex, AGCGUI.storageIndex))
        if (canonicalSource !in currentTags) return

        val tagsWithoutSource = currentTags.filterNot { it == canonicalSource }
        if (shouldTagBeDisabled(groupIndex, ship, canonicalEdited)) return
        if (isIncompatibleWithExistingTags(canonicalEdited, tagsWithoutSource)) return

        val updatedTags = canonicalizeWeaponTagNames(tagsWithoutSource + canonicalEdited)
        if (updatedTags == currentTags) return
        context.saveWeaponTags(groupIndex, AGCGUI.storageIndex, updatedTags)
        TagButton.notifyCampaignTagSelectionChanged()
    }

    private fun canonicalTag(tag: String): String = canonicalizeWeaponTagName(tag)
}
