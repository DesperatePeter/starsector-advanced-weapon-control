package com.dp.advancedgunnerycontrol.typesandvalues

import com.dp.advancedgunnerycontrol.weaponais.suffixes.FluxSuffix
import com.dp.advancedgunnerycontrol.weaponais.suffixes.SuffixBase
import com.fs.starfarer.api.combat.WeaponAPI

enum class Suffixes {
    NONE, FLUX_BELOW90, FLUX_BELOW75, FLUX_BELOW50
}

fun createSuffix(suffix: Suffixes?, weapon: WeaponAPI) : SuffixBase {
    return when(suffix){
        Suffixes.NONE -> SuffixBase(weapon)
        Suffixes.FLUX_BELOW50 -> FluxSuffix(weapon, 0.5f)
        Suffixes.FLUX_BELOW75 -> FluxSuffix(weapon, 0.75f)
        Suffixes.FLUX_BELOW90 -> FluxSuffix(weapon, 0.9f)
        null -> SuffixBase(weapon)
    }
}