package com.dp.advancedgunnerycontrol.gui.presets

import com.dp.advancedgunnerycontrol.gui.style.CampaignActionButtonKind
import com.dp.advancedgunnerycontrol.presets.WeaponPresetBackend
import com.dp.advancedgunnerycontrol.presets.WeaponPresetScope

enum class PendingPresetAction {
    SAVE,
    LOAD,
}

data class PresetControlState(
    val scope: WeaponPresetScope = WeaponPresetScope.SINGLE,
    val backend: WeaponPresetBackend = WeaponPresetBackend.CAMPAIGN,
    val overwrite: Boolean = false,
    val pendingAction: PendingPresetAction? = null,
    val cleanTags: List<String>? = null,
    val backendManuallySelected: Boolean = false,
)

data class PresetActionExecutionResult(
    val executed: Boolean,
    val action: PendingPresetAction?,
    val affectedGroupIndexes: Set<Int> = emptySet(),
)

internal data class PresetControlAvailability(
    val enabled: Boolean,
    val disabledTooltip: String? = null,
)

private const val SAVE_TOOLTIP = "Open the Save preset popup for this weapon group."
private const val LOAD_TOOLTIP = "Open the Load preset popup for this weapon group."

internal fun PendingPresetAction.slot(): Int {
    return when (this) {
        PendingPresetAction.SAVE -> 0
        PendingPresetAction.LOAD -> 1
    }
}

internal fun PendingPresetAction.dataPrefix(): String {
    return when (this) {
        PendingPresetAction.SAVE -> "save_preset"
        PendingPresetAction.LOAD -> "load_preset"
    }
}

internal fun PendingPresetAction.label(): String {
    return when (this) {
        PendingPresetAction.SAVE -> "Save"
        PendingPresetAction.LOAD -> "Load"
    }
}

internal fun PendingPresetAction.kind(): CampaignActionButtonKind {
    return when (this) {
        PendingPresetAction.SAVE -> CampaignActionButtonKind.SAVE
        PendingPresetAction.LOAD -> CampaignActionButtonKind.LOAD
    }
}

internal fun PendingPresetAction.tooltip(): String {
    return when (this) {
        PendingPresetAction.SAVE -> SAVE_TOOLTIP
        PendingPresetAction.LOAD -> LOAD_TOOLTIP
    }
}

internal fun PendingPresetAction.allGroupsTooltip(): String {
    return "${label()} presets for all non-empty weapon groups on this ship."
}
