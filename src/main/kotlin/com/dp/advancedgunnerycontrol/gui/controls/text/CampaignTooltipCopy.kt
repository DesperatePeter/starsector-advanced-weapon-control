package com.dp.advancedgunnerycontrol.gui.controls.text

import com.dp.advancedgunnerycontrol.config.Values
import com.dp.advancedgunnerycontrol.customlists.WeaponTagListMode

object CampaignTooltipCopy {
    const val NEXT_PAGE = "Show the next page."
    const val PREV_PAGE = "Show the previous page."
    const val RESET_FILTERS = "Clear all weapon filters. This does not change suggested tags."
    const val BACK_TO_FILTER_OPTIONS = "Return to suggested-tags options."
    const val BACK_TO_WEAPON_GROUPS = "Return to the AGC weapon-group editor."

    const val RESET_SUGGESTED_TAGS =
        "Reset customized suggested tags back to the mod defaults. This replaces current custom suggestions."
    const val BACKUP_SUGGESTED_TAGS =
        "Save current customized suggested tags to the cross-campaign backup file saves/common/${Values.CUSTOM_SUGGESTED_TAG_JSON_FILE_NAME}. This overwrites the previous backup."
    const val RESTORE_SUGGESTED_TAGS =
        "Load customized suggested tags from the cross-campaign backup file saves/common/${Values.CUSTOM_SUGGESTED_TAG_JSON_FILE_NAME}. This replaces current custom suggestions."

    fun shipEditorTagListMode(mode: WeaponTagListMode): String {
        val prefix = "Switch this ship's visible weapon-tag and ship-mode lists. " +
            "Only \"All Ships\" and \"This Ship\" are editable. " +
            "Left-click for next; right-click for previous. " +
            "Active off-list tags and modes stay visible until you disable them."
        return "$prefix\n\n${shipEditorTagListModeSuffix(mode)}"
    }

    fun suggestedTagListMode(mode: WeaponTagListMode): String {
        val prefix = "Choose which weapon-tag list is shown in Customize Suggested Tags. " +
            "This does not change any ship's editor list. " +
            "Left-click for next; right-click for previous."
        return "$prefix\n\n${suggestedTagListModeSuffix(mode)}"
    }

    fun weaponInfoSection(title: String, expanded: Boolean): String {
        val action = if (expanded) "Hide" else "Show"
        val detail = if (title == "Advanced Info") "advanced weapon stats" else "basic weapon stats"
        return "$action $detail for this weapon."
    }

    fun filterToggle(active: Boolean): String {
        return if (active) "Remove this active filter." else "Enable this weapon filter."
    }

    fun filterCategoryToggle(category: String, expanded: Boolean, active: Boolean): String {
        val visibility = if (expanded) "Collapse" else "Expand"
        val activeNote = if (active) " Active filters in this group remain applied while collapsed." else ""
        return "$visibility $category filters.$activeNote"
    }

    fun autoApplyToggle(enabled: Boolean): String {
        return if (enabled) {
            "Stop automatically applying suggested tags to empty weapon groups."
        } else {
            "Automatically apply suggested tags to weapon groups that do not already have saved tags."
        }
    }

    private fun shipEditorTagListModeSuffix(mode: WeaponTagListMode): String {
        return when (mode) {
            WeaponTagListMode.CUSTOM_GLOBAL -> "\"All Ships\" is a Custom list shared by all ships."
            WeaponTagListMode.CUSTOM -> "\"This Ship\" is a Custom list for this ship only."
            WeaponTagListMode.NOVICE -> "\"Novice\" is a smaller built-in list focused on the most commonly useful tags and ship modes."
            WeaponTagListMode.CLASSIC -> "\"Classic\" is the standard built-in list curated by the mod authors."
            WeaponTagListMode.COMPLETE -> "\"Complete\" contains one example of every supported weapon tag and ship mode."
        }
    }

    private fun suggestedTagListModeSuffix(mode: WeaponTagListMode): String {
        return when (mode) {
            WeaponTagListMode.CUSTOM_GLOBAL -> "\"All Ships\" uses your shared Custom weapon-tag list."
            WeaponTagListMode.NOVICE -> "\"Novice\" is a smaller built-in weapon-tag list focused on common tags."
            WeaponTagListMode.CLASSIC -> "\"Classic\" is the standard built-in weapon-tag list curated by the mod authors."
            WeaponTagListMode.COMPLETE -> "\"Complete\" contains one example of every supported weapon tag."
            WeaponTagListMode.CUSTOM -> "This mode is not available in Customize Suggested Tags."
        }
    }
}
