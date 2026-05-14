package com.dp.advancedgunnerycontrol.gui.presets

import com.dp.advancedgunnerycontrol.gui.modals.CampaignHighlightedText
import com.dp.advancedgunnerycontrol.gui.modals.confirmationFooterText
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.dp.advancedgunnerycontrol.presets.WeaponCompositionPresetPeekStatus
import com.dp.advancedgunnerycontrol.presets.WeaponPresetScope
import com.dp.advancedgunnerycontrol.presets.sanitizeWeaponCompositionPresetTagsForGroup
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI

internal object PresetConfirmationReviewCopy {
    fun reviewBody(
        groupIndex: Int,
        state: PresetControlState,
        ship: FleetMemberAPI?,
        runtimeShip: ShipAPI?,
        activePersistenceContext: ShipEditorPersistenceContext?,
        loadoutIndex: Int,
        presetPeekCache: PresetPeekCache? = null,
    ): PresetActionReviewBody? {
        if (groupIndex == CampaignSaveLoadPanelRenderer.ALL_WEAPON_GROUPS_INDEX) return null
        return when (state.pendingAction) {
            PendingPresetAction.SAVE -> saveReviewBody(
                groupIndex,
                state,
                ship,
                runtimeShip,
                activePersistenceContext,
                loadoutIndex,
            )
            PendingPresetAction.LOAD -> loadReviewBody(groupIndex, state, ship, loadoutIndex, presetPeekCache)
            null -> null
        }
    }

    private fun saveReviewBody(
        groupIndex: Int,
        state: PresetControlState,
        ship: FleetMemberAPI?,
        runtimeShip: ShipAPI?,
        activePersistenceContext: ShipEditorPersistenceContext?,
        loadoutIndex: Int,
    ): PresetActionReviewBody {
        val preset = PresetConfirmationDetails.presetDescriptor(state.scope, state.backend)
        val eligibleGroups = saveReviewGroupIndexes(ship, groupIndex, state.scope)
        val extra = mutableListOf<CampaignHighlightedText>()
        if (state.overwrite && state.scope != WeaponPresetScope.SUGGESTED) {
            extra.add(PresetConfirmationDetails.overwriteScopeParagraph(ship, groupIndex, state.scope))
        }
        return PresetActionReviewBody(
            intro = PresetConfirmationDetails.highlightPresetParagraph("Confirming will, for the following set of weapon types:"),
            weaponRows = reviewWeaponRows(ship, eligibleGroups),
            tagIntro = CampaignHighlightedText("Save the following tags:"),
            tagRows = saveReviewTagRows(ship, runtimeShip, activePersistenceContext, eligibleGroups, loadoutIndex),
            closing = PresetConfirmationDetails.highlightPresetParagraph("as the associated $preset tag preset."),
            extraParagraphs = extra,
            footer = confirmationFooterText(),
        )
    }

    private fun loadReviewBody(
        groupIndex: Int,
        state: PresetControlState,
        ship: FleetMemberAPI?,
        loadoutIndex: Int,
        presetPeekCache: PresetPeekCache?,
    ): PresetActionReviewBody {
        val preset = PresetConfirmationDetails.presetDescriptor(state.scope, state.backend)
        val groups = loadReviewGroupIndexes(ship, groupIndex)
        val loadout = PresetConfirmationDetails.loadoutPhraseOrNull(state.scope, loadoutIndex)
        return PresetActionReviewBody(
            intro = PresetConfirmationDetails.highlightPresetParagraph("Confirming will, for the following set of weapon types:"),
            weaponRows = reviewWeaponRows(ship, groups),
            tagIntro = PresetConfirmationDetails.highlightPresetParagraph("Load the associated $preset tag preset:"),
            tagRows = loadReviewTagRows(ship, groups, state, loadoutIndex, presetPeekCache),
            closing = PresetConfirmationDetails.highlightPresetParagraph(
                "into this weapon group (#${groupIndex + 1})${PresetConfirmationDetails.loadoutTargetSuffix(loadout)}."
            ),
            extraParagraphs = emptyList(),
            footer = confirmationFooterText(),
        )
    }

    private fun saveReviewGroupIndexes(
        ship: FleetMemberAPI?,
        groupIndex: Int,
        scope: WeaponPresetScope,
    ): List<Int> {
        if (ship == null) return emptyList()
        if (groupIndex != CampaignSaveLoadPanelRenderer.ALL_WEAPON_GROUPS_INDEX) {
            return listOf(groupIndex)
        }
        return if (scope == WeaponPresetScope.SUGGESTED) {
            PresetConfirmationDetails.singleWeaponPresetGroupIndexes(ship)
        } else {
            PresetConfirmationDetails.weaponPresetGroupIndexes(ship)
        }
    }

