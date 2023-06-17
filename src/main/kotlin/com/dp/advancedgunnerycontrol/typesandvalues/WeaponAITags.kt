package com.dp.advancedgunnerycontrol.typesandvalues

import com.dp.advancedgunnerycontrol.WeaponControlPlugin
import com.dp.advancedgunnerycontrol.gui.isElligibleForPD
import com.dp.advancedgunnerycontrol.gui.isEverythingBlacklisted
import com.dp.advancedgunnerycontrol.gui.usesAmmo
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.utils.generateUniversalFleetMemberId
import com.dp.advancedgunnerycontrol.utils.loadPersistentTags
import com.dp.advancedgunnerycontrol.utils.persistTags
import com.dp.advancedgunnerycontrol.weaponais.mapBooleanToSpecificString
import com.dp.advancedgunnerycontrol.weaponais.tags.*
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI

// NOTE: Tag names should NOT exceed 13 characters to be able to cleanly fit on buttons!

val pdTags = listOf("PD", "NoPD", "PD(Flx>N%)", "NoMissiles")
val ammoTags = listOf("ConserveAmmo", "CnsrvPDAmmo")

val holdRegex = Regex("Hold\\(Fl?u?x?>(\\d+)%\\)")
val pdFluxRegex = Regex("PD\\(Fl?u?x?>(\\d+)%\\)")
val avoidArmorRegex = Regex("AvdArmor\\((\\d+)%\\)")
val panicFireRegex = Regex("Panic\\(H<(\\d+)%\\)")
val rangeRegex = Regex("Range<(\\d+)%")
val forceFireRegex = Regex("ForceF\\(Fl?u?x?<(\\d+)%\\)")

//val prioPdRegex = Regex("PrioP[dD]\\((\\d+)\\)")
//val prioFightersRegex = Regex("PrioFighter\\((\\d+)\\)")
//val prioMissilesRegex = Regex("PrioMissile\\((\\d+)\\)")

fun extractRegexThreshold(regex: Regex, name: String): Float {
    return (regex.matchEntire(name)?.groupValues?.get(1)?.toFloat() ?: 0f) / 100f
}

fun extractRegexThresholdAsPercentageString(regex: Regex, name: String): String {
    return "${(extractRegexThreshold(regex, name) * 100f).toInt()}%"
}

fun shouldTagBeDisabled(groupIndex: Int, sh: FleetMemberAPI, tag: String): Boolean {
    val modTag = tagNameToRegexName(tag)
    if (pdTags.contains(modTag) && !isElligibleForPD(groupIndex, sh)) return true
    if (ammoTags.contains(modTag) && !usesAmmo(groupIndex, sh)) return true
    return isEverythingBlacklisted(groupIndex, sh)
}

val priorityBoilerplateText = "\nIncreases priority ba a factor of ${Settings.prioXModifier()} (adjustable in Settigs.editme)." +
        "\nCombine multiple Prio-tags to de-prioritize everything else."

