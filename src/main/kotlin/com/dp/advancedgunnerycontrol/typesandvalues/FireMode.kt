package com.dp.advancedgunnerycontrol.typesandvalues

import com.dp.advancedgunnerycontrol.weaponais.*
import com.dp.advancedgunnerycontrol.weaponais.suffixes.SuffixBase
import com.fs.starfarer.api.combat.AutofireAIPlugin

typealias FireModeMap = Map<FireMode, AutofireAIPlugin>

enum class FireMode {
    DEFAULT, PD, MISSILE, FIGHTER, NO_FIGHTERS, BIG_SHIPS, SMALL_SHIPS, MINING
}

object FMValues{
    val modesAvailableForCustomAI = // Only add if AI has "isBaseAIOverwritable(): Boolean = true"
        listOf(FireMode.SMALL_SHIPS, FireMode.BIG_SHIPS, FireMode.FIGHTER, FireMode.MISSILE)
// "PD", "Fighters", "Missiles", "NoFighters", "BigShips", "SmallShips", "Mining"
    var FIRE_MODE_DESCRIPTIONS = mutableMapOf(
        FireMode.DEFAULT to "Default",
        FireMode.PD to "PD",
        FireMode.FIGHTER to "Fighters",
        FireMode.MISSILE to "Missiles",
        FireMode.NO_FIGHTERS to "NoFighters",
        FireMode.BIG_SHIPS to "BigShips",
        FireMode.SMALL_SHIPS to "SmallShips",
        FireMode.MINING to "Mining"
    )

    val FIRE_MODE_TRANSLATIONS = mapOf(
        "Default" to FireMode.DEFAULT,
        "PD" to FireMode.PD,
        "Fighters" to FireMode.FIGHTER,
        "Missiles" to FireMode.MISSILE,
        "NoFighters" to FireMode.NO_FIGHTERS,
        "BigShips" to FireMode.BIG_SHIPS,
        "SmallShips" to FireMode.SMALL_SHIPS,
        "Mining" to FireMode.MINING
    )

    // not technically a value, but this way all the mappings are in one place...
    fun modeToPluginMap(baseAI: AutofireAIPlugin, suffix: SuffixBase): FireModeMap {
        return mapOf(
            FireMode.DEFAULT to baseAI,
            FireMode.PD to PDAIPlugin(baseAI),
            FireMode.FIGHTER to AdvancedFighterAIPlugin(baseAI),
            FireMode.MISSILE to AdvancedMissileAIPlugin(baseAI),
            FireMode.NO_FIGHTERS to NoFighterAIPlugin(baseAI),
            FireMode.BIG_SHIPS to BigShipAI(baseAI),
            FireMode.SMALL_SHIPS to SmallShipAI(baseAI),
            FireMode.MINING to MiningAI(baseAI)
        )
    }
}


