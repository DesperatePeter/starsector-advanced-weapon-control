package com.dp.advancedgunnerycontrol.typesandvalues

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.suffixes.*
import com.fs.starfarer.api.combat.WeaponAPI

enum class Suffixes {
    NONE, FLUX_90, FLUX_75, FLUX_50, CONSERVE_AMMO, PANIC_FIRE
}

const val defaultSuffixString = ""

fun createSuffix(suffix: Suffixes?, weapon: WeaponAPI) : SuffixBase {
    return when(suffix){
        Suffixes.NONE -> SuffixBase(weapon)
        Suffixes.FLUX_50 -> FluxSuffix(weapon, Settings.holdFire50())
        Suffixes.FLUX_75 -> FluxSuffix(weapon, Settings.holdFire75())
        Suffixes.FLUX_90 -> FluxSuffix(weapon, Settings.holdFire90())
        Suffixes.CONSERVE_AMMO -> ConserveAmmoSuffix(weapon)
        Suffixes.PANIC_FIRE -> PanicFireSuffix(weapon, Settings.panicFireHull())
        null -> SuffixBase(weapon)
    }
}

val suffixDescriptions = mapOf(
    Suffixes.NONE to "",
    Suffixes.FLUX_50 to "HoldFire (Flux>50%)",
    Suffixes.FLUX_75 to "HoldFire (Flux>75%)",
    Suffixes.FLUX_90 to "HoldFire (Flux>90%)",
    Suffixes.CONSERVE_AMMO to "ConserveAmmo",
    Suffixes.PANIC_FIRE to "PanicFire"
)

val suffixFromString = suffixDescriptions.map { it.value to it.key }.toMap()

val detailedSuffixDescriptions = mapOf(
    Suffixes.NONE to "No suffix",
    Suffixes.FLUX_50 to "Weapon group will stop firing if ship flux exceeds ${(Settings.holdFire50()*100f).toInt()}%.",
    Suffixes.FLUX_75 to "Weapon group will stop firing if ship flux exceeds ${(Settings.holdFire75()*100f).toInt()}%.",
    Suffixes.FLUX_90 to "Weapon group will stop firing if ship flux exceeds ${(Settings.holdFire90()*100f).toInt()}%.",
    Suffixes.CONSERVE_AMMO to "When below ${(Settings.conserveAmmo()*100f).toInt()}% ammo, " +
            "weapons will only fire when the shot seems effective (similar to Opportunist fire mode). " +
            "Weapons without ammo will ignore this suffix. Works best with modes that support custom AI.",
    Suffixes.PANIC_FIRE to "When the ship is dying (below ${(Settings.panicFireHull()*100f).toInt()}% hull), " +
            "it will blindly fire this weapon group. " +
            "Mainly useful for guided missiles. Best combined wth Opportunist fire mode. Works best with modes that support custom AI."
).withDefault { it.toString() }
