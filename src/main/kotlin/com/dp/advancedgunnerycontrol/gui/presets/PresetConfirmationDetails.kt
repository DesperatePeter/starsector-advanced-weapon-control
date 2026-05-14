package com.dp.advancedgunnerycontrol.gui.presets

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*

import com.dp.advancedgunnerycontrol.gui.modals.CampaignHighlightedText
import com.dp.advancedgunnerycontrol.gui.modals.CampaignTextHighlight
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.dp.advancedgunnerycontrol.presets.WeaponCompositionPresetPeekStatus
import com.dp.advancedgunnerycontrol.presets.WeaponPresetBackend
import com.dp.advancedgunnerycontrol.presets.WeaponPresetScope
import com.dp.advancedgunnerycontrol.shipdata.agcShortShipId
import com.dp.advancedgunnerycontrol.presets.getWeaponCompositionPresetKey
import com.dp.advancedgunnerycontrol.presets.getWeaponCompositionPresetWeaponNames
import com.dp.advancedgunnerycontrol.presets.previewWeaponPresetOverwriteTargetMembers
import com.dp.advancedgunnerycontrol.presets.sanitizeWeaponCompositionPresetTagsForGroup
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI

internal object PresetConfirmationDetails {
    const val NO_TAGS_SELECTED = PRESET_NO_TAGS_SELECTED_LABEL
    const val NO_SAVED_TAGS_FOUND = "(No saved tags found)"

    fun overwriteScopeParagraph(
        ship: FleetMemberAPI?,
        groupIndex: Int,
        scope: WeaponPresetScope,
    ): CampaignHighlightedText {
        val groupText = if (groupIndex == CampaignSaveLoadPanelRenderer.ALL_WEAPON_GROUPS_INDEX) {
            "all matching weapon combinations"
        } else {
            "all instances of this weapon combination"
        }
        val target = overwriteTargetPhrase(ship, scope)
        val text = "This will also overwrite the active tags of $groupText across all loadouts for $target."
        return CampaignHighlightedText(
            text,
            listOf(
                CampaignTextHighlight(text, CampaignGuiStyle.ALERT_RED_COLOR),
                CampaignTextHighlight("overwrite", CampaignGuiStyle.ALERT_RED_COLOR),
                CampaignTextHighlight("all loadouts", CampaignGuiStyle.ALERT_RED_COLOR),
            )
        )
    }

    fun highlightPresetParagraph(text: String): CampaignHighlightedText {
        val highlights = bracketedTextHighlights(text).toMutableList()
        if (text.contains("overwrit", ignoreCase = true)) {
            highlights.add(CampaignTextHighlight("overwriting", CampaignGuiStyle.ALERT_RED_COLOR))
            highlights.add(CampaignTextHighlight("overwrite", CampaignGuiStyle.ALERT_RED_COLOR))
        }
        return CampaignHighlightedText(text, highlights)
    }

    private fun bracketedTextHighlights(text: String): List<CampaignTextHighlight> {
        val highlights = mutableListOf<CampaignTextHighlight>()
        var depth = 0
        var start = -1
        text.forEachIndexed { index, char ->
            when (char) {
                '(' -> {
                    if (depth == 0) start = index
                    depth++
                }
                ')' -> {
                    if (depth > 0) {
                        depth--
                        if (depth == 0 && start >= 0) {
                            highlights.add(
                                CampaignTextHighlight(
                                    text.substring(start, index + 1),
                                    CampaignGuiStyle.MODIFIER_TEXT_COLOUR
                                )
                            )
                            start = -1
                        }
                    }
                }
            }
        }
        return highlights
    }

    private fun storageFamilyPhrase(backend: WeaponPresetBackend): String {
        return CampaignPresetTerminology.bracketedStorageLabel(backend)
    }

    fun loadContextPhrase(
        ship: FleetMemberAPI?,
        weapons: String,
        scope: WeaponPresetScope,
        loadoutIndex: Int,
    ): String {
        return naturalJoin(
            contextParts(
                ship,
                weapons,
                scope,
                includeLoadout = scope != WeaponPresetScope.SUGGESTED,
                loadoutIndex = loadoutIndex,
            )
        )
    }

    fun saveContextPhrase(
        ship: FleetMemberAPI?,
        weapons: String,
        scope: WeaponPresetScope,
        loadoutIndex: Int,
    ): String {
        val weaponPart = if (scope == WeaponPresetScope.SUGGESTED) {
            "this weapon type ($weapons)"
        } else {
            "this combination of weapons ($weapons)"
        }
        return naturalJoin(
            contextParts(
                ship,
                weapons,
                scope,
                includeLoadout = scope != WeaponPresetScope.SUGGESTED,
                weaponPart = weaponPart,
                loadoutIndex = loadoutIndex,
            )
        )
    }

