package com.dp.advancedgunnerycontrol.combatgui.agccombatgui

import com.dp.advancedgunnerycontrol.combatgui.buttongroups.ButtonGroupAction
import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.dp.advancedgunnerycontrol.utils.applyTagsToWeaponGroup
import com.dp.advancedgunnerycontrol.utils.saveTags
import com.fs.starfarer.api.combat.ShipAPI

class WeaponGroupAction(private val ship: ShipAPI, private val index: Int) : ButtonGroupAction {
    override fun execute(data: List<Any>, triggeringButtonData: Any?) {
        val tagStrings = data.filterIsInstance<String>()
        applyTagsToWeaponGroup(ship, index, tagStrings)
        saveTags(ship, index, Values.storageIndex, tagStrings)
    }
}