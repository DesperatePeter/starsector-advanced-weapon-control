package com.dp.advancedgunnerycontrol.shipmodes

import com.dp.advancedgunnerycontrol.settings.Settings

fun officialShipModeNames(): List<String> = listOf(
    "PreAim",
    "Vent(TF>75%,S=2.0)",
    "Vent(TF>25%,S=0.25)",
    "LowShield(TF>50%)",
    "ShieldUp(TF<90%)",
    "RunCR(CR<50%)",
    "RunHP(HP<50%)",
    "Personality(Steady)",
    "DisableSystem",
    "SpamSystem",
    "Charge",
    "FarAway",
    "StayAway",
    "NeverVent",
)

fun classicShipModeNames(): List<String> = officialShipModeNames()

fun fullShipModeNames(): List<String> = officialShipModeNames()

fun classicShipModeList(): List<ShipModes> = classicShipModeNames().mapNotNull { parseShipMode(it)?.mode }.distinct()

fun fullShipModeList(): List<ShipModes> = fullShipModeNames().mapNotNull { parseShipMode(it)?.mode }.distinct()

val detailedShipModeDescriptions = mapOf(
    ShipModes.DEFAULT to "Clears other ship modes and restores default ship AI.",
    ShipModes.FORCE_AUTOFIRE to "Forces autofire for all weapon groups. Use this when ships need to obey weapon tags literally." +
            "\nUse with caution and combine with HoldFire tags to prevent the ship from fluxing out.",
    ShipModes.PERSONALITY_OVERRIDE to "Overrides the ship AI personality while this ship mode is active. The personality can be edited in the custom ship-mode editor.",
    ShipModes.PRE_AIM to "Lets custom weapon AI aim at the ship's current target even when that target is out of weapon range. This minimizes time-to-bear without changing fire permission.",
    ShipModes.SHIELDS_OFF to "Turns shields off when ship flux exceeds ${(Settings.shieldOffThreshold() * 100f).toInt()}%. " +
            "Make sure you have enough armor/PD to pull this off.",
    ShipModes.VENT to "Vents when ship flux exceeds ${(Settings.ventFluxThreshold() * 100f).toInt()}%. The ship tries to evaluate " +
            "the situation and only vent if it believes" +
            " that it will survive doing so.\nThe ship will feel safer if it has high armor/hull and enemies lack high DPS HE weapons" +
            " and finisher missiles, or if there are many allies nearby. Works best on big, heavily armored ships.",
    ShipModes.VENT_AGGRESSIVE to "Legacy aggressive vent alias for ${shipModeToString[ShipModes.VENT]}. It vents at " +
            "${(Settings.aggressiveVentFluxThreshold() * 100f).toInt()}% flux and with much less concern for" +
            " the ship's survival. It will also prevent the AI from backing off while venting. Use with caution!",
    ShipModes.RETREAT to "Orders the ship to retreat when hull is below ${(Settings.retreatHullThreshold() * 100f).toInt()}%. This uses one command point.",
    ShipModes.CR_RETREAT to "Orders the ship to retreat when combat readiness crosses an enabled threshold. By default this checks 75%, 50%, and 25% CR, and each threshold can be disabled in the custom ship-mode editor.",
    ShipModes.HULL_RETREAT to "Orders the ship to retreat when hull crosses an enabled threshold. By default this checks 75%, 50%, and 25% hull, and each threshold can be disabled in the custom ship-mode editor.",
    ShipModes.NO_SYSTEM to "Prevents the ship from using its ship system.",
    ShipModes.SHIELDS_UP to "Keeps shields up while flux is below 90% and enemies are within weapon range.",
    ShipModes.SHIELDS_UP_PLUS to "Keeps shields up until flux reaches 100%.",
    ShipModes.STAY_AWAY to "Moves backward away from enemies that get too close.",
    ShipModes.FAR_AWAY to "Experimental. Tries to stay far away from all enemies." +
            "\nWhen relevant enemy ships are nearby, the ship will analyze the enemy ship density and select a point " +
            "where there are fewest enemies both at the point and on the route to the point and try to move there.",
    ShipModes.SPAM_SYSTEM to "Uses the ship system whenever it is available.",
    ShipModes.CHARGE to "Accelerates toward the current target until all weapons are in range." +
            "\nCaution: may cause suicidal behavior.",
    ShipModes.NEVER_VENT to "Prevents the ship from actively venting flux.",
).withDefault { it.toString() }

fun shipModeDescription(modeName: String): String {
    val parsed = parseShipMode(modeName) ?: return modeName
    val baseDescription = detailedShipModeDescriptions[parsed.mode] ?: modeName
    val customThreshold = when (parsed.mode) {
        ShipModes.SHIELDS_OFF -> parsed.fluxThreshold?.let { "Uses this custom threshold: shields off above ${(it * 100f).toInt()}% total flux." }
        ShipModes.PERSONALITY_OVERRIDE -> parsed.personality?.let { "Uses this custom personality: $it." }
        ShipModes.VENT -> parsed.fluxThreshold?.let {
            val safetyText = parsed.ventSafetyFactor?.let { safety -> " Safety factor ${formatVentSafetyFactor(safety)}." }.orEmpty()
            val aggressiveText = if (parsed.aggressiveVent) " Aggressive venting enabled." else ""
            "Uses this custom threshold: vent above ${(it * 100f).toInt()}% total flux.$safetyText$aggressiveText"
        }
        ShipModes.VENT_AGGRESSIVE -> parsed.fluxThreshold?.let { "Uses this custom threshold: aggressively vent above ${(it * 100f).toInt()}% total flux." }
        ShipModes.RETREAT -> parsed.hullThreshold?.let { "Uses this custom threshold: retreat below ${(it * 100f).toInt()}% hull." }
        ShipModes.CR_RETREAT -> parsed.crThresholds.takeIf { it.isNotEmpty() }?.let { thresholds ->
            val rendered = thresholds.map { threshold -> threshold?.let { "${(it * 100f).toInt()}%" } ?: "disabled" }
                .joinToString(" / ")
            val directRetreatText = if (parsed.directRetreat == true) " Uses direct retreat ordering." else ""
            "Uses these custom CR thresholds: $rendered.$directRetreatText"
        }
        ShipModes.HULL_RETREAT -> parsed.hullThresholds.takeIf { it.isNotEmpty() }?.let { thresholds ->
            val rendered = thresholds.map { threshold -> threshold?.let { "${(it * 100f).toInt()}%" } ?: "disabled" }
                .joinToString(" / ")
            val directRetreatText = if (parsed.directRetreat == true) " Uses direct retreat ordering." else ""
            "Uses these custom hull thresholds: $rendered.$directRetreatText"
        }
        ShipModes.SHIELDS_UP -> parsed.fluxThreshold?.let { "Uses this custom threshold: keep shields up below ${(it * 100f).toInt()}% total flux." }
        ShipModes.SHIELDS_UP_PLUS -> parsed.fluxThreshold?.let { "Uses this custom threshold: keep shields up below ${(it * 100f).toInt()}% total flux." }
        else -> null
    }
    return customThreshold?.let { "$baseDescription\n$it" } ?: baseDescription
}