package com.dp.advancedgunnerycontrol.gui.suggestedtags.controls

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weapontags.getSuggestedTagsForWeaponId

internal object SuggestedTagSelectionStore {
    fun supportedSuggestedTagsForWeapon(
        weaponId: String,
        supportedTags: Set<String> = Settings.getCurrentWeaponTagList().toSet(),
    ): List<String> {
        return getSuggestedTagsForWeaponId(weaponId, supportedTags)
    }

    fun saveSuggestedTags(weaponId: String, tags: List<String>) {
        val st = Settings.getCurrentSuggestedTags().toMutableMap()
        st[weaponId] = tags
        Settings.customSuggestedTags = st
    }
}
