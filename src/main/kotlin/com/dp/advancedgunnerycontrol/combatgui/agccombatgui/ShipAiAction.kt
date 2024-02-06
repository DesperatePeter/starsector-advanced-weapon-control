package com.dp.advancedgunnerycontrol.combatgui.agccombatgui

import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.dp.advancedgunnerycontrol.typesandvalues.assignShipModes
import com.dp.advancedgunnerycontrol.typesandvalues.defaultShipMode
import com.dp.advancedgunnerycontrol.typesandvalues.saveShipModes
import com.fs.starfarer.api.combat.ShipAPI
import org.magiclib.combatgui.buttongroups.MagicCombatButtonGroupAction

class ShipAiAction(private val ship: ShipAPI) : MagicCombatButtonGroupAction {
    override fun execute(data: List<Any>, selectedButtonData: Any?, deselectedButtonData: Any?) {
        var tags = data.filterIsInstance<String>()
        tags = if ((defaultShipMode == selectedButtonData) || tags.isEmpty()) {
            listOf(defaultShipMode)
        } else {
            tags.filter { it != defaultShipMode }
        }
        assignShipModes(tags, ship)
        saveShipModes(ship, Values.storageIndex, tags)
    }
}