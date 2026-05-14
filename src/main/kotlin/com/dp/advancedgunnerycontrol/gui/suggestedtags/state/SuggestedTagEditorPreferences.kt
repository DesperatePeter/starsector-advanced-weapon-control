package com.dp.advancedgunnerycontrol.gui.suggestedtags.state

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*


import com.dp.advancedgunnerycontrol.gui.suggestedtags.filters.WeaponFilter
import com.dp.advancedgunnerycontrol.settings.Settings

object SuggestedTagEditorPreferences {
    private val SUPPORTED_LIST_MODES = listOf(
        WeaponTagListMode.CUSTOM_GLOBAL,
        WeaponTagListMode.NOVICE,
        WeaponTagListMode.CLASSIC,
        WeaponTagListMode.COMPLETE,
    )

    fun listModes(): List<WeaponTagListMode> = SUPPORTED_LIST_MODES

    fun activeListMode(): WeaponTagListMode {
        val stored = WeaponTagListMode.fromStorageId(Settings.suggestedTagEditorListMode)
        return if (stored in SUPPORTED_LIST_MODES) stored else WeaponTagListMode.CUSTOM_GLOBAL
    }

    fun saveListMode(mode: WeaponTagListMode) {
        Settings.suggestedTagEditorListMode = (if (mode in SUPPORTED_LIST_MODES) mode else WeaponTagListMode.CUSTOM_GLOBAL).storageId
    }

    fun activeFilters(): List<WeaponFilter> {
        val stored = Settings.suggestedTagEditorFilters.toSet()
        return WeaponFilter.allFilters.filter { it.name() in stored }
    }

    fun saveFilters(filters: List<WeaponFilter>) {
        Settings.suggestedTagEditorFilters = WeaponFilter.allFilters
            .filter { filter -> filter in filters }
            .map { it.name() }
    }
}
