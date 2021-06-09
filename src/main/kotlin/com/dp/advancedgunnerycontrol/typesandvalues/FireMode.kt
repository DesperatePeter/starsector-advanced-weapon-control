package com.dp.advancedgunnerycontrol.typesandvalues

import com.dp.advancedgunnerycontrol.weaponais.*
import com.dp.advancedgunnerycontrol.weaponais.suffixes.SuffixBase
import com.fs.starfarer.api.combat.AutofireAIPlugin

typealias FireModeMap = Map<FireMode, AutofireAIPlugin>

enum class FireMode {
    DEFAULT, PD, MISSILE, FIGHTER, NO_FIGHTERS, BIG_SHIPS, SMALL_SHIPS, MINING, OPPORTUNIST, TARGET_SHIELDS, AVOID_SHIELDS
}

object FMValues{
    val modesAvailableForCustomAI = // Only add if base AI is overwritable
        listOf(FireMode.SMALL_SHIPS, FireMode.BIG_SHIPS, FireMode.FIGHTER, FireMode.MISSILE,
            FireMode.OPPORTUNIST, FireMode.TARGET_SHIELDS, FireMode.AVOID_SHIELDS)

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
        FireMode.OPPORTUNIST to "Opportunist",
        FireMode.AVOID_SHIELDS to "AvoidShields",
        FireMode.TARGET_SHIELDS to "TargetShields"
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
        FireMode.MINING to "Only shoot/target asteroids. This is mode is just for fun. Always uses custom AI.",
        FireMode.OPPORTUNIST to "Only fire if the shot is likely to hit and be effective. Good for limited ammo weapons (e.g. missiles)." +
                "\nHE weapons will only fire if target is shieldless or at high flux (>80%), kinetic weapons will only fire if target" +
                " is shielded and at lowish (<70%) flux." +
                "\nThis mode will only fire, if the enemy is likely to still be in range after the projectile travels, even" +
                " if the target moves away, and if the target is slow enough" +
                " that it is unlikely to evade (depending on projectile speed and tracking)." +
                "\nThis mode won't fire at missiles/fighters. Always uses custom AI.",
        FireMode.TARGET_SHIELDS to "Weapon will prioritize targets with shields or low flux/shields on." +
                "\n - If the targets shields are off, will fire if target flux level < ~65%" +
                "\n - If target shields are on, will fire if target flux level < ~90%" +
                "\n - Will never fire if target doesn't have shields." +
                "\nThis mode won't fire at missiles. Always uses custom AI." +
                "\nNote: In future versions, I might add geometrical analysis of shield facing.",
        FireMode.AVOID_SHIELDS to "Weapon will prioritize targets without shields or high flux/shields off." +
                "\n - If the targets shields are off, will fire if target flux level > ~60%" +
                "\n - If target shields are on, will fire if target flux level > ~85%" +
                "\n - Will always fire if target doesn't have shields." +
                "\nThis mode won't fire at missiles. Always uses custom AI." +
                "\nNote: In future versions, I might add geometrical analysis of shield facing."
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
            FireMode.OPPORTUNIST to OpportunistAI(baseAI, suffix),
            FireMode.TARGET_SHIELDS to TargetShieldsAI(baseAI, suffix),
            FireMode.AVOID_SHIELDS to AvoidShieldsAI(baseAI, suffix)
        )
    }
}


