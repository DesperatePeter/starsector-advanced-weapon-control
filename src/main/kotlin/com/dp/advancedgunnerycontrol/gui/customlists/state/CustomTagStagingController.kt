package com.dp.advancedgunnerycontrol.gui.customlists.state

import com.dp.advancedgunnerycontrol.weapontags.canonicalizeWeaponTagName
import com.dp.advancedgunnerycontrol.weapontags.canonicalizeWeaponTagNames


internal class CustomTagStagingController(
    private val draftStore: CustomListDraftStore,
    private val markedForRemovalProvider: (() -> Set<String>)?,
    private val markedForRemovalUpdater: ((Set<String>) -> Unit)?,
    private val onChanged: () -> Unit,
) {
    fun additions(): MutableList<String> =
        draftStore.canonicalList(
            key = CustomListDraftKeys.Tags.ADDITIONS,
            canonicalize = ::canonicalTag,
        ).toMutableList()

    fun setAdditions(tags: List<String>) {
        draftStore.writeCanonicalList(
            key = CustomListDraftKeys.Tags.ADDITIONS,
            values = tags,
            canonicalize = ::canonicalTag,
        )
    }

    fun edits(): Map<String, String> =
        draftStore.canonicalEditMap(
            key = CustomListDraftKeys.Tags.EDITS,
            canonicalize = ::canonicalTag,
        )

    fun setEdits(edits: Map<String, String>) {
        draftStore.writeCanonicalEditMap(
            key = CustomListDraftKeys.Tags.EDITS,
            edits = edits,
            canonicalize = ::canonicalTag,
        )
    }

    fun currentMarkedForRemoval(): MutableSet<String> {
        return markedForRemovalProvider?.invoke()?.toMutableSet() ?: mutableSetOf()
    }

    fun toggleMarkedForRemoval(tag: String) {
        val marked = currentMarkedForRemoval()
        if (!marked.add(tag)) {
            marked.remove(tag)
        }
        updateMarkedForRemoval(marked)
    }

    fun normalizeStagedState(currentTags: List<String>): CustomTagManagerStagedState {
        val currentSet = canonicalizeWeaponTagNames(currentTags).toSet()
        val rawAdditions = additions()
        val additions = rawAdditions.filterNot { it in currentSet }.distinct()
        if (additions != rawAdditions) {
            setAdditions(additions)
        }

        val rawRemovals = currentMarkedForRemoval()
        val removalSources = rawRemovals.filter { it in currentSet }.toSet()
        val rawEdits = edits()
        val edits = rawEdits
            .filter { (source, edited) ->
                source in currentSet &&
                    source !in removalSources &&
                    source != edited &&
                    edited !in additions &&
                    edited !in (currentSet - source)
            }
        if (edits != rawEdits) {
            setEdits(edits)
        }

        val editSources = edits.keys
        val editTargets = edits.values.toSet()
        val removals = rawRemovals
            .filter { it in currentSet && (it !in editSources || it in removalSources) && it !in editTargets }
            .toMutableSet()
        if (removals != rawRemovals) {
            updateMarkedForRemoval(removals)
        }
        return CustomTagManagerStagedState(additions, removals, edits)
    }

    fun stagedTags(
        currentTags: List<String>,
        pendingAdditions: List<String>,
        pendingRemovals: Set<String>,
        pendingEdits: Map<String, String>,
    ): List<String> =
        stagedCustomTags(currentTags, pendingAdditions, pendingRemovals, pendingEdits)

    fun stageAddition(tag: String, currentTags: List<String>) {
        val canonicalTag = canonicalTag(tag)
        val currentSet = canonicalizeWeaponTagNames(currentTags).toSet()
        val additions = additions()
        val removals = currentMarkedForRemoval()
        removals.remove(canonicalTag)
        updateMarkedForRemoval(removals)
        if (canonicalTag !in currentSet && canonicalTag !in additions) {
            additions += canonicalTag
            setAdditions(additions)
        } else {
            onChanged()
        }
    }

    fun removeAddition(tag: String) {
        val canonicalTag = canonicalTag(tag)
        setAdditions(additions().filterNot { it == canonicalTag })
    }

    fun replaceAddition(sourceTag: String, editedTag: String) {
        val canonicalSource = canonicalTag(sourceTag)
        val canonicalEdited = canonicalTag(editedTag)
        val additions = additions()
            .filterNot { it == canonicalSource || it == canonicalEdited }
            .toMutableList()
        additions += canonicalEdited
        setAdditions(additions)
    }

    fun stageEdit(sourceTag: String, editedTag: String) {
        val canonicalSource = canonicalTag(sourceTag)
        val canonicalEdited = canonicalTag(editedTag)
        val edits = edits().toMutableMap()
        if (canonicalSource == canonicalEdited) {
            edits.remove(canonicalSource)
        } else {
            edits[canonicalSource] = canonicalEdited
        }
        val removals = currentMarkedForRemoval()
        removals.remove(canonicalSource)
        removals.remove(canonicalEdited)
        updateMarkedForRemoval(removals)
        setEdits(edits)
    }

    fun sourceForEdit(editedTag: String): String? {
        val canonicalEdited = canonicalTag(editedTag)
        return edits().entries.firstOrNull { it.value == canonicalEdited }?.key
    }

    private fun canonicalTag(tag: String): String = canonicalizeWeaponTagName(tag)

    private fun updateMarkedForRemoval(tags: Set<String>) {
        markedForRemovalUpdater?.invoke(tags)
        onChanged()
    }
}
