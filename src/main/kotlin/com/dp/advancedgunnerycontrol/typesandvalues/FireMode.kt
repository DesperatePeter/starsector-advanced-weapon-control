package com.dp.advancedgunnerycontrol.typesandvalues

import com.dp.advancedgunnerycontrol.weaponais.*
import com.fs.starfarer.api.combat.AutofireAIPlugin

typealias FireModeMap = Map<FireMode, AutofireAIPlugin>

enum class FireMode {
    DEFAULT, PD, MISSILE, FIGHTER, NO_FIGHTERS, BIG_SHIPS, SMALL_SHIPS, MINING

}

object FMValues{
    val modesAvailableForCustomAI = // Only add if AI has "isBaseAIOverwritable(): Boolean = true"
        listOf(FireMode.SMALL_SHIPS, FireMode.BIG_SHIPS, FireMode.FIGHTER, FireMode.MISSILE)


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

    var FIRE_MODE_DESCRIPTIONS = mutableMapOf(
        FireMode.DEFAULT to "Default (base AI)",
        FireMode.PD to "PD Mode (base AI)",
        FireMode.FIGHTER to "Fighters only",
        FireMode.MISSILE to "Missiles only",
        FireMode.NO_FIGHTERS to "Ignore Fighters (base AI)",
        FireMode.BIG_SHIPS to "Big Ships",
        FireMode.SMALL_SHIPS to "Small Ships",
        FireMode.MINING to "Mining (Asteroids)"
    )

    // not technically a value, but this way all the mappings are in one place...
    fun modeToPluginMap(baseAI: AutofireAIPlugin): FireModeMap {
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