    private fun loadReviewGroupIndexes(
        ship: FleetMemberAPI?,
        groupIndex: Int,
    ): List<Int> {
        if (ship == null) return emptyList()
        if (groupIndex != CampaignSaveLoadPanelRenderer.ALL_WEAPON_GROUPS_INDEX) {
            return listOf(groupIndex)
        }
        return PresetConfirmationDetails.weaponPresetGroupIndexes(ship)
    }

    private fun reviewWeaponRows(
        ship: FleetMemberAPI?,
        groupIndexes: List<Int>,
    ): List<PresetReviewListRow> {
        if (ship == null || groupIndexes.isEmpty()) {
            return listOf(PresetReviewListRow(PRESET_UNKNOWN_WEAPON_COMBINATION_LABEL))
        }
        val includeGroupPrefix = groupIndexes.size > 1
        val rows = mutableListOf<PresetReviewListRow>()
        groupIndexes.forEach { groupIndex ->
            val weaponIds = PresetConfirmationDetails.weaponIdsForGroup(ship, groupIndex)
            if (weaponIds.isEmpty()) {
                rows.add(PresetReviewListRow(groupPrefix(groupIndex, includeGroupPrefix) + PRESET_UNKNOWN_WEAPON_COMBINATION_LABEL))
            } else {
                weaponIds.forEach { weaponId ->
                    val spec = runCatching { Global.getSettings().getWeaponSpec(weaponId) }.getOrNull()
                    rows.add(
                        PresetReviewListRow(
                            label = groupPrefix(groupIndex, includeGroupPrefix) + (spec?.weaponName ?: weaponId),
                            sprite = spec?.turretSpriteName,
                        )
                    )
                }
            }
        }
        return rows
    }

    private fun saveReviewTagRows(
        ship: FleetMemberAPI?,
        runtimeShip: ShipAPI?,
        activePersistenceContext: ShipEditorPersistenceContext?,
        groupIndexes: List<Int>,
        loadoutIndex: Int,
    ): List<PresetReviewListRow> {
        if (ship == null || groupIndexes.isEmpty()) {
            return listOf(PresetReviewListRow(PresetConfirmationDetails.NO_TAGS_SELECTED))
        }
        val context = activePersistenceContext ?: ShipEditorPersistenceContext(ship, runtimeShip)
        val includeGroupPrefix = groupIndexes.size > 1
        val rows = mutableListOf<PresetReviewListRow>()
        groupIndexes.forEach { groupIndex ->
            val tags = sanitizeWeaponCompositionPresetTagsForGroup(
                ship,
                groupIndex,
                context.loadWeaponTags(groupIndex, loadoutIndex)
            )
            if (tags.isEmpty()) {
                rows.add(PresetReviewListRow(groupPrefix(groupIndex, includeGroupPrefix) + PresetConfirmationDetails.NO_TAGS_SELECTED))
            } else {
                tags.forEach { tag ->
                    rows.add(PresetReviewListRow(groupPrefix(groupIndex, includeGroupPrefix) + tag))
                }
            }
        }
        return rows
    }

    private fun loadReviewTagRows(
        ship: FleetMemberAPI?,
        groupIndexes: List<Int>,
        state: PresetControlState,
        loadoutIndex: Int,
        presetPeekCache: PresetPeekCache?,
    ): List<PresetReviewListRow> {
        if (ship == null || groupIndexes.isEmpty()) {
            return listOf(PresetReviewListRow(PresetConfirmationDetails.NO_SAVED_TAGS_FOUND))
        }
        val includeGroupPrefix = groupIndexes.size > 1
        val rows = mutableListOf<PresetReviewListRow>()
        groupIndexes.forEach { groupIndex ->
            val tags = PresetPeekCache.peek(
                presetPeekCache,
                ship,
                groupIndex,
                loadoutIndex,
                state.scope,
                state.backend,
            )
                .takeIf { it.status == WeaponCompositionPresetPeekStatus.FOUND }
                ?.tags
                .orEmpty()
            if (tags.isEmpty()) {
                rows.add(PresetReviewListRow(groupPrefix(groupIndex, includeGroupPrefix) + PresetConfirmationDetails.NO_TAGS_SELECTED))
            } else {
                tags.forEach { tag ->
                    rows.add(PresetReviewListRow(groupPrefix(groupIndex, includeGroupPrefix) + tag))
                }
            }
        }
        return rows
    }

    private fun groupPrefix(groupIndex: Int, include: Boolean): String {
        return if (include) "Group ${groupIndex + 1}: " else ""
    }
}
