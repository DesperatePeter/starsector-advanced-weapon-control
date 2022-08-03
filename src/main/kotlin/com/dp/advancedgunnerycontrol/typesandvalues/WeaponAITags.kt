package com.dp.advancedgunnerycontrol.typesandvalues

import com.dp.advancedgunnerycontrol.WeaponControlPlugin
import com.dp.advancedgunnerycontrol.gui.isElligibleForPD
import com.dp.advancedgunnerycontrol.gui.usesAmmo
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.tags.*
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI

val tags = Settings.tagList()

val pdTags = listOf("PD", "NoPD")

val ammoTags = listOf("ConserveAmmo")

val holdRegex = Regex("Hold\\(Flx>(\\d+)%\\)")
val pdFluxRegex = Regex("PD\\(Flx>(\\d+)%\\)")

fun extractRegexThreshold(regex: Regex, name: String) : Float{
    return (regex.matchEntire(name)?.groupValues?.get(1)?.toFloat()  ?: 0f) / 100f
}

fun shouldTagBeDisabled(groupIndex: Int, sh: FleetMemberAPI, tag: String) : Boolean{
    if(pdTags.contains(tag) && !isElligibleForPD(groupIndex, sh)){
        return true
    }
    if(ammoTags.contains(tag) && !usesAmmo(groupIndex, sh)){
        return true
    }
    return false
}

val tagTooltips = mapOf(
    "PD" to "Restricts targeting to fighters and missiles.",
    "NoPD" to "Forbids targeting missiles and prioritizes ships over fighters.",
    "Fighter" to "Restricts targeting to fighters.",
    "AvoidShields" to "Weapon will prioritize targets without shields, flanked shields or high flux/shields off.",
    "TargetShields" to "Weapon will prioritize shooting shields. Tip: Keep one kinetic weapon on default to keep up pressure.",
    "NoFighters" to "Weapon won't target fighters.",
    "ConserveAmmo" to "Weapon will be much more hesitant to fire when ammo below ${(Settings.conserveAmmo()*100f).toInt()}%.",
    "Opportunist" to "Weapon will be more hesitant to fire and won't target missiles or fighters. Use for e.g. limited ammo weapons."
)

fun getTagTooltip(tag: String) : String{
    if (tagTooltips.containsKey(tag)){
        return tagTooltips[tag] ?: ""
    }
    return when{
        holdRegex.matches(tag) -> "Weapon will stop firing if ship flux exceeds ${(extractRegexThreshold(holdRegex, tag) *100f).toInt()}%."
        pdFluxRegex.matches(tag) -> "Weapon will act as PD mode while ship flux > ${(extractRegexThreshold(pdFluxRegex, tag) *100f).toInt()}%."
        else -> ""
    }
}

fun createTag(name: String, weapon: WeaponAPI) : WeaponAITagBase?{
    when{
        holdRegex.matches(name) -> return FluxTag(weapon, extractRegexThreshold(holdRegex, name))
        pdFluxRegex.matches(name) -> return PDAtFluxThresholdTag(weapon, extractRegexThreshold(holdRegex, name))
    }
    return when (name){
        "PD" -> PDTag(weapon)
        "NoPD" -> NoPDTag(weapon)
        "Fighter" -> FighterTag(weapon)
        "AvoidShields" -> AvoidShieldsTag(weapon)
        "TargetShields" -> TargetShieldsTag(weapon)
        "NoFighters" -> NoFightersTag(weapon)
        "ConserveAmmo" -> ConserveAmmoTag(weapon, Settings.conserveAmmo())
        "Opportunist" -> OpportunistTag(weapon)
        else -> {
            Global.getLogger(WeaponControlPlugin.Companion::class.java).error("Unknown weapon tag: $name!")
            null
        }
    }
}

fun tagNameToRegexName(tag: String) : String{
    return when{
        holdRegex.matches(tag) -> "Hold(Flx>N%)"
        pdFluxRegex.matches(tag) -> "PD(Flx>N%)"
        else -> tag
    }
}

val tagIncompatibility = mapOf(
    "PD" to listOf("Fighter", "Opportunist", "NoPD", "PD(Flx>N%)"),
    "Fighter" to listOf("PD", "NoFighters", "Opportunist", "NoPD", "PD(Flx>N%)"),
    "NoPD" to listOf("PD", "Fighter", "PD(Flx>N%)"),
    "AvoidShields" to listOf("TargetShields"),
    "TargetShields" to listOf("AvoidShields"),
    "NoFighters" to listOf("Fighter", "Opportunist"),
    "Opportunist" to listOf("Fighter", "PD", "NoFighters", "PD(Flx>N%)"),
    "PD(Flx>N%)" to listOf("Fighter", "Opportunist", "NoPD", "PD")
)

fun isIncompatibleWithExistingTags(tag: String, existingTags: List<String>) : Boolean{
    val modTag = tagNameToRegexName(tag)
    if (tagIncompatibility.containsKey(modTag)){
        return existingTags.map { tagNameToRegexName(it) }.any { tagIncompatibility[modTag]?.contains(it) == true }
    }
    return false
}

fun createTags(names: List<String>, weapon: WeaponAPI) : List<WeaponAITagBase>{
    return names.mapNotNull { createTag(it, weapon) }
}