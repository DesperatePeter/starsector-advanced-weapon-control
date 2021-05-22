package com.dp.advancedgunnerycontrol.typesandvalues

import com.dp.advancedgunnerycontrol.weaponais.suffixes.*
import com.fs.starfarer.api.combat.WeaponAPI

enum class Suffixes {
    NONE, FLUX_90, FLUX_75, FLUX_50, CONSERVE_AMMO, PANIC_FIRE, PD_IF_FLUX50, PD_IF_LOW_AMMO
}

const val defaultSuffixString = ""

fun createSuffix(suffix: Suffixes?, weapon: WeaponAPI) : SuffixBase {
    return when(suffix){
        Suffixes.NONE -> SuffixBase(weapon)
        Suffixes.FLUX_50 -> FluxSuffix(weapon, 0.5f)
        Suffixes.FLUX_75 -> FluxSuffix(weapon, 0.75f)
        Suffixes.FLUX_90 -> FluxSuffix(weapon, 0.9f)
        Suffixes.CONSERVE_AMMO -> ConserveAmmoSuffix(weapon)
        Suffixes.PANIC_FIRE -> PanicFireSuffix(weapon)
        Suffixes.PD_IF_FLUX50 -> PDAtFluxThresholdSuffix(weapon, 0.5f)
        Suffixes.PD_IF_LOW_AMMO -> PDAtAmmoThresholdSuffix(weapon)
        null -> SuffixBase(weapon)
    }
}

val suffixDescriptions = mapOf(
    Suffixes.NONE to "",
    Suffixes.FLUX_50 to "HoldFire(Flux>50%)",
    Suffixes.FLUX_75 to "HoldFire(Flux>75%)",
    Suffixes.FLUX_90 to "HoldFire(Flux>90%)",
    Suffixes.CONSERVE_AMMO to "ConserveAmmo",
    Suffixes.PANIC_FIRE to "PanicFire",
    Suffixes.PD_IF_FLUX50 to "PD(Flux>50%)",
    Suffixes.PD_IF_LOW_AMMO to "PD(Ammo<90%)"
)

val suffixFromString = suffixDescriptions.map { it.value to it.key }.toMap()