package com.dp.advancedgunnerycontrol.gui.customlists.state

import com.dp.advancedgunnerycontrol.shipmodes.canonicalizeShipModeName


internal class CustomShipModeStagingController(
    private val draftStore: CustomListDraftStore,
    private val onChanged: () -> Unit,
) {
    fun additions(): MutableList<String> =
        draftStore.canonicalList(
            key = CustomListDraftKeys.ShipModes.ADDITIONS,
            canonicalize = ::canonicalShipMode,
        ).toMutableList()

    fun setAdditions(modes: List<String>) {
        draftStore.writeCanonicalList(
            key = CustomListDraftKeys.ShipModes.ADDITIONS,
            values = modes,
            canonicalize = ::canonicalShipMode,
        )
    }

    fun removals(): MutableSet<String> =
        draftStore.canonicalList(
            key = CustomListDraftKeys.ShipModes.REMOVALS,
            canonicalize = ::canonicalShipMode,
        ).toMutableSet()

    fun setRemovals(modes: Set<String>) {
        draftStore.writeCanonicalList(
            key = CustomListDraftKeys.ShipModes.REMOVALS,
            values = modes.toList(),
            canonicalize = ::canonicalShipMode,
        )
    }

    fun edits(): Map<String, String> =
        draftStore.canonicalEditMap(
            key = CustomListDraftKeys.ShipModes.EDITS,
            canonicalize = ::canonicalShipMode,
        )

    fun setEdits(edits: Map<String, String>) {
        draftStore.writeCanonicalEditMap(
            key = CustomListDraftKeys.ShipModes.EDITS,
            edits = edits,
            canonicalize = ::canonicalShipMode,
        )
    }

    fun toggleMarkedForRemoval(mode: String) {
        val canonicalMode = canonicalShipMode(mode)
        val removals = removals()
        if (!removals.add(canonicalMode)) removals.remove(canonicalMode)
        setRemovals(removals)
    }

    fun stageAddition(mode: String, currentModes: List<String>) {
        val canonicalMode = canonicalShipMode(mode)
        val currentSet = currentModes.map(::canonicalShipMode).toSet()
        val additions = additions()
        val removals = removals()
        removals.remove(canonicalMode)
        setRemovals(removals)
        if (canonicalMode !in currentSet && canonicalMode !in additions) {
            additions += canonicalMode
            setAdditions(additions)
        } else {
            onChanged()
        }
    }

    fun removeAddition(mode: String) {
        val canonicalMode = canonicalShipMode(mode)
        setAdditions(additions().filterNot { it == canonicalMode })
    }

    fun normalizeStagedState(currentModes: List<String>): CustomShipModeManagerStagedState {
        val currentSet = currentModes.map(::canonicalShipMode).toSet()
        val rawAdditions = additions()
        val additions = rawAdditions.filterNot { it in currentSet }.distinct()
        if (additions != rawAdditions) {
            setAdditions(additions)
        }

        val rawRemovals = removals()
        val rawEdits = edits()
        val edits = rawEdits
            .filter { (source, edited) ->
                source in currentSet &&
                    source !in rawRemovals &&
                    source != edited &&
                    edited !in additions &&
                    edited !in (currentSet - source)
            }
        if (edits != rawEdits) {
            setEdits(edits)
        }
        val editTargets = edits.values.toSet()
        // Removal wins over edit: an edited source marked for removal is shown
        // and applied as a single removal, not as both an edit and a removal.
        val removals = rawRemovals
            .filter { it in currentSet && it !in editTargets }
            .toSet()
        if (removals != rawRemovals) {
            setRemovals(removals)
        }
        return CustomShipModeManagerStagedState(additions, removals, edits)
    }

    fun stagedShipModes(
        currentModes: List<String>,
        pendingAdditions: List<String>,
        pendingRemovals: Set<String>,
        pendingEdits: Map<String, String>,
    ): List<String> =
        stagedCustomShipModes(currentModes, pendingAdditions, pendingRemovals, pendingEdits)

    fun stageEdit(sourceMode: String, editedMode: String) {
        val canonicalSource = canonicalShipMode(sourceMode)
        val canonicalEdited = canonicalShipMode(editedMode)
        val edits = edits().toMutableMap()
        if (canonicalSource == canonicalEdited) {
            edits.remove(canonicalSource)
        } else {
            edits[canonicalSource] = canonicalEdited
        }
        val removals = removals()
        removals.remove(canonicalSource)
        removals.remove(canonicalEdited)
        setRemovals(removals)
        setEdits(edits)
    }

    fun replaceAddition(sourceMode: String, editedMode: String) {
        val canonicalSource = canonicalShipMode(sourceMode)
        val canonicalEdited = canonicalShipMode(editedMode)
        val additions = additions()
            .filterNot { it == canonicalSource || it == canonicalEdited }
            .toMutableList()
        additions += canonicalEdited
        setAdditions(additions)
    }

    fun sourceForEdit(editedMode: String): String? {
        val canonicalEdited = canonicalShipMode(editedMode)
        return edits().entries.firstOrNull { it.value == canonicalEdited }?.key
    }

    private fun canonicalShipMode(mode: String): String = canonicalizeShipModeName(mode)
}
