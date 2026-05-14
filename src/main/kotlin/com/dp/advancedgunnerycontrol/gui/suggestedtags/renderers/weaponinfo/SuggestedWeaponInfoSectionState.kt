package com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers.weaponinfo

internal class SuggestedWeaponInfoSectionState(
    private val collapsedWeaponInfoSections: MutableSet<String>,
    private val expandedAdvancedWeaponInfoSections: MutableSet<String>,
    private val onStateChanged: () -> Unit,
) {
    fun key(weaponId: String, sectionId: String): String = "$weaponId:$sectionId"

    fun weaponInfoSectionCollapsed(key: String, sectionId: String): Boolean {
        return when (sectionId) {
            "advanced" -> key !in expandedAdvancedWeaponInfoSections
            else -> key in collapsedWeaponInfoSections
        }
    }

    fun toggleWeaponInfoSection(sectionId: String, key: String) {
        if (sectionId == "advanced") {
            if (!expandedAdvancedWeaponInfoSections.add(key)) {
                expandedAdvancedWeaponInfoSections.remove(key)
            }
        } else if (!collapsedWeaponInfoSections.add(key)) {
            collapsedWeaponInfoSections.remove(key)
        }
        onStateChanged()
    }
}
