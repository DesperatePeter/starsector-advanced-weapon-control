package com.dp.advancedgunnerycontrol.typesandvalues

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.utils.InShipShipModeStorage
import com.dp.advancedgunnerycontrol.utils.doesShipHaveLocalShipModes
import com.dp.advancedgunnerycontrol.utils.generateUniversalFleetMemberId
import com.dp.advancedgunnerycontrol.weaponais.shipais.*
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.combat.ai.BasicShipAI
import java.lang.ref.WeakReference

enum class ShipModes {
    DEFAULT, FORCE_AUTOFIRE, SHIELDS_OFF, VENT, VENT_AGGRESSIVE, RETREAT, NO_SYSTEM, SHIELDS_UP
}

const val defaultShipMode = "DEFAULT"

val shipModeFromString = mapOf(
    "DEFAULT" to ShipModes.DEFAULT,
    "ForceAF" to ShipModes.FORCE_AUTOFIRE,
    "LowShields" to ShipModes.SHIELDS_OFF,
    "Vent(Flx>75%)" to ShipModes.VENT,
    "VntA(Flx>25%)" to ShipModes.VENT_AGGRESSIVE,
    "Run(HP<50%)" to ShipModes.RETREAT,
    "NoSystem" to ShipModes.NO_SYSTEM,
    "ShieldsUp" to ShipModes.SHIELDS_UP
)

val shipModeToString = shipModeFromString.map { it.value to it.key }.toMap()

val detailedShipModeDescriptions = mapOf(
    ShipModes.DEFAULT to "Overwrites all other ship modes and forces default ship AI. Use to quickly turn off all ship mode tags.",
    ShipModes.FORCE_AUTOFIRE to "Forces autofire for all weapon groups. Use this to make ships obey all modes literally (99% of the time)." +
            "\nUse with caution and make sure to combine with HoldFire-suffixes to prevent the ship from fluxing out.",
    ShipModes.SHIELDS_OFF to "Force turn off the shield when ship flux exceeds ${(Settings.shieldsOffThreshold() * 100f).toInt()}%. " +
            "Make sure you have enough armor/PD to pull this off.",
    ShipModes.VENT to "Vent when ship flux exceeds ${(Settings.ventFluxThreshold() * 100f).toInt()}%. The ship will try to evaluate " +
            "the situation and only vent if it believes" +
            " that it will survive doing so.\nThe ship will feel safer if it has high armor/hull and enemies lack high DPS HE weapons" +
            " and finisher missiles and/or if there are many allies nearby. Works best on big, heavily armored, ships.",
    ShipModes.VENT_AGGRESSIVE to "Similar to ${shipModeToString[ShipModes.VENT]}, but at " +
            "${(Settings.aggressiveVentFluxThreshold() * 100f).toInt()}% flux and with much less concern for" +
            " the ship's survival. It will also prevent the AI from backing off while venting. Use with caution!",
    ShipModes.RETREAT to "Order a retreat command to the ship if hull < ${(Settings.retreatHullThreshold() * 100f).toInt()}%. This WILL use a CP.",
    ShipModes.NO_SYSTEM to "Ship will not use its ship system.",
    ShipModes.SHIELDS_UP to "Ship will not turn its shields off while flux < 90% and enemies are within weapon range."
).withDefault { it.toString() }

private fun generateCommander(mode: ShipModes, ship: ShipAPI): ShipCommandGenerator {
    return when (mode) {
        ShipModes.FORCE_AUTOFIRE -> AutofireShipAI(ship)
        ShipModes.SHIELDS_OFF -> ShieldsOffShipAI(ship, Settings.shieldsOffThreshold())
        ShipModes.VENT -> VentShipAI(ship, Settings.ventFluxThreshold(), Settings.ventSafetyFactor(), false)
        ShipModes.VENT_AGGRESSIVE -> VentShipAI(
            ship,
            Settings.aggressiveVentFluxThreshold(),
            Settings.aggressiveVentSafetyFactor(),
            true
        )
        ShipModes.RETREAT -> RetreatShipAI(ship, Settings.retreatHullThreshold())
        ShipModes.NO_SYSTEM -> NoSystemAI(ship)
        ShipModes.SHIELDS_UP -> ShieldsUpAI(ship, 0.9f)
        else -> ShipCommandGenerator(ship)
    }
}

