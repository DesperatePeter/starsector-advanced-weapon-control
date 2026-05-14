package com.dp.advancedgunnerycontrol.combatgui.agccombatgui

import com.dp.advancedgunnerycontrol.config.Values
import com.dp.advancedgunnerycontrol.shipmodes.defaultShipMode
import com.dp.advancedgunnerycontrol.shipmodes.loadShipModes
import com.dp.advancedgunnerycontrol.shipmodes.saveShipModes
import com.fs.starfarer.api.combat.ShipAPI
import org.magiclib.combatgui.buttongroups.MagicCombatDataButtonGroup
import org.magiclib.combatgui.buttongroups.MagicCombatRefreshButtonsAction

class RefreshShipAiButtons(private val ship: ShipAPI) : MagicCombatRefreshButtonsAction {
    override fun refreshButtons(group: MagicCombatDataButtonGroup) {
        val modes = loadShipModes(ship, Values.storageIndex)
        if (modes.isEmpty()) {
            val defaultModes = listOf(defaultShipMode)
            saveShipModes(ship, Values.storageIndex, defaultModes)
            group.refreshAllButtons(defaultModes)
        } else {
            group.refreshAllButtons(modes)
        }
    }
}