    private fun contextParts(
        ship: FleetMemberAPI?,
        weapons: String,
        scope: WeaponPresetScope,
        includeLoadout: Boolean,
        weaponPart: String = "this combination of weapons ($weapons)",
        loadoutIndex: Int,
    ): List<String> {
        val parts = mutableListOf(weaponPart)
        scopeIdentityPhrase(ship, scope)?.let(parts::add)
        if (includeLoadout) parts.add("loadout (${loadoutPhrase(loadoutIndex)})")
        return parts
    }

    fun allGroupsContextPhrase(ship: FleetMemberAPI?, scope: WeaponPresetScope, loadout: String?): String {
        val parts = mutableListOf("all weapon groups")
        scopeIdentityPhrase(ship, scope)?.let(parts::add)
        parts.add("loadout (${loadout ?: "unknown"})")
        return naturalJoin(parts)
    }

    private fun scopeIdentityPhrase(ship: FleetMemberAPI?, scope: WeaponPresetScope): String? {
        return when (scope) {
            WeaponPresetScope.SINGLE -> "ship ID (${shortShipId(ship)})"
            WeaponPresetScope.CLASS -> "ship class (${ship?.hullSpec?.hullName ?: "unknown"})"
            WeaponPresetScope.GLOBAL,
            WeaponPresetScope.SUGGESTED -> null
        }
    }

    fun presetDescriptor(
        scope: WeaponPresetScope,
        backend: WeaponPresetBackend,
    ): String {
        val storage = storageFamilyPhrase(backend)
        val scopeText = CampaignPresetTerminology.presetDescriptorScopeLabel(scope)
        return "$storage, $scopeText"
    }

    private fun overwriteTargetPhrase(ship: FleetMemberAPI?, scope: WeaponPresetScope): String {
        return when (scope) {
            WeaponPresetScope.SINGLE -> "this specific ship"
            WeaponPresetScope.CLASS -> {
                val className = ship?.hullSpec?.hullName ?: "this"
                "$className class ships in your fleet and storage${overwriteTargetListSuffix(ship, scope, maxEntries = null)}"
            }
            WeaponPresetScope.GLOBAL ->
                "all ships in your fleet and storage${overwriteTargetListSuffix(ship, scope, maxEntries = 10)}"
            WeaponPresetScope.SUGGESTED -> "suggested presets"
        }
    }

    private fun overwriteTargetListSuffix(
        ship: FleetMemberAPI?,
        scope: WeaponPresetScope,
        maxEntries: Int?,
    ): String {
        val source = ship ?: return ""
        val entries = sortedOverwriteTargetMembers(previewWeaponPresetOverwriteTargetMembers(source, scope))
            .map { member ->
                val name = member.shipName.ifBlank { member.hullSpec.hullName }
                "$name (${shortShipId(member)})"
            }
        if (entries.isEmpty()) return ""
        val visible = maxEntries?.let(entries::take) ?: entries
        val remaining = entries.size - visible.size
        val suffix = if (remaining > 0) {
            visible.joinToString(", ") + ", and $remaining more"
        } else {
            visible.joinToString(", ")
        }
        return " ($suffix)"
    }

    private fun sortedOverwriteTargetMembers(members: List<FleetMemberAPI>): List<FleetMemberAPI> {
        val sorted = members.toMutableList()
        for (i in 1 until sorted.size) {
            val value = sorted[i]
            var j = i - 1
            while (j >= 0 && compareOverwriteTargetMembers(sorted[j], value) > 0) {
                sorted[j + 1] = sorted[j]
                j--
            }
            sorted[j + 1] = value
        }
        return sorted
    }

    private fun compareOverwriteTargetMembers(left: FleetMemberAPI, right: FleetMemberAPI): Int {
        val leftName = left.shipName.ifBlank { left.hullSpec.hullName }
        val rightName = right.shipName.ifBlank { right.hullSpec.hullName }
        val nameComparison = leftName.compareTo(rightName)
        if (nameComparison != 0) return nameComparison
        return left.id.orEmpty().compareTo(right.id.orEmpty())
    }

    private fun shortShipId(ship: FleetMemberAPI?): String {
        return agcShortShipId(ship)
    }

    private fun loadoutPhrase(loadoutIndex: Int): String {
        return Settings.loadoutDisplayName(loadoutIndex)
    }

    fun loadoutPhraseOrNull(scope: WeaponPresetScope, loadoutIndex: Int): String? {
        return if (scope == WeaponPresetScope.SUGGESTED) null else loadoutPhrase(loadoutIndex)
    }

    fun loadoutTargetSuffix(loadout: String?): String {
        return loadout?.let { " in this loadout ($it)" }.orEmpty()
    }

    private fun naturalJoin(parts: List<String>): String {
        return when (parts.size) {
            0 -> ""
            1 -> parts.first()
            2 -> "${parts[0]} and ${parts[1]}"
            else -> parts.dropLast(1).joinToString(", ") + " and " + parts.last()
        }
    }

