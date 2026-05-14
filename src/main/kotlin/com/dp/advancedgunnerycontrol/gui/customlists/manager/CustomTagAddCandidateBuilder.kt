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

import com.dp.advancedgunnerycontrol.settings.Settings

internal object CustomTagAddCandidateBuilder {
    fun completeTagListArchetypes(): List<CustomTagArchetype> {
        return archetypes(addCandidates())
    }

    fun addCandidates(): List<CustomTagAddCandidate> {
        val candidates = mutableListOf<CustomTagAddCandidate>()
        val seenDefinitions = mutableSetOf<String>()
        val seenDirectTags = mutableSetOf<String>()

        fun addDefinition(definition: EditableWeaponTagDefinition) {
            if (!seenDefinitions.add(definition.id)) return
            candidates += CustomTagAddCandidate(
                id = "definition:${definition.id}",
                label = definition.templateTag,
                definition = definition,
                directTag = null,
            )
        }

        Settings.getWeaponTagListForMode(WeaponTagListMode.COMPLETE).forEach { tag ->
            val definition = EditableWeaponTagDefinitions.definitionForTemplate(tag)
            if (definition != null) {
                addDefinition(definition)
                return@forEach
            }

            val canonicalTag = canonicalizeWeaponTagName(tag)
            if (!CustomWeaponTagListStore.isSupportedTag(canonicalTag) || !seenDirectTags.add(canonicalTag)) {
                return@forEach
            }
            candidates += CustomTagAddCandidate(
                id = "tag:$canonicalTag",
                label = EditableWeaponTagDefinitions.displayName(canonicalTag),
                definition = null,
                directTag = canonicalTag,
            )
        }

        EditableWeaponTagDefinitions.definitions.forEach(::addDefinition)
        return candidates
    }

    fun archetypes(candidates: List<CustomTagAddCandidate>): List<CustomTagArchetype> {
        val archetypes = mutableListOf<CustomTagArchetype>()
        for (candidate in candidates) {
            val category = WeaponTagCategory.categoryFor(
                candidate.definition?.templateTag ?: candidate.directTag ?: candidate.label,
            )
            archetypes += CustomTagArchetype(
                id = candidate.id,
                label = candidate.definition?.templateTag ?: candidate.label,
                definition = candidate.definition,
                directTag = candidate.directTag,
                category = category,
            )
        }
        return archetypes
    }
}