val tagTooltips = mapOf(
    "PD" to "Restricts targeting to fighters and missiles.",
    "PrioritisePD" to "Weapon will always prioritise from small to large (Missiles > fighters > small ships > big ships)." +
            if (Settings.strictBigSmall()) "\nRestricts targeting to missiles and ships smaller than cruisers." else "\nNo targeting restrictions.",
    "NoPD" to "Forbids targeting missiles and prioritizes ships over fighters.",
    "Fighter" to "Restricts targeting to fighters.",
    "AvoidShields" to "Weapon will prioritize targets without shields, flanked shields or high flux/shields off. \nShields of fighters will ${
        mapBooleanToSpecificString(
            Settings.ignoreFighterShields(),
            "",
            "not"
        )
    } be ignored (configurable in settings)" +
            "\nNo targeting restrictions.",
    "TargetShields" to "Weapon will prioritize shooting shields. Will stop firing against enemies with very high flux. \nShields of fighters will ${
        mapBooleanToSpecificString(
            Settings.ignoreFighterShields(),
            "",
            "not"
        )
    } be ignored (configurable in settings)" +
            "\nTip: Keep one kinetic weapon on default to keep up pressure." +
            "\nNo targeting restrictions.",
    "TgtShields+" to "As TargetShields, but more aggressive." +
            "\nWill only stop shooting when flanking shields or shields are disabled (experimental)." +
            "\nShields of fighters will ${
        mapBooleanToSpecificString(
            Settings.ignoreFighterShields(),
            "",
            "not"
        )
    } be ignored (configurable in settings)",
    "AvdShields+" to "As AvoidShields, but less aggressive." +
            "\nWill only shoot when flanking shields or shields are disabled (experimental)." +
            "\nShields of fighters will ${
        mapBooleanToSpecificString(
            Settings.ignoreFighterShields(),
            "",
            "not"
        )
    } be ignored (configurable in settings)",
    "NoFighters" to "Forbids targeting fighters.",
    "ConserveAmmo" to "Weapon will be much more hesitant to fire when ammo below ${(Settings.conserveAmmo() * 100f).toInt()}%." +
            "\nNo targeting restrictions.",
    "CnsrvPDAmmo" to "When ammo is below ${(Settings.conservePDAmmo() * 100f).toInt()}%, weapon will only fire when the target is a fighter/missile." +
            "\nFor non-PD weapons, only fighters will be fired upon." +
            "\nNo targeting restrictions.",
    "Opportunist" to "Weapon will be much more hesitant to fire and won't target missiles or fighters. Use for e.g. limited ammo weapons.",
    "AvoidDebris" to "Weapon will not fire when the shot is blocked by debris/asteroids." +
            "\nNote: This only affects the custom AI and the Opportunist mode already includes this option.",
    "BigShips" to "Weapon won't target missiles and prioritize big ships" +
            if (Settings.strictBigSmall()) " and won't target anything smaller than destroyers." else ".",
    "SmallShips" to "Weapon will ignore missiles and prioritize small ships (including fighters)" +
            if (Settings.strictBigSmall()) " and won't target anything bigger than destroyers." else ".",
    "ForceAF" to "Will force AI-controlled ships to set this group to autofire, like the ForceAF ship mode does to all groups." +
            "\nNote: This will modify the ShipAI, as the Starsector API doesn't allow to directly set a weapon group to autofire." +
            "\n      The ShipAI might still try to select this weapon group, but will be forced to deselect it again.",
    "AvoidPhased" to "Weapon will ignore phase-ships, unless they are unable to avoid the shot by phasing (due to flux or cooldown)." +
            "\nNo targeting restrictions.",
    "TargetPhase" to "Weapon will prioritize phase-ships. Does not care if the ship is currently phased or not." +
            "\nUseful for fast-firing weapons (e.g. beams) to keep up pressure on enemy phase coils." +
            "\nNo targeting restrictions.",
    "ShipTarget" to "Weapon will only target the selected ship target (R-Key). I like to use this for regenerating missiles.",
    "TgtShieldsFT" to "As TargetShields, but will allow targeting of anything when flux is below ${(Settings.targetShieldsAtFT() * 100f).toInt()}%. \nShields of fighters will ${
        mapBooleanToSpecificString(
            Settings.ignoreFighterShields(),
            "",
            "not"
        )
    } be ignored (configurable in settings)",
    "AvdShieldsFT" to "As AvoidShields, but will allow targeting of anything when flux is below ${(Settings.avoidShieldsAtFT() * 100f).toInt()}%. \nShields of fighters will ${
        mapBooleanToSpecificString(
            Settings.ignoreFighterShields(),
            "",
            "not"
        )
    } be ignored (configurable in settings)",
    "NoMissiles" to "Weapon won't target missiles.",
    "Overloaded" to "Weapon will only target and fire at overloaded ships.",
    "ShieldsOff" to "Simplified version of AvoidShields. Will only fire at targets that have no shields or have shields turned off.",
    "Merge" to "Press [${Settings.mergeHotkey().uppercaseChar()}] to merge all weapons with this tag into current weapon group. " +
            "\nFor player controlled ship only! Press [${Settings.mergeHotkey().uppercaseChar()}] again to undo." +
            "\nUse this tag to unleash big manually aimed barrages at your enemies!",
    "PrioFighter" to "Prioritize fighters over all other targets but target other things if no fighters present.$priorityBoilerplateText",
    "PrioMissile" to "Prioritize missiles over all other targets but target other things if no missiles present.$priorityBoilerplateText",
    "PrioShips" to "Prioritize non-fighter ships over all other targets but target other things if no ships present.$priorityBoilerplateText",
    "PrioWounded" to "Prioritize targets that have already taken lots of hull damage.",
    "BlockBeams" to "Will shoot at enemies that are shooting this ship, even when out of range. Intended mainly for the SVC Ink Spitter gun."
)

