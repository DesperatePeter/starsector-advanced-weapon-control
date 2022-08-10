package com.dp.advancedgunnerycontrol.utils

import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

const val autofireMissiles = "AutofireAllMissiles"

fun determineTagsByGroup(ship: ShipAPI) : Map<Int, List<String>>{
    if(!ship.customData.containsKey(Values.CUSTOM_SHIP_DATA_OPTIONS_TO_APPLY_KEY)) return emptyMap()
    val opts = (ship.customData[Values.CUSTOM_SHIP_DATA_OPTIONS_TO_APPLY_KEY] as? List<*>)?.filterIsInstance<String>() ?: return emptyMap()
    val toReturn = mutableMapOf<Int, MutableSet<String>>()
    when{
        opts.contains(autofireMissiles) -> {
            ship.weaponGroupsCopy.forEachIndexed { index, group ->
                if(group.weaponsCopy?.any { it.type == WeaponAPI.WeaponType.MISSILE } == true){
                    toReturn.getOrPut(index) { mutableSetOf() }.add("ForceAF")
                }
            }
        }
    }
    return toReturn.mapValues { it.value.toList() }
}