package com.dp.advancedgunnerycontrol.typesandvalues

import com.dp.advancedgunnerycontrol.WeaponControlPlugin
import com.dp.advancedgunnerycontrol.gui.isEverythingBlacklisted
import com.dp.advancedgunnerycontrol.gui.isElligibleForPD
import com.dp.advancedgunnerycontrol.gui.usesAmmo
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.mapBooleanToSpecificString
import com.dp.advancedgunnerycontrol.weaponais.tags.*
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI

val pdTags = listOf("PD", "NoPD", "PD(Flx>N%)", "NoMissiles")
val ammoTags = listOf("ConserveAmmo", "ConservePDAmmoTag")

val holdRegex = Regex("Hold\\(Flx>(\\d+)%\\)")
val pdFluxRegex = Regex("PD\\(Flx>(\\d+)%\\)")
val avoidArmorRegex = Regex("AvdArmor\\((\\d+)%\\)")
val panicFireRegex = Regex("Panic\\(H<(\\d+)%\\)")
val rangeRegex = Regex("Range<(\\d+)%")

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
    "PrioritisePD" to "Weapon will always prioritise from small to large (Missiles > fighters > small ships > big ships)",
    "NoPD" to "Forbids targeting missiles and prioritizes ships over fighters.",
    "Fighter" to "Restricts targeting to fighters.",
    "AvoidShields" to "Weapon will prioritize targets without shields, flanked shields or high flux/shields off. Shields of fighters will ${mapBooleanToSpecificString(Settings.ignoreFighterShields(), "", "not")} be ignored (configurable in settings)",
    "TargetShields" to "Weapon will prioritize shooting shields. Will stop firing against enemies with very high flux. Shields of fighters will ${mapBooleanToSpecificString(Settings.ignoreFighterShields(), "", "not")} be ignored (configurable in settings)" +
            "\nTip: Keep one kinetic weapon on default to keep up pressure.",
    "TgtShields+" to "As TargetShields, but will always shoot when shields are up and not flanked. (experimental). Shields of fighters will ${mapBooleanToSpecificString(Settings.ignoreFighterShields(), "", "not")} be ignored (configurable in settings)",
    "AvdShields+" to "As AvoidShields, but will never fire when shields are up and not flanked. (experimental). Shields of fighters will ${mapBooleanToSpecificString(Settings.ignoreFighterShields(), "", "not")} be ignored (configurable in settings)",
    "NoFighters" to "Weapon won't target fighters.",
    "ConserveAmmo" to "Weapon will be much more hesitant to fire when ammo below ${(Settings.conserveAmmo()*100f).toInt()}%.",
    "ConservePDAmmo" to "Weapon will only fire at maximum ROF when the target is of type PD and ammo is below ${(Settings.conservePDAmmo()*100f).toInt()}%.",
    "Opportunist" to "Weapon will be more hesitant to fire and won't target missiles or fighters. Use for e.g. limited ammo weapons.",
    "AvoidDebris" to "Weapon will not fire when the shot is blocked by debris/asteroids." +
            "\nNote: This only affects the custom AI and the Opportunist mode already includes this option.",
    "BigShips" to "Weapon will ignore missiles and prioritize big ships" +
            if(Settings.strictBigSmall()) " and won't fire at anything smaller than destroyers." else "",
    "SmallShips" to "Weapon will ignore missiles and prioritize small ships (including fighters)" +
            if(Settings.strictBigSmall()) " and won't fire at anything bigger than destroyers." else "",
    "ForceAF" to "Will force AI-controlled ships to set this group to autofire, like the ForceAF ship mode does to all groups." +
            "\nNote: This will modify the ShipAI, as the Starsector API doesn't allow to directly set a weapon group to autofire." +
            "\n      The ShipAI might still try to select this weapon group, but will be forced to deselect it again.",
    "AvoidPhased" to "Weapon will ignore phase-ships unless they are unable to avoid the shot by phasing (due to flux or cooldown).",
    "ShipTarget" to "Weapon will only fire at the selected ship target (R-Key). I like to use this for regenerating missiles.",
    "TargetShieldsAtFT" to "As TargetShields but will allow targeting of anything when flux is below ${(Settings.targetShieldsAtFT()*100f).toInt()}%. Shields of fighters will ${mapBooleanToSpecificString(Settings.ignoreFighterShields(), "", "not")} be ignored (configurable in settings)",
    "AvoidShieldsAtFT" to "As AvoidShields but will allow targeting of anything when flux is below ${(Settings.avoidShieldsAtFT()*100f).toInt()}%. Shields of fighters will ${mapBooleanToSpecificString(Settings.ignoreFighterShields(), "", "not")} be ignored (configurable in settings)",
    "NoMissiles" to "Weapon won't target missiles."
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
        rangeRegex.matches(tag) -> "Weapon will only target and fire at targets if they are closer than ${(extractRegexThreshold(
            rangeRegex, tag) * 100f).toInt()}% of weapon range." +
                "\nThis is useful for weapons (especially missiles) with slow projectiles, such as e.g. sabots."
        else -> ""
    }
}

