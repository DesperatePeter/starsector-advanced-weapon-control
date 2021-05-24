package com.dp.advancedgunnerycontrol.typesandvalues

import com.dp.advancedgunnerycontrol.weaponais.*
import com.dp.advancedgunnerycontrol.weaponais.suffixes.SuffixBase
import com.fs.starfarer.api.combat.AutofireAIPlugin

typealias FireModeMap = Map<FireMode, AutofireAIPlugin>

enum class FireMode {
    DEFAULT, PD, MISSILE, FIGHTER, NO_FIGHTERS, BIG_SHIPS, SMALL_SHIPS, MINING, OPPORTUNIST
}

object FMValues{
    val modesAvailableForCustomAI = // Only add if AI has "isBaseAIOverwritable(): Boolean = true"
        listOf(FireMode.SMALL_SHIPS, FireMode.BIG_SHIPS, FireMode.FIGHTER, FireMode.MISSILE, FireMode.OPPORTUNIST)

    const val defaultFireModeString = "Default"

    var fireModeAsString = mapOf(
        FireMode.DEFAULT to "Default",
        FireMode.PD to "PD",
        FireMode.FIGHTER to "Fighters",
        FireMode.MISSILE to "Missiles",
        FireMode.NO_FIGHTERS to "NoFighters",
        FireMode.BIG_SHIPS to "BigShips",
        FireMode.SMALL_SHIPS to "SmallShips",
        FireMode.MINING to "Mining",
        FireMode.OPPORTUNIST to "Opportunist"
    )

    val fireModeDetailedDescriptions = mapOf(
        FireMode.DEFAULT to "Use the base AI (most suffixes still apply).",
        FireMode.PD to "Use the base AI, but only fire at missiles/fighters. " +
                "Won't fire if the base AI would target something else. " +
                "Only for PD-weapons (non-PD weapons in this group will use default).",
        FireMode.FIGHTER to "Only target and fire at fighters. Can use custom AI.",
        FireMode.MISSILE to "Only target and fire at missiles. Only for PD-weapons (non-PD weapons in this group will use default). " +
                "Can use custom AI.",
        FireMode.NO_FIGHTERS to "Use the base AI, but don't fire if target is a fighter.",
        FireMode.BIG_SHIPS to "Ignore frigates and fighters, prioritize capitals>cruisers>destroyers. Can use custom AI.",
        FireMode.SMALL_SHIPS to "Ignore anything bigger than destroyers, prioritize fighters>frigates>destroyers. " +
                "Can use custom AI.",
        FireMode.MINING to "Only target asteroids. Always uses custom AI.",
        FireMode.OPPORTUNIST to "Only fire if the shot is likely to hit and be effective. Good for limited ammo weapons (e.g. missiles). " +
                "HE weapons will only fire if target is shieldless or at high flux, kinetic weapons will only fire if target " +
                "is shielded and at lowish flux. This mode won't fire at missiles/fighters. Always uses custom AI."
    ).withDefault { it.toString() }

    var FIRE_MODE_DESCRIPTIONS = fireModeAsString.toMutableMap()

    val FIRE_MODE_TRANSLATIONS = fireModeAsString.map { it.value to it.key }.toMap()

    // not technically a value, but this way all the mappings are in one place...
    fun modeToPluginMap(baseAI: AutofireAIPlugin, suffix: SuffixBase): FireModeMap {
        return mapOf(
            FireMode.DEFAULT to baseAI,
            FireMode.PD to PDAIPlugin(baseAI, suffix),
            FireMode.FIGHTER to AdvancedFighterAIPlugin(baseAI, suffix),
            FireMode.MISSILE to AdvancedMissileAIPlugin(baseAI, suffix),
            FireMode.NO_FIGHTERS to NoFighterAIPlugin(baseAI, suffix),
            FireMode.BIG_SHIPS to BigShipAI(baseAI, suffix),
            FireMode.SMALL_SHIPS to SmallShipAI(baseAI, suffix),
            FireMode.MINING to MiningAI(baseAI, suffix),
            FireMode.OPPORTUNIST to OpportunistAI(baseAI, suffix)
        )
    }
}


