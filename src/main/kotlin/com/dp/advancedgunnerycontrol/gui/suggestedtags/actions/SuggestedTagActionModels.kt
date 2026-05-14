package com.dp.advancedgunnerycontrol.gui.suggestedtags.actions

import com.dp.advancedgunnerycontrol.gui.style.CampaignActionButtonKind

data class SuggestedTagUiAction(
    val name: String,
    val shortcuts: List<Int> = emptyList(),
    val tooltip: String = "",
    val rebuildAfter: Boolean = true,
    val active: Boolean = false,
    val kind: CampaignActionButtonKind = CampaignActionButtonKind.UNCOLOURED,
    val activeKind: CampaignActionButtonKind = CampaignActionButtonKind.ACTIVE,
    val indent: Float = 0f,
    val onRightClick: (() -> Unit)? = null,
    val callback: () -> Unit,
) {
    fun effectiveKind(): CampaignActionButtonKind {
        return if (active) activeKind else kind
    }

    fun usesCheckedVisualState(requestedStateful: Boolean): Boolean {
        if (!requestedStateful) return false
        return effectiveKind() != CampaignActionButtonKind.SUGGESTED_FILTER_ACTIVE
    }
}

enum class SuggestedTagDangerousAction {
    RESET,
    BACKUP,
    RESTORE;

    fun actionName(): String {
        return when (this) {
            RESET -> "Reset"
            BACKUP -> "Backup"
            RESTORE -> "Restore"
        }
    }

    fun warningTitle(): String {
        return when (this) {
            RESET -> "Reset Suggested Tags Warning"
            BACKUP -> "Backup Suggested Tags Warning"
            RESTORE -> "Restore Suggested Tags Warning"
        }
    }

    fun confirmationBody(): String {
        return when (this) {
            RESET ->
                "Confirming will reset the customized suggested-tag list to the mod defaults. This replaces current custom suggestions."
            BACKUP ->
                "Confirming will save the current customized suggested-tag list to the cross-campaign backup file, overwriting the previous backup."
            RESTORE ->
                "Confirming will load the suggested-tag list from the cross-campaign backup file, replacing the current customized list."
        }
    }
}
