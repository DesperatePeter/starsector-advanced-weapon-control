package com.dp.advancedgunnerycontrol.utils

import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType

const val MISSILE_MAGIC_KEY = "!MAGIC!Missile"
const val ENERGY_MAGIC_KEY = "!MAGIC!Energy"
const val BALLISTIC_MAGIC_KEY = "!MAGIC!Ballistic"

val magicKeyToType = mapOf(
    MISSILE_MAGIC_KEY to WeaponType.MISSILE,
    ENERGY_MAGIC_KEY to WeaponType.ENERGY,
    BALLISTIC_MAGIC_KEY to WeaponType.BALLISTIC
)

fun determineTagsByGroup(ship: ShipAPI) : Map<WeaponAPI, List<String>>{
    if(!ship.customData.containsKey(Values.CUSTOM_SHIP_DATA_OPTIONS_TO_APPLY_KEY)) return emptyMap()
    val opts = (ship.customData[Values.CUSTOM_SHIP_DATA_OPTIONS_TO_APPLY_KEY] as? Map<*, *>)
        ?.filter { mapPair -> mapPair.key is String && (mapPair.value as? List<*>)?.all { it is String } == true } ?: return emptyMap()

    val toReturn = mutableMapOf<WeaponAPI, MutableSet<String>>()
    opts.forEach { m ->
        when{
            magicKeyToType.containsKey(m.key) -> {
                ship.allWeapons.filter { it.type == magicKeyToType[m.key] }.forEach { w ->
                    val tags = (m.value as? List<*>)?.filterIsInstance<String>() ?: listOf()
                    toReturn.getOrPut(w) { mutableSetOf() }.addAll(tags)
                }
            }
            m.key is String -> {
                val k = m.key as? String ?: ""
                ship.allWeapons.filter { it.id == k || Regex(k).matches(it.id)}.forEach { w ->
                    val tags = (m.value as? List<*>)?.filterIsInstance<String>() ?: listOf()
                    toReturn.getOrPut(w) { mutableSetOf() }.addAll(tags)
                }
            }
        }
    }

    return toReturn.mapValues { it.value.toList() }
}