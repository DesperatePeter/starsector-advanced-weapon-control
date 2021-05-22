package com.dp.advancedgunnerycontrol.typesandvalues

import com.dp.advancedgunnerycontrol.weaponais.shipais.AutofireShipAI
import com.dp.advancedgunnerycontrol.weaponais.shipais.RetreatShipAI
import com.dp.advancedgunnerycontrol.weaponais.shipais.ShieldsOffShipAI
import com.dp.advancedgunnerycontrol.weaponais.shipais.VentShipAI
import com.fs.starfarer.api.combat.ShipAPI

enum class ShipModes {
    DEFAULT, FORCE_AUTOFIRE, SHIELDS_OFF, RETREAT, VENT
}

const val defaultShipMode = "DEFAULT"

 val shipModeFromString = mapOf(
    "DEFAULT" to ShipModes.DEFAULT,
    "ForceAutofire" to ShipModes.FORCE_AUTOFIRE,
    "ShieldsOff (Flux>50%)" to ShipModes.SHIELDS_OFF,
    "Vent (Flux>50%)" to ShipModes.VENT,
    "Retreat (Hull<50%)" to ShipModes.RETREAT
)

val detailedShipModeDescriptions = mapOf(
    ShipModes.DEFAULT to "Base game ship AI",
    ShipModes.FORCE_AUTOFIRE to "Forces autofire for all weapon groups. Use this to make ships obey all modes literally.",
    ShipModes.SHIELDS_OFF to "Force turn off the shield when ship flux exceeds 50%",
    ShipModes.VENT to "Force vent when ship flux exceeds 50%",
    ShipModes.RETREAT to "Order a retreat command to the ship if hull < 50%. This WILL use a CP."
).withDefault { it.toString() }

val shipModeToString = shipModeFromString.map { it.value to it.key }.toMap()

fun assignShipMode(mode: String, ship: ShipAPI){
    ship.resetDefaultAI()
    val baseAI = ship.shipAI ?: return
    val plugin = when (shipModeFromString[mode]){
        ShipModes.DEFAULT -> baseAI
        ShipModes.FORCE_AUTOFIRE -> AutofireShipAI(baseAI, ship)
        ShipModes.SHIELDS_OFF -> ShieldsOffShipAI(baseAI, ship)
        ShipModes.VENT -> VentShipAI(baseAI, ship)
        ShipModes.RETREAT -> RetreatShipAI(baseAI, ship)
        else -> baseAI
    }
    ship.shipAI = plugin
}