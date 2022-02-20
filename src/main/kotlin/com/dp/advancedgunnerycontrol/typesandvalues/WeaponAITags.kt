package com.dp.advancedgunnerycontrol.typesandvalues

import com.dp.advancedgunnerycontrol.weaponais.tags.AvoidShieldsTag
import com.dp.advancedgunnerycontrol.weaponais.tags.FighterTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PDTag
import com.dp.advancedgunnerycontrol.weaponais.tags.WeaponAITagBase
import com.fs.starfarer.api.combat.WeaponAPI

val tags = listOf("PD", "Fighter", "AvoidShields")

val tagTooltips = mapOf(
    "PD" to "Restricts targeting to fighters and missiles",
    "Fighter" to "Restricts targeting to fighters",
    "AvoidShields" to "todo"
)

fun createTag(name: String, weapon: WeaponAPI) : WeaponAITagBase?{
    return when (name){
        "PD" -> PDTag(weapon)
        "Fighter" -> FighterTag(weapon)
        "AvoidShields" -> AvoidShieldsTag(weapon)
        else -> null
    }
}

fun createTags(names: List<String>, weapon: WeaponAPI) : List<WeaponAITagBase>{
    return names.mapNotNull { createTag(it, weapon) }
}