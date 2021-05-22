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