fun getTagTooltip(tag: String): String {
    if (tagTooltips.containsKey(tag)) {
        return tagTooltips[tag] ?: "No description available."
    }
    return when {
        holdRegex.matches(tag) -> "Weapon will stop firing if ship flux exceeds ${
            extractRegexThresholdAsPercentageString(
                holdRegex,
                tag
            )
        }."

        pdFluxRegex.matches(tag) -> "Weapon will act as PD mode while ship flux > ${
            extractRegexThresholdAsPercentageString(
                pdFluxRegex,
                tag
            )
        }."

        avoidArmorRegex.matches(tag) -> "Weapon will fire when the shot is likely to hit shields (as TargetShields) OR a section of hull " +
                "\nwhere the armor is low enough to achieve at least ${
                    extractRegexThresholdAsPercentageString(
                        avoidArmorRegex,
                        tag
                    )
                } " +
                "effectiveness vs armor." +
                "\nCombine with AvoidShields to also avoid shields (e.g. for frag weapons)."

        panicFireRegex.matches(tag) -> "Weapon will blindly fire without considering if/what the shot will hit as long as the ship" +
                " hull level is below ${extractRegexThresholdAsPercentageString(panicFireRegex, tag)}." +
                "\nFor AI-controlled ships, this will put the weapon group into ForceAF-mode once the hull threshold has been reached."

        rangeRegex.matches(tag) -> "Weapon will only target and fire at targets if they are closer than ${
            extractRegexThresholdAsPercentageString(
                rangeRegex, tag
            )
        } of weapon range." +
                "\nThis is useful for weapons (especially missiles) with slow projectiles, such as e.g. sabots."

        forceFireRegex.matches(tag) -> "ForceFire: Weapon will ignore firing restrictions of other tags while flux < ${
            extractRegexThresholdAsPercentageString(
                forceFireRegex, tag
            )
        }." +
                "\nNote: This will not circumvent targeting restrictions, only firing restrictions."

        else -> "No description available."
    }
}

