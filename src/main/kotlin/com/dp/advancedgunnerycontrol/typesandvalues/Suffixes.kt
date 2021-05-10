package com.dp.advancedgunnerycontrol.typesandvalues

import com.dp.advancedgunnerycontrol.weaponais.suffixes.FluxSuffix
import com.dp.advancedgunnerycontrol.weaponais.suffixes.SuffixBase
import com.fs.starfarer.api.combat.WeaponAPI

enum class Suffixes {
    NONE, FLUX_90, FLUX_75, FLUX_50
}

const val defaultSuffixString = ""

fun createSuffix(suffix: Suffixes?, weapon: WeaponAPI) : SuffixBase {
    return when(suffix){
        Suffixes.NONE -> SuffixBase(weapon)
        Suffixes.FLUX_50 -> FluxSuffix(weapon, 0.5f)
        Suffixes.FLUX_75 -> FluxSuffix(weapon, 0.75f)
        Suffixes.FLUX_90 -> FluxSuffix(weapon, 0.9f)
        null -> SuffixBase(weapon)
    }
}

val suffixDescriptions = mapOf(
    Suffixes.NONE to "",
    Suffixes.FLUX_50 to "Flux<50%",
    Suffixes.FLUX_75 to "Flux<75%",
    Suffixes.FLUX_90 to "Flux<90%"
)

val suffixFromString = suffixDescriptions.map { it.value to it.key }.toMap()