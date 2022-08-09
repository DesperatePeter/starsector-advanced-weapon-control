package com.dp.advancedgunnerycontrol.gui.actions

import com.dp.advancedgunnerycontrol.gui.AGCGUI.Companion.storageIndex
import com.dp.advancedgunnerycontrol.gui.GUIAttributes
import com.dp.advancedgunnerycontrol.settings.Settings

class CycleLoadoutAction(attributes: GUIAttributes) : GUIAction(attributes) {
    override fun execute() {
        storageIndex = if (storageIndex < Settings.maxLoadouts() - 1) storageIndex + 1 else 0
    }

    override fun getTooltip(): String {
        return "You can have different mode loadouts for your fleet. Each loadout stores modes independently. " +
                "This let's you adapt to the enemy loadout you are facing. For instance, if you are fighting against " +
                "an enemy fleet with lots of strike craft, you might want to switch to a loadout where with some ships " +
                "having weapon groups set to Fighter mode. During combat, cycle loadouts via GUI-Button." +
                "\nNote: Loadouts are cycled fleet-wide, not per ship."
    }

    override fun getName(): String = "Cycle loadout [Current" +
            " ${storageIndex + 1} / ${Settings.maxLoadouts()}] <${Settings.loadoutNames().getOrNull(storageIndex) ?:
            "NoName"}>"
}