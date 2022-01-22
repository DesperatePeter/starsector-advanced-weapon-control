package com.dp.advancedgunnerycontrol.typesandvalues

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.*
import com.dp.advancedgunnerycontrol.weaponais.suffixes.SuffixBase
import com.fs.starfarer.api.combat.AutofireAIPlugin

typealias FireModeMap = Map<FireMode, AutofireAIPlugin>

enum class FireMode {
    DEFAULT, PD, MISSILE, FIGHTER, NO_FIGHTERS, BIG_SHIPS, SMALL_SHIPS, MINING, OPPORTUNIST,
    TARGET_SHIELDS, AVOID_SHIELDS, PD_FLUX, PD_AMMO
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
        FireMode.TARGET_SHIELDS to "TargetShields",
        FireMode.PD_AMMO to "PD (Ammo<90%)",
        FireMode.PD_FLUX to "PD (Flux>50%)"
    )

    val fireModeDetailedDescriptions = mapOf(
        FireMode.DEFAULT to "Use the base AI (most suffixes still apply). It's recommended to keep most weapons on default. " +
                "Other modes will mainly lead to the weapon firing less.",
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
        FireMode.OPPORTUNIST to "(experimental) Only fire if the shot is likely to hit and be effective. Good for limited ammo weapons (e.g. missiles)." +
                "\nHE weapons will only fire if target is shieldless/flanked or at high flux (value defined in settings), kinetic weapons will only fire if target" +
                " is shielded, at lowish (see settings) flux and not flanked. Same logic as target/avoid shields but much stricter." +
                "\nThis mode will only fire, if the enemy is likely to still be in range after the projectile travels, even" +
                " if the target moves away, and if the target is slow enough" +
                " that it is unlikely to evade (depending on projectile speed and tracking)." +
                "\nThis mode will be more conservative when the weapon uses ammo (and even more so if the ammo doesn't reload)." +
                "\nFinisher type missiles will only fire if the target is defenseless (overloaded or unshielded)." +
                "\nThis mode won't fire at missiles/fighters. Always uses custom AI." +
                "\nTip: Ideally you want to manually control weapons rather than using opportunist mode. " +
                "For non-missile weapons consider target/avoid shields instead.",
        FireMode.TARGET_SHIELDS to "Weapon will prioritize shooting shields." +
                "\n - Will not fire at overloaded/unshielded/venting/phase ships" +
                "\n - Will not fire when flanking enemy shields" +
                "\n - Will never fire if target doesn't have shields." +
                "\n - Will not fire at very high flux (80%+) enemies" +
                "\n - Will not fire at missiles. Always uses custom AI." +
                "\nTip: Leave some kinetic weapons on Default to guarantee constant pressure against high-flux enemies",
        FireMode.AVOID_SHIELDS to "Weapon will prioritize targets without shields or high flux/shields off." +
                "\n - Will not fire at low flux (50%-) enemies unless flanking shields." +
                "\n - Will always fire if target doesn't have shields." +
                "\n - Will not fire at missiles. Always uses custom AI.",
        FireMode.PD_FLUX to "Weapon will behave like Default when ship flux < 50% and like PD otherwise.",
        FireMode.PD_AMMO to "Weapon will behave like Default when ammo > 90% and like PD otherwise." +
                " Useful for e.g. Burst PD Lasers."
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
            FireMode.AVOID_SHIELDS to AvoidShieldsAI(baseAI, suffix),
            FireMode.PD_AMMO to PDAtAmmoThresholdAI(baseAI, suffix, Settings.pdAmmo90()),
            FireMode.PD_FLUX to PDAtFluxThresholdAI(baseAI, suffix, Settings.pdFlux50())
        )
    }
}


