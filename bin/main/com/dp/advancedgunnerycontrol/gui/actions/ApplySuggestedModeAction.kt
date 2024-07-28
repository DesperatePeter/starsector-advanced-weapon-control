package com.dp.advancedgunnerycontrol.gui.actions

import com.dp.advancedgunnerycontrol.gui.GUIAttributes
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.applySuggestedModes
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

    override fun getName(): String =  "Load suggested modes" + nameSuffix()
}