package com.dp.advancedgunnerycontrol.typesandvalues

import com.dp.advancedgunnerycontrol.WeaponControlPlugin
import com.dp.advancedgunnerycontrol.gui.isEverythingBlacklisted
import com.dp.advancedgunnerycontrol.gui.isElligibleForPD
import com.dp.advancedgunnerycontrol.gui.usesAmmo
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.tags.*
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI

val pdTags = listOf("PD", "NoPD", "PD(Flx>N%)")

val ammoTags = listOf("ConserveAmmo")

val holdRegex = Regex("Hold\\(Flx>(\\d+)%\\)")
val pdFluxRegex = Regex("PD\\(Flx>(\\d+)%\\)")
val avoidArmorRegex = Regex("AvdArmor\\((\\d+)%\\)")
val panicFireRegex = Regex("Panic\\(H<(\\d+)%\\)")

fun extractRegexThreshold(regex: Regex, name: String) : Float{
    return (regex.matchEntire(name)?.groupValues?.get(1)?.toFloat()  ?: 0f) / 100f
}

fun shouldTagBeDisabled(groupIndex: Int, sh: FleetMemberAPI, tag: String) : Boolean{
    val modTag = tagNameToRegexName(tag)
    if(pdTags.contains(modTag) && !isElligibleForPD(groupIndex, sh)) return true
    if(ammoTags.contains(modTag) && !usesAmmo(groupIndex, sh)) return true
    if(isEverythingBlacklisted(groupIndex, sh)) return true
    return false
}

val tagTooltips = mapOf(
    "PD" to "Restricts targeting to fighters and missiles.",
    "NoPD" to "Forbids targeting missiles and prioritizes ships over fighters.",
    "Fighter" to "Restricts targeting to fighters.",
    "AvoidShields" to "Weapon will prioritize targets without shields, flanked shields or high flux/shields off.",
    "TargetShields" to "Weapon will prioritize shooting shields. Will stop firing against enemies with very high flux." +
            "\nTip: Keep one kinetic weapon on default to keep up pressure.",
    "TgtShields+" to "As TargetShields, but will always shoot when shields are up and not flanked. (experimental)",
    "AvdShields+" to "As AvoidShields, but will never fire when shields are up and not flanked. (experimental)",
    "NoFighters" to "Weapon won't target fighters.",
    "ConserveAmmo" to "Weapon will be much more hesitant to fire when ammo below ${(Settings.conserveAmmo()*100f).toInt()}%.",
    "Opportunist" to "Weapon will be more hesitant to fire and won't target missiles or fighters. Use for e.g. limited ammo weapons.",
    "AvoidDebris" to "Weapon will not fire when the shot is blocked by debris/asteroids." +
            "\nNote: This only affects the custom AI and the Opportunist mode already includes this option.",
    "BigShips" to "Weapon will ignore missiles and prioritize big ships" +
            if(Settings.strictBigSmall()) " and won't fire at anything smaller than destroyers." else "",
    "SmallShips" to "Weapon will ignore missiles and prioritize small ships" +
            if(Settings.strictBigSmall()) " and won't fire at anything bigger than destroyers." else "",
    "ForceAF" to "Will force AI-controlled ships to set this group to autofire, like the ForceAF ship mode does to all groups." +
            "\nNote: This will modify the ShipAI, as the Starsector API doesn't allow to directly set a weapon group to autofire." +
            "\n      The ShipAI might still try to select this weapon group, but will be forced to deselect it again."
)

fun getTagTooltip(tag: String) : String{
    if (tagTooltips.containsKey(tag)){
        return tagTooltips[tag] ?: ""
    }
    return when{
        holdRegex.matches(tag) -> "Weapon will stop firing if ship flux exceeds ${(extractRegexThreshold(holdRegex, tag) *100f).toInt()}%."
        pdFluxRegex.matches(tag) -> "Weapon will act as PD mode while ship flux > ${(extractRegexThreshold(pdFluxRegex, tag) *100f).toInt()}%."
        avoidArmorRegex.matches(tag) -> "Weapon will fire when the shot is likely to hit shields (as TargetShields) OR a section of hull " +
                "\nwhere the armor is low enough to achieve at least ${(extractRegexThreshold(avoidArmorRegex, tag) *100f).toInt()}% " +
                "effectiveness vs armor." +
                "\nCombine with AvoidShields to also avoid shields. (experimental)"
        panicFireRegex.matches(tag) -> "Weapon will blindly fire without considering if/what the shot will hit as long as the ship" +
                " hull level is below ${(extractRegexThreshold(panicFireRegex, tag) *100f).toInt()}%." +
                "\nFor AI-controlled ships, this will put the weapon group into ForceAF-mode once the hull threshold has been reached."
        else -> ""
    }
}

