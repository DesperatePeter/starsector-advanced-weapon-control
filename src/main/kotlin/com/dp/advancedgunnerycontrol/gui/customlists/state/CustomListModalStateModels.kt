package com.dp.advancedgunnerycontrol.gui.customlists.state

import com.dp.advancedgunnerycontrol.shipmodes.canonicalizeShipModeName
import com.dp.advancedgunnerycontrol.shipmodes.canonicalizeShipModeNames
import com.dp.advancedgunnerycontrol.weapontags.canonicalizeWeaponTagName
import com.dp.advancedgunnerycontrol.weapontags.canonicalizeWeaponTagNames


internal object CustomListDraftKeys {
    // Safe for AGC-generated tag and ship-mode strings; neither format emits these separators.
    const val STATE_SEPARATOR = "|"
    const val EDIT_PAIR_SEPARATOR = "=>"

    object Tags {
        const val ADDITIONS = "customTagManagerAdditions"
        const val EDITS = "customTagManagerEdits"
        const val EXPANDED_CATEGORIES = "customTagManagerExpandedCategories"
        const val EXPANDED_ARCHETYPES = "customTagManagerExpandedArchetypes"
    }

    object ShipModes {
        const val ADDITIONS = "customShipModeManagerAdditions"
        const val REMOVALS = "customShipModeManagerRemovals"
        const val EDITS = "customShipModeManagerEdits"
    }

    object ListSections {
        const val TAGS = "tags"
        const val SHIP_MODES = "shipModes"
        const val EXPANDED_MANAGER_SECTIONS = "customListManagerExpandedSections"
    }

    object ChangeReview {
        const val EXPANDED_SECTIONS = "customTagChangeReviewExpandedSections"
    }

    object Edit {
        const val KIND = "customTagEditKind"
        const val KIND_TAG = "tag"
        const val KIND_SHIP_MODE = "shipMode"
        const val RETURN = "customTagEditReturn"
        const val RETURN_MANAGER = "manager"
        const val PENDING_ADDITION = "customTagEditPendingAddition"
        const val FIXED_TAG = "customTagEditFixedTag"
    }

    object DebugColor {
        const val INDEX = "debugColorIndex"
        const val RED = "debugColorR"
        const val GREEN = "debugColorG"
        const val BLUE = "debugColorB"
        const val PERSISTENCE = "debugColorPersistence"
    }

    object LoadoutRename {
        const val INDEX = "loadoutIndex"
        const val NAME = "loadoutName"
    }
}

internal data class CustomTagManagerStagedState(
    val additions: List<String>,
    val removals: Set<String>,
    val edits: Map<String, String>,
)

internal data class CustomShipModeManagerStagedState(
    val additions: List<String>,
    val removals: Set<String>,
    val edits: Map<String, String> = emptyMap(),
)

internal fun stagedCustomTags(
    currentTags: List<String>,
    pendingAdditions: List<String>,
    pendingRemovals: Set<String>,
    pendingEdits: Map<String, String>,
): List<String> {
    val canonicalRemovals = canonicalizeWeaponTagNames(pendingRemovals.toList()).toSet()
    val editSources = canonicalizeWeaponTagNames(pendingEdits.keys.toList()).toSet()
    return canonicalizeWeaponTagNames(
        currentTags
            .filterNot {
                val canonical = canonicalizeWeaponTagName(it)
                canonical in canonicalRemovals || canonical in editSources
            } +
            pendingEdits.values +
            pendingAdditions
    )
}

internal fun stagedCustomShipModes(
    currentModes: List<String>,
    pendingAdditions: List<String>,
    pendingRemovals: Set<String>,
    pendingEdits: Map<String, String>,
): List<String> {
    val canonicalRemovals = canonicalizeShipModeNames(pendingRemovals.toList()).toSet()
    val editSources = canonicalizeShipModeNames(pendingEdits.keys.toList()).toSet()
    return canonicalizeShipModeNames(
        currentModes
            .filterNot {
                val canonical = canonicalizeShipModeName(it)
                canonical in canonicalRemovals || canonical in editSources
            } +
            pendingEdits.values +
            pendingAdditions
    )
}

internal interface CustomListModalChangeHandler {
    fun onCustomListModalStateChanged()
}
