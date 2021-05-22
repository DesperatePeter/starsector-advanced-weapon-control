package com.dp.advancedgunnerycontrol.typesandvalues

import com.dp.advancedgunnerycontrol.weaponais.shipais.AutofireShipAI
import com.fs.starfarer.api.combat.ShipAPI

enum class ShipModes {
    DEFAULT, FORCE_AUTOFIRE
}

const val defaultShipMode = "DEFAULT"

 val shipModeFromString = mapOf(
    "DEFAULT" to ShipModes.DEFAULT,
    "ForceAutofire" to ShipModes.FORCE_AUTOFIRE
)

val shipModeToString = shipModeFromString.map { it.value to it.key }.toMap()

fun assignShipMode(mode: String, ship: ShipAPI){
    val baseAI = ship.shipAI ?: return
    val plugin = when (shipModeFromString[mode]){
        ShipModes.DEFAULT -> baseAI
        ShipModes.FORCE_AUTOFIRE -> AutofireShipAI(baseAI, ship)
        else -> baseAI
    }
    ship.shipAI = plugin
}