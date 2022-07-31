package com.dp.advancedgunnerycontrol.typesandvalues

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.tags.*
import com.fs.starfarer.api.combat.WeaponAPI

val tags = listOf("PD", "Fighter", "AvoidShields", "TargetShields", "NoFighters",
    "Hold(Flx>90%)", "Hold(Flx>75%)", "Hold(Flx>50%)", "ConserveAmmo", "Opportunist")

val tagTooltips = mapOf(
    "PD" to "Restricts targeting to fighters and missiles.",
    "Fighter" to "Restricts targeting to fighters.",
    "AvoidShields" to "Weapon will prioritize targets without shields or high flux/shields off.",
    "TargetShields" to "Weapon will prioritize shooting shields.",
    "NoFighters" to "Weapon won't target fighters.",
    "Hold(Flx>90%)" to "Weapon will stop firing if ship flux exceeds ${(Settings.holdFire90()*100f).toInt()}%.",
    "Hold(Flx>75%)" to "Weapon will stop firing if ship flux exceeds ${(Settings.holdFire75()*100f).toInt()}%.",
    "Hold(Flx>50%)" to "Weapon will stop firing if ship flux exceeds ${(Settings.holdFire50()*100f).toInt()}%.",
    "ConserveAmmo" to "Weapon will be much more hesitant to fire when ammo below ${(Settings.conserveAmmo()*100f).toInt()}%.",
    "Opportunist" to "Weapon will be more hesitant to fire and won't target missiles or fighters. Use for e.g. limited ammo weapons."
)

fun createTag(name: String, weapon: WeaponAPI) : WeaponAITagBase?{
    return when (name){
        "PD" -> PDTag(weapon)
        "Fighter" -> FighterTag(weapon)
        "AvoidShields" -> AvoidShieldsTag(weapon)
        "TargetShields" -> TargetShieldsTag(weapon)
        "NoFighters" -> NoFightersTag(weapon)
        "Hold(Flx>90%)" -> FluxTag(weapon, Settings.holdFire90())
        "Hold(Flx>75%)" -> FluxTag(weapon, Settings.holdFire75())
        "Hold(Flx>50%)" -> FluxTag(weapon, Settings.holdFire50())
        "ConserveAmmo" -> ConserveAmmoTag(weapon, Settings.conserveAmmo())
        "Opportunist" -> OpportunistTag(weapon)
        else -> null
    }
}

val tagIncompatibility = mapOf(
    "PD" to listOf("Fighter", "Opportunist"),
    "Fighter" to listOf("PD", "NoFighters", "Opportunist"),
    "AvoidShields" to listOf("TargetShields"),
    "TargetShields" to listOf("AvoidShields"),
    "NoFighters" to listOf("Fighter", "Opportunist"),
    "Hold(Flx>90%)" to listOf("Hold(Flx>75%)", "Hold(Flx>50%)"),
    "Hold(Flx>75%)" to listOf("Hold(Flx>90%)", "Hold(Flx>50%)"),
    "Hold(Flx>50%)" to listOf("Hold(Flx>75%)", "Hold(Flx>90%)"),
    "Opportunist" to listOf("Fighter", "PD", "NoFighters")
)

fun createTags(names: List<String>, weapon: WeaponAPI) : List<WeaponAITagBase>{
    return names.mapNotNull { createTag(it, weapon) }
}