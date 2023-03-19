package com.dp.advancedgunnerycontrol.combatgui.agccombatgui

import com.dp.advancedgunnerycontrol.combatgui.buttongroups.ButtonGroupAction
import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.dp.advancedgunnerycontrol.typesandvalues.assignShipMode
import com.dp.advancedgunnerycontrol.typesandvalues.defaultShipMode
import com.dp.advancedgunnerycontrol.typesandvalues.saveShipModes
import com.fs.starfarer.api.combat.ShipAPI

class ShipAiAction(private val ship: ShipAPI) : ButtonGroupAction {
    override fun execute(data: List<Any>, triggeringButtonData: Any?) {
        var tags = data.filterIsInstance<String>()
        tags = if ((defaultShipMode == triggeringButtonData) || tags.isEmpty()) {
            listOf(defaultShipMode)
        } else {
            tags.filter { it != defaultShipMode }
        }
        assignShipMode(tags, ship)
        saveShipModes(ship, Values.storageIndex, tags)
    }
}