fun assignShipMode(modes: List<String>, ship: ShipAPI, forceAssign: Boolean = false) {
    if (ship.shipAI == null) return
    ship.resetDefaultAI()
    if(ship.customData.containsKey(Values.CUSTOM_SHIP_DATA_SHIP_AI_KEY)){
        ship.customData.remove(Values.CUSTOM_SHIP_DATA_SHIP_AI_KEY)
    }
    val shipModes = modes.mapNotNull { shipModeFromString[it] }
    if (!forceAssign && (shipModes.contains(ShipModes.DEFAULT) || shipModes.isEmpty())) return

    val baseAI = ship.shipAI ?: return

    val commanders = shipModes.map { generateCommander(it, ship) }
    val customAI = CustomShipAI(baseAI, ship, commanders)
    ship.setCustomData(Values.CUSTOM_SHIP_DATA_SHIP_AI_KEY, WeakReference(customAI))
    ship.shipAI = customAI
}

fun hasCustomAI(ship: ShipAPI) : Boolean{
    if (!ship.customData.containsKey(Values.CUSTOM_SHIP_DATA_SHIP_AI_KEY)) return false
    if (ship.shipAI is BasicShipAI) return false
    return (ship.customData[Values.CUSTOM_SHIP_DATA_SHIP_AI_KEY] as? WeakReference<*>)?.get() != null
}

fun getCustomShipAI(ship: ShipAPI) : CustomShipAI?{
    if(!hasCustomAI(ship)) return null
    return ((ship.customData[Values.CUSTOM_SHIP_DATA_SHIP_AI_KEY] as? WeakReference<*>)?.get() as? CustomShipAI)
}

fun persistShipModes(shipId: String, loadoutIndex: Int, tags: List<String>) {
    Settings.shipModeStorage[loadoutIndex].modesByShip[shipId] = mutableMapOf()
    tags.forEachIndexed { index, s ->
        Settings.shipModeStorage[loadoutIndex].modesByShip[shipId]?.set(index, s)
    }
}

fun saveShipModesInShip(ship: ShipAPI, tags: List<String>, storageIndex: Int) {
    if (!ship.customData.containsKey(Values.CUSTOM_SHIP_DATA_SHIP_MODES_KEY)) {
        ship.setCustomData(Values.CUSTOM_SHIP_DATA_SHIP_MODES_KEY, InShipShipModeStorage())
    }
    (ship.customData[Values.CUSTOM_SHIP_DATA_WEAPONS_TAG_KEY] as? InShipShipModeStorage)?.modes?.set(storageIndex,
        tags.toMutableList()
    )
}

fun saveShipModes(ship: ShipAPI, loadoutIndex: Int, tags: List<String>) {
    if (Settings.enableCombatChangePersistance()) {
        val shipId = generateUniversalFleetMemberId(ship)
        persistShipModes(shipId, loadoutIndex, tags)
    } else {
        saveShipModesInShip(ship, tags, loadoutIndex)
    }
}

fun loadPersistedShipModes(shipId: String, loadoutIndex: Int): List<String> {
    return Settings.shipModeStorage[loadoutIndex].modesByShip[shipId]?.values?.toList() ?: emptyList()
}

fun loadShipModesFromShip(ship: ShipAPI, storageIndex: Int): List<String> {
    return (ship.customData[Values.CUSTOM_SHIP_DATA_SHIP_MODES_KEY] as? InShipShipModeStorage)?.modes?.get(storageIndex)
        ?: emptyList()
}

fun loadShipModes(ship: ShipAPI, loadoutIndex: Int): List<String> {
    if (Settings.enableCombatChangePersistance() || !doesShipHaveLocalShipModes(ship, loadoutIndex)) {
        val shipId = generateUniversalFleetMemberId(ship)
        return loadPersistedShipModes(shipId, loadoutIndex)
    }
    return loadShipModesFromShip(ship, loadoutIndex)
}