var unknownTagWarnCounter = 0
fun createTag(name: String, weapon: WeaponAPI): WeaponAITagBase? {
    when {
        holdRegex.matches(name) -> return FluxTag(weapon, extractRegexThreshold(holdRegex, name))
        pdFluxRegex.matches(name) -> return PDAtFluxThresholdTag(weapon, extractRegexThreshold(pdFluxRegex, name))
        avoidArmorRegex.matches(name) -> return AvoidArmorTag(weapon, extractRegexThreshold(avoidArmorRegex, name))
        panicFireRegex.matches(name) -> return PanicFireTag(weapon, extractRegexThreshold(panicFireRegex, name))
        rangeRegex.matches(name) -> return RangeTag(weapon, extractRegexThreshold(rangeRegex, name))
        forceFireRegex.matches(name) -> return ForceFireTag(weapon, extractRegexThreshold(forceFireRegex, name))
    }
    return when (name) {
        "PD" -> PDTag(weapon)
        "PrioritisePD" -> PrioritisePDTag(weapon, Settings.prioXModifier())
        "NoPD" -> NoPDTag(weapon)
        "Fighter" -> FighterTag(weapon)
        "AvoidShields" -> AvoidShieldsTag(weapon)
        "TargetShields" -> TargetShieldsTag(weapon)
        "AvdShields+" -> AvoidShieldsTag(weapon, 0.02f)
        "TgtShields+" -> TargetShieldsTag(weapon, 0.01f)
        "NoFighters" -> NoFightersTag(weapon)
        "ConserveAmmo" -> ConserveAmmoTag(weapon, Settings.conserveAmmo())
        "CnsrvPDAmmo" -> ConservePDAmmoTag(weapon, Settings.conservePDAmmo())
        "Opportunist" -> OpportunistTag(weapon)
        "AvoidDebris" -> AvoidDebrisTag(weapon)
        "BigShips" -> BigShipTag(weapon)
        "SmallShips" -> SmallShipTag(weapon)
        "ForceAF" -> ForceAutofireTag(weapon)
        "AvoidPhased" -> AvoidPhaseTag(weapon)
        "TargetPhase" -> TargetPhaseTag(weapon)
        "ShipTarget" -> ShipTargetTag(weapon)
        "TgtShieldsFT" -> TargetShieldsAtFTTag(weapon)
        "AvdShieldsFT" -> AvoidShieldsAtFTTag(weapon)
        "NoMissiles" -> NoMissilesTag(weapon)
        "Overloaded" -> OverloadTag(weapon)
        "ShieldsOff" -> ShieldsOff(weapon)
        "Merge" -> MergeTag(weapon)
        "PrioFighter" -> PrioritizeFightersTag(weapon, Settings.prioXModifier())
        "PrioMissile" -> PrioritizeMissilesTag(weapon, Settings.prioXModifier())
        "PrioShips" -> PrioritizeShipsTag(weapon, Settings.prioXModifier())
        "PrioWounded" -> PrioritizeWoundedTag(weapon)
        "BlockBeams" -> InterdictBeamsTag(weapon)
        else -> {
            unknownTagWarnCounter++
            when {
                unknownTagWarnCounter < 10 -> Global.getLogger(WeaponControlPlugin.Companion::class.java)
                    .warn("Unknown weapon tag: $name! Will be ignored.")

                unknownTagWarnCounter == 10 -> Global.getLogger(WeaponControlPlugin.Companion::class.java).warn(
                    "Unknown weapon tag: $name! Future warnings of this type will be skipped."
                )
            }
            null
        }
    }
}

fun tagNameToRegexName(tag: String): String {
    return when {
        holdRegex.matches(tag) -> "Hold(Flx>N%)"
        pdFluxRegex.matches(tag) -> "PD(Flx>N%)"
        avoidArmorRegex.matches(tag) -> "AvoidArmor"
        panicFireRegex.matches(tag) -> "Panic"
        rangeRegex.matches(tag) -> "Range"
        forceFireRegex.matches(tag) -> "ForceF(Flx<N%)"
        else -> tag
    }
}

