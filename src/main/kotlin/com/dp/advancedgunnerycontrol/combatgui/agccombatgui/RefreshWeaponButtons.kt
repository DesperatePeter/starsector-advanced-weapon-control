package com.dp.advancedgunnerycontrol.combatgui.agccombatgui

import com.dp.advancedgunnerycontrol.combatgui.buttongroups.DataButtonGroup
import com.dp.advancedgunnerycontrol.combatgui.buttongroups.RefreshButtonsAction
import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.dp.advancedgunnerycontrol.typesandvalues.createTag
import com.dp.advancedgunnerycontrol.typesandvalues.isIncompatibleWithExistingTags
import com.dp.advancedgunnerycontrol.utils.loadTags
import com.fs.starfarer.api.combat.ShipAPI

class RefreshWeaponButtons(private val ship: ShipAPI, private val index: Int) : RefreshButtonsAction() {
    override fun refreshButtons(group: DataButtonGroup) {
        val currentTags = loadTags(ship, index, Values.storageIndex)
        group.refreshAllButtons(currentTags)
        group.enableAllButtons()
        group.buttons.forEach {
            val str = it.data as? String ?: ""
            if(isIncompatibleWithExistingTags(str, currentTags)){
                it.isDisabled = true
            }
            if(true != ship.weaponGroupsCopy.getOrNull(index)?.weaponsCopy?.any { w -> createTag(str, w )?.isValid() == true }){
                it.isDisabled = true
            }
        }
    }
}