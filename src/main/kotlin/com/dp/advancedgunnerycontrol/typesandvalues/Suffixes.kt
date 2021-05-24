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
    Suffixes.FLUX_50 to "HoldFire (Flux>50%)",
    Suffixes.FLUX_75 to "HoldFire (Flux>75%)",
    Suffixes.FLUX_90 to "HoldFire (Flux>90%)",
    Suffixes.CONSERVE_AMMO to "ConserveAmmo",
    Suffixes.PANIC_FIRE to "PanicFire",
    Suffixes.PD_IF_FLUX50 to "PD (Flux>50%)",
    Suffixes.PD_IF_LOW_AMMO to "PD (Ammo<90%)"
)

val detailedSuffixDescriptions = mapOf(
    Suffixes.NONE to "No suffix",
    Suffixes.FLUX_50 to "Weapon group will stop firing if ship flux exceeds 50%.",
    Suffixes.FLUX_75 to "Weapon group will stop firing if ship flux exceeds 75%.",
    Suffixes.FLUX_90 to "Weapon group will stop firing if ship flux exceeds 90%.",
    Suffixes.CONSERVE_AMMO to "When below 90% ammo, weapons will only fire when the shot seems effective (same as Opportunist fire mode). " +
            "Weapons without ammo will ignore this suffix. Works best with modes that support custom AI.",
    Suffixes.PANIC_FIRE to "When the ship is dying (below 50% hull), it will blindly fire this weapon group. " +
            "Mainly useful for guided missiles. Best combined wth Opportunist fire mode. Works best with modes that support custom AI.",
    Suffixes.PD_IF_FLUX50 to "Weapon group will only shoot missiles/fighters if flux exceeds 50%. " +
            "Only use with PD weapons and modes that can target missiles/fighters!",
    Suffixes.PD_IF_LOW_AMMO to "Weapon group will only shoot missiles/fighters if weapon uses ammo and ammo < 90%. Mainly for Burst PD Lasers. " +
            "Only use with PD weapons and modes that can target missiles/fighters! Weapons without ammo will ignore this suffix."
).withDefault { it.toString() }

val suffixFromString = suffixDescriptions.map { it.value to it.key }.toMap()