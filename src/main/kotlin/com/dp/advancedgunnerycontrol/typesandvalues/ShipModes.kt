package com.dp.advancedgunnerycontrol.typesandvalues

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.shipais.*
import com.fs.starfarer.api.combat.ShipAPI

enum class ShipModes {
    DEFAULT, FORCE_AUTOFIRE, SHIELDS_OFF, VENT, VENT_AGGRESSIVE, RETREAT, HELP
}

const val defaultShipMode = "DEFAULT"

 val shipModeFromString = mapOf(
    "DEFAULT" to ShipModes.DEFAULT,
    "ForceAutofire" to ShipModes.FORCE_AUTOFIRE,
    "ShieldsOff (Flux>50%)" to ShipModes.SHIELDS_OFF,
    "Vent (Flux>75%)" to ShipModes.VENT,
    "VentAggressive (Flux>25%)" to ShipModes.VENT_AGGRESSIVE,
    "Retreat (Hull<50%)" to ShipModes.RETREAT,
     "?" to ShipModes.HELP
)

val shipModeToString = shipModeFromString.map { it.value to it.key }.toMap()

val detailedShipModeDescriptions = mapOf(
    ShipModes.DEFAULT to "Base ship AI. Recommended most of the time.",
    ShipModes.FORCE_AUTOFIRE to "Forces autofire for all weapon groups. Use this to make ships obey all modes literally (99% of the time)." +
            " Use with caution and make sure to combine with HoldFire-suffixes to prevent the ship from fluxing out.",
    ShipModes.SHIELDS_OFF to "Force turn off the shield when ship flux exceeds ${(Settings.shieldsOffThreshold()*100f).toInt()}%. " +
            "Make sure you have enough armor/PD to pull this off.",
    ShipModes.VENT to "Vent when ship flux exceeds ${(Settings.ventFluxThreshold()*100f).toInt()}%. The ship will try to evaluate " +
            "the situation and only vent if it believes" +
            " that it will survive doing so. The ship will feel safer if it has high armor/hull and enemies lack high DPS HE weapons" +
            " and finisher missiles and/or if there are many allies nearby. This mode works best on big, heavily armored, ships.",
    ShipModes.VENT_AGGRESSIVE to "Similar to ${shipModeToString[ShipModes.VENT]}, but at " +
            "${(Settings.aggressiveVentFluxThreshold()*100f).toInt()}% flux and with much less concern for" +
            " the ship's survival. It will also prevent the AI from backing off while venting. Use with caution!",
    ShipModes.RETREAT to "Order a retreat command to the ship if hull < ${(Settings.retreatHullThreshold()*100f).toInt()}%. This WILL use a CP.",
    ShipModes.HELP to Values.HELP_TEXT
).withDefault { it.toString() }

private fun generateCommander(mode: ShipModes, ship: ShipAPI) : ShipCommandGenerator{
    return when (mode){
        ShipModes.FORCE_AUTOFIRE -> AutofireShipAI(ship)
        ShipModes.SHIELDS_OFF -> ShieldsOffShipAI(ship, Settings.shieldsOffThreshold())
        ShipModes.VENT -> VentShipAI(ship, Settings.ventFluxThreshold(), Settings.ventSafetyFactor(), false)
        ShipModes.VENT_AGGRESSIVE -> VentShipAI(ship, Settings.aggressiveVentFluxThreshold(), Settings.aggressiveVentSafetyFactor(), true)
        ShipModes.RETREAT -> RetreatShipAI(ship, Settings.retreatHullThreshold())
        else -> ShipCommandGenerator(ship)
    }
}

fun assignShipMode(modes: List<String>, ship: ShipAPI){
    if(ship.shipAI == null) return
    ship.resetDefaultAI()
    val shipModes = modes.mapNotNull { shipModeFromString[it] }
    if(shipModes.contains(ShipModes.DEFAULT) || shipModes.isEmpty()) return

    val baseAI = ship.shipAI ?: return

    val commanders = shipModes.map { generateCommander(it, ship) }

    ship.shipAI = CustomShipAI(baseAI, ship, commanders)
}