val tagIncompatibility = mapOf(
    "PD" to listOf(
        "Fighter",
        "Opportunist",
        "NoPD",
        "PD(Flx>N%)",
        "BigShips",
        "SmallShips",
        "CnsrvPDAmmo",
        "PrioritisePD"
    ),
    "PrioritisePD" to listOf("Opportunist", "NoPD", "BigShips", "SmallShips", "Fighter", "PD"),
    "Fighter" to listOf(
        "PD",
        "NoFighters",
        "Opportunist",
        "NoPD",
        "PD(Flx>N%)",
        "BigShips",
        "SmallShips",
        "PrioritisePD",
        "CnsrvPDAmmo",
    ),
    "NoPD" to listOf("PD", "Fighter", "PD(Flx>N%)", "PrioritisePD", "CnsrvPDAmmo"),
    "ShieldsOff" to listOf(
        "AvoidShields",
        "TargetShields",
        "TgtShields+",
        "AvdShields+",
        "AvdShieldsFT",
        "TgtShieldsFT"
    ),
    "AvoidShields" to listOf(
        "TargetShields",
        "TgtShields+",
        "AvdShields+",
        "AvdShieldsFT",
        "TgtShieldsFT",
        "ShieldsOff"
    ),
    "TargetShields" to listOf(
        "AvoidShields",
        "AvdShields+",
        "TgtShields+",
        "AvdShieldsFT",
        "TgtShieldsFT",
        "ShieldsOff"
    ),
    "TgtShields+" to listOf(
        "AvoidShields",
        "AvdShields+",
        "TargetShields",
        "AvdShieldsFT",
        "TgtShieldsFT",
        "ShieldsOff"
    ),
    "AvdShields+" to listOf(
        "TargetShields",
        "TgtShields+",
        "AvoidShields",
        "AvdShieldsFT",
        "TgtShieldsFT",
        "ShieldsOff"
    ),
    "AvdShieldsFT" to listOf(
        "AvoidShields",
        "AvdShields+",
        "TargetShields",
        "TgtShields+",
        "TgtShieldsFT",
        "ShieldsOff"
    ),
    "TgtShieldsFT" to listOf(
        "AvoidShields",
        "AvdShields+",
        "TargetShields",
        "TgtShields+",
        "AvdShieldsFT",
        "ShieldsOff"
    ),
    "NoFighters" to listOf("Fighter", "Opportunist"),
    "CnsrvPDAmmo" to listOf("PD", "Fighter", "NoPD"),
    "Opportunist" to listOf("Fighter", "PD", "NoFighters", "PD(Flx>N%)", "PrioritisePD", "CnsrvPDAmmo", "NoMissiles"),
    "PD(Flx>N%)" to listOf("Fighter", "Opportunist", "NoPD", "PD", "BigShips", "SmallShips"),
    "SmallShips" to listOf("BigShips", "PD", "Fighter", "PD(Flx>N%)", "PrioritisePD"),
    "BigShips" to listOf("SmallShips", "PD", "Fighter", "PD(Flx>N%)", "PrioritisePD"),
    "NoMissiles" to listOf("Opportunist"),
    "TargetPhase" to listOf("AvoidPhased"),
    "AvoidPhased" to listOf("TargetPhase")
)

fun isIncompatibleWithExistingTags(tag: String, existingTags: List<String>): Boolean {
    val modTag = tagNameToRegexName(tag)
    if (tagIncompatibility.containsKey(modTag)) {
        return existingTags.map { tagNameToRegexName(it) }.any { tagIncompatibility[modTag]?.contains(it) == true }
    }
    return false
}

fun createTags(names: List<String>, weapon: WeaponAPI): List<WeaponAITagBase> {
    return names.mapNotNull { createTag(it, weapon) }.filter { it.isValid() }
}

fun applySuggestedModes(ship: FleetMemberAPI, storageIndex: Int, allowOverriding: Boolean = true, shipId: String? = null) {
    val id = shipId ?: ship.id
    val groups = ship.variant.weaponGroups

    groups.forEachIndexed { index, group ->
        if(allowOverriding || loadPersistentTags(id, index, storageIndex).isEmpty()){
            val weaponID = group.slots.first()?.let { ship.variant.getWeaponId(it) } ?: ""
            persistTags(id, index, storageIndex, getSuggestedModesForWeaponId(weaponID))
        }
    }
}

fun getSuggestedModesForWeaponId(weaponID: String) : List<String>{
    val tagKey: String = if (Settings.getCurrentSuggestedTags().containsKey(weaponID)) {
        weaponID
    } else {
        Settings.getCurrentSuggestedTags().keys.map { Regex(it) }.find { it.matches(weaponID) }.toString()
    }
    return Settings.getCurrentSuggestedTags()[tagKey] ?: emptyList()
}