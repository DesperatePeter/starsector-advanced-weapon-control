package com.dp.advancedgunnerycontrol.shipdata

import com.dp.advancedgunnerycontrol.config.Values
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

fun determineTagsByWeaponFromCustomData(ship: ShipAPI): Map<WeaponAPI, List<String>> {
    if (!ship.customData.containsKey(Values.CUSTOM_SHIP_DATA_OPTIONS_TO_APPLY_KEY)) return emptyMap()
    val optionTagEntries = (ship.customData[Values.CUSTOM_SHIP_DATA_OPTIONS_TO_APPLY_KEY] as? Map<*, *>)
        ?.filter { mapPair -> mapPair.key is String && (mapPair.value as? List<*>)?.all { it is String } == true }
        ?: return emptyMap()

    val tagsByWeapon = mutableMapOf<WeaponAPI, MutableSet<String>>()
    optionTagEntries.forEach { optionEntry ->
        when {
            magicKeyToType.containsKey(optionEntry.key) -> {
                ship.allWeapons.filter { it.type == magicKeyToType[optionEntry.key] }.forEach { weapon ->
                    val tags = (optionEntry.value as? List<*>)?.filterIsInstance<String>() ?: listOf()
                    tagsByWeapon.getOrPut(weapon) { mutableSetOf() }.addAll(tags)
                }
            }

            optionEntry.key is String -> {
                val weaponIdOrPattern = optionEntry.key as? String ?: ""
                val weaponIdPattern = runCatching { Regex(weaponIdOrPattern) }.getOrNull()
                ship.allWeapons.filter { weapon ->
                    weapon.id == weaponIdOrPattern || weaponIdPattern?.matches(weapon.id) == true
                }.forEach { weapon ->
                    val tags = (optionEntry.value as? List<*>)?.filterIsInstance<String>() ?: listOf()
                    tagsByWeapon.getOrPut(weapon) { mutableSetOf() }.addAll(tags)
                }
            }
        }
    }

    return tagsByWeapon.mapValues { it.value.toList() }
}

fun determineShipModesFromCustomData(ship: ShipAPI): List<String>{
    if(!ship.customData.containsKey(Values.CUSTOM_SHIP_DATA_SHIP_MODES_TO_APPLY_KEY)) return emptyList()
    val modes = (ship.customData[Values.CUSTOM_SHIP_DATA_SHIP_MODES_TO_APPLY_KEY] as? List<*>)?.filterIsInstance<String>()
    return modes ?: emptyList()
}