fun createTag(name: String, weapon: WeaponAPI) : WeaponAITagBase?{
    when{
        holdRegex.matches(name) -> return FluxTag(weapon, extractRegexThreshold(holdRegex, name))
        pdFluxRegex.matches(name) -> return PDAtFluxThresholdTag(weapon, extractRegexThreshold(pdFluxRegex, name))
        avoidArmorRegex.matches(name) -> return AvoidArmorTag(weapon, extractRegexThreshold(avoidArmorRegex, name))
        panicFireRegex.matches(name) -> return PanicFireTag(weapon, extractRegexThreshold(panicFireRegex, name))
    }
    return when (name){
        "PD"            -> PDTag(weapon)
        "NoPD"          -> NoPDTag(weapon)
        "Fighter"       -> FighterTag(weapon)
        "AvoidShields"  -> AvoidShieldsTag(weapon)
        "TargetShields" -> TargetShieldsTag(weapon)
        "AvdShields+"   -> AvoidShieldsTag(weapon, 0.01f)
        "TgtShields+"   -> TargetShieldsTag(weapon, 0.99f)
        "NoFighters"    -> NoFightersTag(weapon)
        "ConserveAmmo"  -> ConserveAmmoTag(weapon, Settings.conserveAmmo())
        "Opportunist"   -> OpportunistTag(weapon)
        "AvoidDebris"   -> AvoidDebrisTag(weapon)
        "BigShips"      -> BigShipTag(weapon)
        "SmallShips"    -> SmallShipTag(weapon)
        "ForceAF"       -> ForceAutofireTag(weapon)
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
        avoidArmorRegex.matches(tag) -> "AvoidArmor"
        panicFireRegex.matches(tag) -> "Panic"
        else -> tag
    }
}

val tagIncompatibility = mapOf(
    "PD" to listOf("Fighter", "Opportunist", "NoPD", "PD(Flx>N%)", "BigShips", "SmallShips"),
    "Fighter" to listOf("PD", "NoFighters", "Opportunist", "NoPD", "PD(Flx>N%)", "BigShips", "SmallShips"),
    "NoPD" to listOf("PD", "Fighter", "PD(Flx>N%)"),
    "AvoidShields" to listOf("TargetShields", "TgtShields+", "AvdShields+"),
    "TargetShields" to listOf("AvoidShields", "AvdShields+", "TgtShields+"),
    "TgtShields+" to listOf("AvoidShields", "AvdShields+", "TargetShields"),
    "AvdShields+" to listOf("TargetShields", "TgtShields+", "AvoidShields"),
    "NoFighters" to listOf("Fighter", "Opportunist"),
    "Opportunist" to listOf("Fighter", "PD", "NoFighters", "PD(Flx>N%)"),
    "PD(Flx>N%)" to listOf("Fighter", "Opportunist", "NoPD", "PD", "BigShips", "SmallShips"),
    "SmallShips" to listOf("BigShips", "PD", "Fighter", "PD(Flx>N%)"),
    "BigShips" to listOf("SmallShips", "PD", "Fighter", "PD(Flx>N%)")
)

fun isIncompatibleWithExistingTags(tag: String, existingTags: List<String>) : Boolean{
    val modTag = tagNameToRegexName(tag)
    if (tagIncompatibility.containsKey(modTag)){
        return existingTags.map { tagNameToRegexName(it) }.any { tagIncompatibility[modTag]?.contains(it) == true }
    }
    return false
}

fun createTags(names: List<String>, weapon: WeaponAPI) : List<WeaponAITagBase>{
    return names.mapNotNull { createTag(it, weapon) }.filter { it.isValid() }
}