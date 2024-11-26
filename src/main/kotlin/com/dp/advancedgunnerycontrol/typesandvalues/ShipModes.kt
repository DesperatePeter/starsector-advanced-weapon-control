package com.dp.advancedgunnerycontrol.typesandvalues

import com.dp.advancedgunnerycontrol.gui.AGCGUI
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.utils.InShipShipModeStorage
import com.dp.advancedgunnerycontrol.utils.doesShipHaveLocalShipModes
import com.dp.advancedgunnerycontrol.utils.generateUniversalFleetMemberId
import com.dp.advancedgunnerycontrol.weaponais.shipais.*
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.combat.ai.BasicShipAI
import java.lang.ref.WeakReference

enum class ShipModes {
    DEFAULT, FORCE_AUTOFIRE, SHIELDS_OFF, VENT, VENT_AGGRESSIVE,
    RETREAT, NO_SYSTEM, SHIELDS_UP, SPAM_SYSTEM, CHARGE, SHIELDS_UP_PLUS,
    STAY_AWAY, FAR_AWAY, NEVER_VENT
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
    "ShieldsUp" to ShipModes.SHIELDS_UP,
    "SpamSystem" to ShipModes.SPAM_SYSTEM,
    "Charge" to ShipModes.CHARGE,
    "ShieldsUp+" to ShipModes.SHIELDS_UP_PLUS,
    "StayAway" to ShipModes.STAY_AWAY,
    "FarAway" to ShipModes.FAR_AWAY,
    "NeverVent" to ShipModes.NEVER_VENT
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
    ShipModes.SHIELDS_UP to "Ship will not turn its shields off while flux < 90% and enemies are within weapon range.",
    ShipModes.SHIELDS_UP_PLUS to "Like shields up, but until 100% flux.",
    ShipModes.STAY_AWAY to "Will move away backwards from enemies that get too close",
    ShipModes.FAR_AWAY to "EXPERIMENTAL! Will try to stay far away from all enemies." +
            "\nWhen relevant enemy ships are nearby, the ship will analyze the enemy ship density and select a point " +
            "where there are fewest enemies both at the point and on the route to the point and try to move there.",
    ShipModes.SPAM_SYSTEM to "Ship will always use the ship system when available.",
    ShipModes.CHARGE to "If the ship has a target it will accelerate towards it until all weapons are in range." +
            "\nCaution! Might cause suicidal behavior!",
    ShipModes.NEVER_VENT to "Prevents the ship from actively venting flux"
).withDefault { it.toString() }

private fun generateCommander(mode: ShipModes, ship: ShipAPI): ShipCommandGenerator {
    return when (mode) {
        ShipModes.FORCE_AUTOFIRE -> AutofireShipAI(ship)
        ShipModes.SHIELDS_OFF -> ShieldsOffShipAI(ship, Settings.shieldsOffThreshold())
        ShipModes.VENT -> VentShipAI(ship, Settings.ventFluxThreshold(), Settings.ventSafetyFactor(), false)
        ShipModes.VENT_AGGRESSIVE -> VentShipAI(ship, Settings.aggressiveVentFluxThreshold(), Settings.aggressiveVentSafetyFactor(), true)
        ShipModes.RETREAT -> RetreatShipAI(ship, Settings.retreatHullThreshold())
        ShipModes.NO_SYSTEM -> NoSystemAI(ship)
        ShipModes.SHIELDS_UP -> ShieldsUpAI(ship, 0.9f)
        ShipModes.SHIELDS_UP_PLUS -> ShieldsUpAI(ship, 1.1f)
        ShipModes.STAY_AWAY -> StayAwayAI(ship)
        ShipModes.FAR_AWAY -> StayFarAI(ship)
        ShipModes.SPAM_SYSTEM -> SpamSystemAI(ship)
        ShipModes.CHARGE -> ChargeShipAI(ship)
        ShipModes.NEVER_VENT -> NeverVentAI(ship)
        else -> ShipCommandGenerator(ship)
    }
}

