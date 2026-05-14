package com.dp.advancedgunnerycontrol.gui.presets

import com.dp.advancedgunnerycontrol.gui.modals.CampaignHighlightedText
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.dp.advancedgunnerycontrol.presets.WeaponPresetScope
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI

internal object PresetConfirmationBodyCopy {
    fun body(
        groupIndex: Int,
        state: PresetControlState,
        ship: FleetMemberAPI?,
        runtimeShip: ShipAPI?,
        activePersistenceContext: ShipEditorPersistenceContext?,
        loadoutIndex: Int,
        presetPeekCache: PresetPeekCache? = null,
    ): List<CampaignHighlightedText> {
        return when (state.pendingAction) {
            PendingPresetAction.SAVE -> saveBody(groupIndex, state, ship, runtimeShip, activePersistenceContext, loadoutIndex)
            PendingPresetAction.LOAD -> loadBody(groupIndex, state, ship, loadoutIndex, presetPeekCache)
            null -> listOf(CampaignHighlightedText("No preset action is currently pending."))
        }
    }

    private fun saveBody(
        groupIndex: Int,
        state: PresetControlState,
        ship: FleetMemberAPI?,
        runtimeShip: ShipAPI?,
        activePersistenceContext: ShipEditorPersistenceContext?,
        loadoutIndex: Int,
    ): List<CampaignHighlightedText> {
        val weapons = PresetConfirmationDetails.weaponSummary(ship, groupIndex)
        val tags = PresetConfirmationDetails.currentTagSummary(
            ship,
            runtimeShip,
            activePersistenceContext,
            groupIndex,
            loadoutIndex,
        )
        val context = PresetConfirmationDetails.saveContextPhrase(ship, weapons, state.scope, loadoutIndex)
        val preset = PresetConfirmationDetails.presetDescriptor(state.scope, state.backend)
        val loadout = PresetConfirmationDetails.loadoutPhraseOrNull(state.scope, loadoutIndex)
        val paragraph = if (groupIndex == CampaignSaveLoadPanelRenderer.ALL_WEAPON_GROUPS_INDEX) {
            allGroupsSaveText(ship, state, preset, loadout)
        } else {
            weaponGroupSaveText(context, tags, preset)
        }
        val body = mutableListOf(PresetConfirmationDetails.highlightPresetParagraph(paragraph))
        if (state.overwrite && state.scope != WeaponPresetScope.SUGGESTED) {
            body.add(PresetConfirmationDetails.overwriteScopeParagraph(ship, groupIndex, state.scope))
        }
        return body
    }

    private fun loadBody(
        groupIndex: Int,
        state: PresetControlState,
        ship: FleetMemberAPI?,
        loadoutIndex: Int,
        presetPeekCache: PresetPeekCache? = null,
    ): List<CampaignHighlightedText> {
        val weapons = PresetConfirmationDetails.weaponSummary(ship, groupIndex)
        val tags = PresetConfirmationDetails.savedTagSummary(ship, groupIndex, state, loadoutIndex, presetPeekCache)
        val context = PresetConfirmationDetails.loadContextPhrase(ship, weapons, state.scope, loadoutIndex)
        val preset = PresetConfirmationDetails.presetDescriptor(state.scope, state.backend)
        val loadout = PresetConfirmationDetails.loadoutPhraseOrNull(state.scope, loadoutIndex)
        val paragraph = if (groupIndex == CampaignSaveLoadPanelRenderer.ALL_WEAPON_GROUPS_INDEX) {
            allGroupsLoadText(ship, state, preset, loadout)
        } else {
            weaponGroupLoadText(context, tags, preset, groupIndex, loadout)
        }
        return listOf(PresetConfirmationDetails.highlightPresetParagraph(paragraph))
    }

    private fun weaponGroupSaveText(
        context: String,
        tags: String,
        preset: String,
    ): String {
        return "Confirming will, for $context, save this weapon group's current tags ($tags) as the associated $preset tag preset."
    }

    private fun weaponGroupLoadText(
        context: String,
        tags: String,
        preset: String,
        groupIndex: Int,
        loadout: String?,
    ): String {
        return "Confirming will, for $context, load the associated $preset tag preset ($tags) " +
            "into this weapon group (#${groupIndex + 1})${PresetConfirmationDetails.loadoutTargetSuffix(loadout)}."
    }

    private fun allGroupsSaveText(
        ship: FleetMemberAPI?,
        state: PresetControlState,
        preset: String,
        loadout: String?,
    ): String {
        return when (state.scope) {
            WeaponPresetScope.SUGGESTED ->
                "Confirming will, for all eligible single-weapon groups, save each weapon group's current tags as the associated $preset tag preset for that weapon type. Groups with multiple weapon types will be skipped."
            else ->
                "Confirming will, for ${PresetConfirmationDetails.allGroupsContextPhrase(ship, state.scope, loadout)}, save each weapon group's current tags as the associated $preset tag preset for that group's weapon combination."
        }
    }

    private fun allGroupsLoadText(
        ship: FleetMemberAPI?,
        state: PresetControlState,
        preset: String,
        loadout: String?,
    ): String {
        return when (state.scope) {
            WeaponPresetScope.SUGGESTED ->
                "Confirming will, for all weapon groups, load all matching associated $preset tag presets into the appropriate weapon groups. Groups without matching saved presets will be left unchanged."
            else ->
                "Confirming will, for ${PresetConfirmationDetails.allGroupsContextPhrase(ship, state.scope, loadout)}, load all matching associated $preset tag presets into the appropriate weapon groups. Groups without matching saved presets will be left unchanged."
        }
    }
}