var unknownTagWarnCounter = 0
fun createTag(name: String, weapon: WeaponAPI) : WeaponAITagBase?{
    when{
        holdRegex.matches(name) -> return FluxTag(weapon, extractRegexThreshold(holdRegex, name))
        pdFluxRegex.matches(name) -> return PDAtFluxThresholdTag(weapon, extractRegexThreshold(pdFluxRegex, name))
        avoidArmorRegex.matches(name) -> return AvoidArmorTag(weapon, extractRegexThreshold(avoidArmorRegex, name))
        panicFireRegex.matches(name) -> return PanicFireTag(weapon, extractRegexThreshold(panicFireRegex, name))
        rangeRegex.matches(name) -> return RangeTag(weapon, extractRegexThreshold(rangeRegex, name))
    }
    return when (name){
        "PD"                -> PDTag(weapon)
        "PrioritisePD"      -> PrioritisePDTag(weapon)
        "NoPD"              -> NoPDTag(weapon)
        "Fighter"           -> FighterTag(weapon)
        "AvoidShields"      -> AvoidShieldsTag(weapon)
        "TargetShields"     -> TargetShieldsTag(weapon)
        "AvdShields+"       -> AvoidShieldsTag(weapon, 0.02f)
        "TgtShields+"       -> TargetShieldsTag(weapon, 0.01f)
        "NoFighters"        -> NoFightersTag(weapon)
        "ConserveAmmo"      -> ConserveAmmoTag(weapon, Settings.conserveAmmo())
        "ConservePDAmmo"    -> ConservePDAmmoTag(weapon, Settings.conservePDAmmo())
        "Opportunist"       -> OpportunistTag(weapon)
        "AvoidDebris"       -> AvoidDebrisTag(weapon)
        "BigShips"          -> BigShipTag(weapon)
        "SmallShips"        -> SmallShipTag(weapon)
        "ForceAF"           -> ForceAutofireTag(weapon)
        "AvoidPhased"       -> PhaseTag(weapon)
        "ShipTarget"        -> ShipTargetTag(weapon)
        "TargetShieldsAtFT" -> TargetShieldsAtFTTag(weapon)
        "AvoidShieldsAtFT"  -> AvoidShieldsAtFTTag(weapon)
        "NoMissiles"        -> NoMissilesTag(weapon)
        else -> {
            unknownTagWarnCounter++
            when{
                unknownTagWarnCounter < 10 -> Global.getLogger(WeaponControlPlugin.Companion::class.java).warn("Unknown weapon tag: $name! Will be ignored.")
                unknownTagWarnCounter == 10 -> Global.getLogger(WeaponControlPlugin.Companion::class.java).warn(
                    "Unknown weapon tag: $name! Future warnings of this type will be skipped.")
            }
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
        rangeRegex.matches(tag) -> "Range"
        else -> tag
    }
}

val tagIncompatibility = mapOf(
    "PD" to listOf("Fighter", "Opportunist", "NoPD", "PD(Flx>N%)", "BigShips", "SmallShips", "ConservePDAmmo", "PrioritisePD"),
    "PrioritisePD" to listOf("Opportunist", "NoPD", "BigShips", "SmallShips", "Fighter", "PD"),
    "Fighter" to listOf("PD", "NoFighters", "Opportunist", "NoPD", "PD(Flx>N%)", "BigShips", "SmallShips", "PrioritisePD", "ConservePDAmmo",),
    "NoPD" to listOf("PD", "Fighter", "PD(Flx>N%)", "PrioritisePD", "ConservePDAmmo"),
    "AvoidShields" to listOf("TargetShields", "TgtShields+", "AvdShields+", "AvoidShieldsAtFT", "TargetShieldsAtFT"),
    "TargetShields" to listOf("AvoidShields", "AvdShields+", "TgtShields+", "AvoidShieldsAtFT", "TargetShieldsAtFT"),
    "TgtShields+" to listOf("AvoidShields", "AvdShields+", "TargetShields", "AvoidShieldsAtFT", "TargetShieldsAtFT"),
    "AvdShields+" to listOf("TargetShields", "TgtShields+", "AvoidShields", "AvoidShieldsAtFT", "TargetShieldsAtFT"),
    "AvoidShieldsAtFT" to listOf("AvoidShields", "AvdShields+", "TargetShields", "TgtShields+", "TargetShieldsAtFT"),
    "TargetShieldsAtFT" to listOf("AvoidShields", "AvdShields+", "TargetShields", "TgtShields+", "AvoidShieldsAtFT"),
    "NoFighters" to listOf("Fighter", "Opportunist"),
    "ConservePDAmmo" to listOf("PD", "Fighter", "NoPD"),
    "Opportunist" to listOf("Fighter", "PD", "NoFighters", "PD(Flx>N%)", "PrioritisePD", "ConservePDAmmo", "NoMissiles"),
    "PD(Flx>N%)" to listOf("Fighter", "Opportunist", "NoPD", "PD", "BigShips", "SmallShips"),
    "SmallShips" to listOf("BigShips", "PD", "Fighter", "PD(Flx>N%)", "PrioritisePD"),
    "BigShips" to listOf("SmallShips", "PD", "Fighter", "PD(Flx>N%)", "PrioritisePD"),
    "NoMissiles" to listOf("Opportunist")
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