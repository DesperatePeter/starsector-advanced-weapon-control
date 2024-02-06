package com.dp.advancedgunnerycontrol.combatgui.agccombatgui

import com.dp.advancedgunnerycontrol.combatgui.buttongroups.DataButtonGroup
import com.dp.advancedgunnerycontrol.combatgui.buttongroups.RefreshButtonsAction
import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.dp.advancedgunnerycontrol.typesandvalues.createTag
import com.dp.advancedgunnerycontrol.typesandvalues.isIncompatibleWithExistingTags
import com.dp.advancedgunnerycontrol.utils.loadTags
import com.fs.starfarer.api.combat.ShipAPI

class RefreshWeaponButtons(private val ship: ShipAPI, private val index: Int) : RefreshButtonsAction {
    override fun refreshButtons(group: DataButtonGroup) {
        val currentTags = loadTags(ship, index, Values.storageIndex)
        group.refreshAllButtons(currentTags)
        group.enableAllButtons()
        group.buttons.forEach {
            val str = it.data as? String ?: ""
            val isInvalid = isIncompatibleWithExistingTags(str, currentTags) ||
                    (false == ship.weaponGroupsCopy.getOrNull(index)?.weaponsCopy?.any { w ->
                    createTag(str, w)?.isValid() == true
                })
            if(it.isActive && isInvalid){
                it.isActive = false
                group.executeAction(listOf(), null, it.data)
            }
            if(isInvalid){
                it.isDisabled = true
            }
        }
    }
}