fun shouldNotOverrideShipAI(ship: ShipAPI): Boolean{
    return ship.customData.containsKey(Values.CUSTOM_SHIP_DATA_DO_NOT_OVERWRITE_AI_KEY) || ship.shipAI?.javaClass?.name == Values.COOP_MOD_SHIP_AI_NAME
}

fun assignShipModes(modes: List<String>, ship: ShipAPI, forceAssign: Boolean = false) {
    if (ship.shipAI == null) return
    if(shouldNotOverrideShipAI(ship)) return
    ship.resetDefaultAI()
    if (ship.customData.containsKey(Values.CUSTOM_SHIP_DATA_SHIP_AI_KEY)) {
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

fun hasCustomAI(ship: ShipAPI): Boolean {
    if (!ship.customData.containsKey(Values.CUSTOM_SHIP_DATA_SHIP_AI_KEY)) return false
    if (ship.shipAI is BasicShipAI) return false
    return (ship.customData[Values.CUSTOM_SHIP_DATA_SHIP_AI_KEY] as? WeakReference<*>)?.get() != null
}

fun getCustomShipAI(ship: ShipAPI): CustomShipAI? {
    if (!hasCustomAI(ship)) return null
    return ((ship.customData[Values.CUSTOM_SHIP_DATA_SHIP_AI_KEY] as? WeakReference<*>)?.get() as? CustomShipAI)
}

fun persistShipModes(shipId: String, loadoutIndex: Int, tags: List<String>) {
    Settings.shipModeStorage[loadoutIndex].modesByShip[shipId] = mutableMapOf()
    Settings.shipModeStorage[loadoutIndex].modesByShip[shipId]?.set(0, tags)
}

fun saveShipModesInShip(ship: ShipAPI, tags: List<String>, storageIndex: Int) {
    if (!ship.customData.containsKey(Values.CUSTOM_SHIP_DATA_SHIP_MODES_KEY)) {
        ship.setCustomData(Values.CUSTOM_SHIP_DATA_SHIP_MODES_KEY, InShipShipModeStorage())
    }
    (ship.customData[Values.CUSTOM_SHIP_DATA_WEAPONS_TAG_KEY] as? InShipShipModeStorage)?.modes?.set(
        storageIndex,
        tags.toMutableList()
    )
}

fun saveShipModes(ship: ShipAPI, loadoutIndex: Int, tags: List<String>) {
    if (Settings.enableCombatChangePersistence()) {
        val shipId = generateUniversalFleetMemberId(ship)
        persistShipModes(shipId, loadoutIndex, tags)
    } else {
        saveShipModesInShip(ship, tags, loadoutIndex)
    }
}

fun loadPersistedShipModes(shipId: String, loadoutIndex: Int): List<String> {
    return Settings.shipModeStorage[loadoutIndex].modesByShip[shipId]?.get(0) ?: emptyList()
}

fun addPersistentShipMode(shipId: String, loadoutIndex: Int, mode: String){
    val modes = loadPersistedShipModes(shipId, AGCGUI.storageIndex)
    val newModes = (modes + mode).toSet().toList()
    persistShipModes(shipId, loadoutIndex, newModes)
}

fun removePersistentShipMode(shipId: String, loadoutIndex: Int, mode: String){
    val modes = loadPersistedShipModes(shipId, AGCGUI.storageIndex)
    persistShipModes(shipId, loadoutIndex, modes.filter { it != mode })
}

fun loadShipModesFromShip(ship: ShipAPI, storageIndex: Int): List<String> {
    return (ship.customData[Values.CUSTOM_SHIP_DATA_SHIP_MODES_KEY] as? InShipShipModeStorage)?.modes?.get(storageIndex)
        ?: emptyList()
}

fun loadShipModes(ship: ShipAPI, loadoutIndex: Int): List<String> {
    if (Settings.enableCombatChangePersistence() || !doesShipHaveLocalShipModes(ship, loadoutIndex)) {
        val shipId = generateUniversalFleetMemberId(ship)
        return loadPersistedShipModes(shipId, loadoutIndex)
    }
    return loadShipModesFromShip(ship, loadoutIndex)
}