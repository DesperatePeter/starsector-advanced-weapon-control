package com.dp.advancedgunnerycontrol.gui.actions

import com.dp.advancedgunnerycontrol.gui.GUIAttributes
import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.fleet.FleetMemberAPI

class ApplySuggestedModeAction(attributes: GUIAttributes) : GUIAction(attributes) {
    override fun execute() {
        affectedLoadouts().forEach { index ->
            affectedShips().forEach { ship ->
                applySuggestedModes(ship, index)
            }
        }
    }

    override fun getTooltip(): String {
        return "This will apply suggested weapon tags to all " +
                "weapon groups. The suggested modes are defined in data/config/modSettings.json. Other mods can also " +
                "define suggested modes in their modSettings.json.\n" +
                "Please double check that all modes look good after applying them. Groups with mixed " +
                "weapons will arbitrarily select one of the weapons to select a mode for the group.\n" +
                "Only affects current loadout and ship.\n" +
                "Note that weapons from mods will usually only have suggested modes if the author " +
                "of that mod included suggested modes in their mod." +
                "\n$modifiersBoilerplateText"
    }

    override fun getName(): String = "Load suggested modes"

    private fun applySuggestedModes(ship: FleetMemberAPI, storageIndex: Int) {
        val groups = ship.variant.weaponGroups
        val tagStore = Settings.tagStorage[storageIndex]
        if (tagStore.modesByShip[ship.id] == null) {
            tagStore.modesByShip[ship.id] = mutableMapOf()
        }
        groups.forEachIndexed { index, group ->
            val weaponID = group.slots.first()?.let { ship.variant.getWeaponId(it) } ?: ""
            val tagKey: String = if (Settings.suggestedTags.containsKey(weaponID)) {
                weaponID
            } else {
                Settings.suggestedTags.keys.map { Regex(it) }.find { it.matches(weaponID) }.toString()
            }
            tagStore.modesByShip[ship.id]?.let { it[index] = Settings.suggestedTags[tagKey] ?: emptyList() }
        }
    }
}