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


internal object CustomTagSectionRows {
    fun addRows(
        rows: MutableList<CustomTagManagerRow>,
        currentTags: List<String>,
        pendingAdditions: List<String>,
        pendingRemovals: Set<String>,
        pendingEdits: Map<String, String>,
        expandedListSections: Set<String>,
        expandedCategories: Set<String>,
        expandedArchetypes: Set<String>,
        archetypes: List<CustomTagArchetype>,
    ) {
        val stagedTags = stagedCustomTags(currentTags, pendingAdditions, pendingRemovals, pendingEdits)
        val visibleTags = canonicalizeWeaponTagNames(stagedTags + pendingRemovals)
        val visibleTagSet = visibleTags.toSet()
        val tagState = CustomTagManagerTagState(
            pendingRemovals = pendingRemovals,
            pendingAdditions = pendingAdditions.toSet(),
            editedTargets = pendingEdits.values.toSet(),
        )

        val tagsExpanded = CustomListDraftKeys.ListSections.TAGS in expandedListSections
        rows += CustomTagManagerRow.ListSectionHeading(
            id = CustomListDraftKeys.ListSections.TAGS,
            label = CustomTagManagerSectionLabels.TAGS,
            expanded = tagsExpanded,
            active = visibleTags.isNotEmpty(),
            count = visibleTags.size,
        )
        if (!tagsExpanded) return

        val archetypesByCategory = archetypesByCategory(archetypes)
        val visibleTagsByArchetypeId = visibleTagsByArchetypeId(visibleTags)

        for (category in WeaponTagCategory.DISPLAY_CATEGORIES) {
            val categoryArchetypes = archetypesByCategory[category] ?: continue
            if (categoryArchetypes.isEmpty()) continue

            var categoryActive = false
            for (archetype in categoryArchetypes) {
                if (visibleTagsByArchetypeId[archetype.id].orEmpty().isNotEmpty()) {
                    categoryActive = true
                    break
                }
            }

            val categoryExpanded = category.title in expandedCategories
            rows += CustomTagManagerRow.CategoryHeading(category, categoryExpanded, categoryActive)
            if (!categoryExpanded) continue

            for (archetype in categoryArchetypes) {
                val archetypeTags = visibleTagsByArchetypeId[archetype.id].orEmpty()
                val archetypeActive = archetypeTags.isNotEmpty()
                val archetypeExpanded = archetype.id in expandedArchetypes
                rows += CustomTagManagerRow.ArchetypeHeading(archetype, archetypeExpanded, archetypeActive)
                if (!archetypeExpanded) continue

                for (tag in sortedTags(archetypeTags, tagState)) {
                    rows += CustomTagManagerRow.TagEntry(
                        archetype = archetype,
                        tag = tag,
                        markedForRemoval = tagState.isMarkedForRemoval(tag),
                        pendingAddition = tagState.isPendingAddition(tag),
                        pendingEdit = tagState.isPendingEdit(tag),
                    )
                }

                val directTag = archetype.directTag
                if (archetype.definition != null || (directTag != null && directTag !in visibleTagSet)) {
                    rows += CustomTagManagerRow.AddTagEntry(archetype = archetype)
                }
            }
        }
    }

    private fun archetypesByCategory(
        archetypes: List<CustomTagArchetype>,
    ): Map<WeaponTagCategory, List<CustomTagArchetype>> {
        val grouped = linkedMapOf<WeaponTagCategory, MutableList<CustomTagArchetype>>()
        for (archetype in archetypes) {
            var categoryArchetypes = grouped[archetype.category]
            if (categoryArchetypes == null) {
                categoryArchetypes = mutableListOf()
                grouped[archetype.category] = categoryArchetypes
            }
            categoryArchetypes.add(archetype)
        }
        return grouped
    }

    private fun visibleTagsByArchetypeId(
        visibleTags: List<String>,
    ): Map<String, List<String>> {
        val grouped = linkedMapOf<String, MutableList<String>>()
        for (tag in visibleTags) {
            val archetypeId = archetypeIdForTag(tag)
            var archetypeTags = grouped[archetypeId]
            if (archetypeTags == null) {
                archetypeTags = mutableListOf()
                grouped[archetypeId] = archetypeTags
            }
            archetypeTags.add(tag)
        }
        return grouped
    }

    private fun archetypeIdForTag(tag: String): String {
        val canonicalTag = canonicalizeWeaponTagName(tag)
        val definitionId = EditableWeaponTagDefinitions.parse(canonicalTag)?.definitionId
        return if (definitionId != null) "definition:$definitionId" else "tag:$canonicalTag"
    }

    private fun sortedTags(
        tags: List<String>,
        tagState: CustomTagManagerTagState,
    ): List<String> {
        val sorted = tags.toMutableList()
        for (i in 1 until sorted.size) {
            val value = sorted[i]
            var j = i - 1
            while (j >= 0 && compareTags(sorted[j], value, tagState) > 0) {
                sorted[j + 1] = sorted[j]
                j--
            }
            sorted[j + 1] = value
        }
        return sorted
    }

    private fun compareTags(
        left: String,
        right: String,
        tagState: CustomTagManagerTagState,
    ): Int {
        val leftStatus = tagState.statusFor(left)
        val rightStatus = tagState.statusFor(right)
        if (leftStatus != rightStatus) return leftStatus.sortRank.compareTo(rightStatus.sortRank)
        return left.compareTo(right)
    }
}