    fun weaponSummary(ship: FleetMemberAPI?, groupIndex: Int): String {
        if (ship == null) return PRESET_UNKNOWN_WEAPON_COMBINATION_LABEL
        if (groupIndex != CampaignSaveLoadPanelRenderer.ALL_WEAPON_GROUPS_INDEX) {
            return getWeaponCompositionPresetWeaponNames(ship, groupIndex)
                .joinToString(" + ")
                .ifBlank { PRESET_UNKNOWN_WEAPON_COMBINATION_LABEL }
        }
        val groups = mutableListOf<String>()
        weaponPresetGroupIndexes(ship).forEach { index ->
            val weapons = getWeaponCompositionPresetWeaponNames(ship, index)
                .joinToString(" + ")
            if (weapons.isNotBlank()) {
                groups.add("Group ${index + 1}: $weapons")
            }
        }
        return groups.joinToString("; ").ifBlank { "unknown weapon combinations" }
    }

    fun currentTagSummary(
        ship: FleetMemberAPI?,
        runtimeShip: ShipAPI?,
        activePersistenceContext: ShipEditorPersistenceContext?,
        groupIndex: Int,
        loadoutIndex: Int,
    ): String {
        if (ship == null) return NO_TAGS_SELECTED
        val context = activePersistenceContext ?: ShipEditorPersistenceContext(ship, runtimeShip)
        if (groupIndex != CampaignSaveLoadPanelRenderer.ALL_WEAPON_GROUPS_INDEX) {
            return sanitizeWeaponCompositionPresetTagsForGroup(
                ship,
                groupIndex,
                context.loadWeaponTags(groupIndex, loadoutIndex)
            ).joinToString(" + ").ifBlank { NO_TAGS_SELECTED }
        }
        val groups = mutableListOf<String>()
        weaponPresetGroupIndexes(ship).forEach { index ->
            val tags = sanitizeWeaponCompositionPresetTagsForGroup(
                ship,
                index,
                context.loadWeaponTags(index, loadoutIndex)
            ).joinToString(" + ").ifBlank { NO_TAGS_SELECTED }
            groups.add("Group ${index + 1}: $tags")
        }
        return groups.joinToString("; ").ifBlank { NO_TAGS_SELECTED }
    }

    fun savedTagSummary(
        ship: FleetMemberAPI?,
        groupIndex: Int,
        state: PresetControlState,
        loadoutIndex: Int,
        presetPeekCache: PresetPeekCache? = null,
    ): String {
        if (ship == null) return NO_SAVED_TAGS_FOUND
        if (groupIndex != CampaignSaveLoadPanelRenderer.ALL_WEAPON_GROUPS_INDEX) {
            return PresetPeekCache.peek(
                presetPeekCache,
                ship,
                groupIndex,
                loadoutIndex,
                state.scope,
                state.backend,
            )
                .takeIf { it.status == WeaponCompositionPresetPeekStatus.FOUND }
                ?.tags
                ?.joinToString(" + ")
                ?.ifBlank { NO_TAGS_SELECTED }
                ?: NO_SAVED_TAGS_FOUND
        }
        val groups = mutableListOf<String>()
        weaponPresetGroupIndexes(ship).forEach { index ->
            val tags = PresetPeekCache.peek(
                presetPeekCache,
                ship,
                index,
                loadoutIndex,
                state.scope,
                state.backend,
            )
                .takeIf { it.status == WeaponCompositionPresetPeekStatus.FOUND }
                ?.tags
                ?.joinToString(" + ")
                ?.ifBlank { NO_TAGS_SELECTED }
                ?: NO_SAVED_TAGS_FOUND
            groups.add("Group ${index + 1}: $tags")
        }
        return groups.joinToString("; ").ifBlank { NO_SAVED_TAGS_FOUND }
    }

    fun weaponPresetGroupIndexes(ship: FleetMemberAPI): List<Int> {
        val indexes = mutableListOf<Int>()
        for (index in 0 until Values.MAX_WEAPON_GROUPS) {
            if (getWeaponCompositionPresetKey(ship, index) != null) {
                indexes.add(index)
            }
        }
        return indexes
    }

    fun singleWeaponPresetGroupIndexes(ship: FleetMemberAPI): List<Int> {
        val indexes = mutableListOf<Int>()
        for (index in 0 until Values.MAX_WEAPON_GROUPS) {
            if (weaponIdsForGroup(ship, index).size == 1) {
                indexes.add(index)
            }
        }
        return indexes
    }

    fun weaponIdsForGroup(ship: FleetMemberAPI, groupIndex: Int): List<String> {
        val key = getWeaponCompositionPresetKey(ship, groupIndex) ?: return emptyList()
        val ids = mutableListOf<String>()
        key.split("|").forEach { weaponId ->
            if (weaponId.isNotBlank()) {
                ids.add(weaponId)
            }
        }
        return ids
    }
}
