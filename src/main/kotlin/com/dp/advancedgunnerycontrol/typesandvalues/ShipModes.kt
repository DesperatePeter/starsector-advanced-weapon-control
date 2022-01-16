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
    ShipModes.SHIELDS_OFF to "Force turn off the shield when ship flux exceeds 50%. Make sure you have enough armor/PD to pull this off.",
    ShipModes.VENT to "Vent when ship flux exceeds 75%. The ship will try to evaluate the situation and only vent if it believes" +
            " that it will survive doing so. The ship will feel safer if it has high armor/hull and enemies lack high DPS HE weapons" +
            " and finisher missiles and/or if there are many allies nearby. This mode works best on big, heavily armored, ships.",
    ShipModes.VENT_AGGRESSIVE to "Similar to ${shipModeToString[ShipModes.VENT]}, but at 50% flux and with much less concern for" +
            " the ship's survival. It will also prevent the AI from backing off while venting. Use with caution!",
    ShipModes.RETREAT to "Order a retreat command to the ship if hull < 50%. This WILL use a CP.",
    ShipModes.HELP to "---Ship AI Modes---\nThese will modify the behavior of the ship AI. They will behave like the normal ship AI, except" +
            " for the stated modifications. Note that, unless you use the ForceAutofire ship mode, AI-controlled ships won't" +
            " follow the configured modes all the time, as the ship AI will manually control/fire weapon groups." +
            "\nAs the name implies, Ship AI modes will only work for AI-controlled ships, not the player controlled ship." +
            "\nIf you set the player-controlled ship to autopilot and want to use the configured ship mode, you have to" +
            " manually load it (${Settings.infoHotkey()}-Key), as the player-controlled ship doesn't use an AI by default." +
            "\n\n---Fire Modes---\nThese are the core of this mod. They will modify what the weapon group targets and whether" +
            " it will fire or not. Given default settings, most modes will first try to find a firing solution using the" +
            " base weapon AI. Only when the base AI selects a target that doesn't match the fire mode, the custom AI will" +
            " kick in and try to find a suitable target." +
            "\nSome modes (e.g. PD-Mode) simply won't fire rather than trying to" +
            " use the custom AI (as the base AI already prioritizes missiles/fighters for PD weapons)." +
            "\nSome modes (e.g. Opportunist) skip the base AI entirely." +
            "\n\n---Suffixes---\nSuffixes allow you to further customize the behavior of fire modes. Suffixes can modify the" +
            " targeting priority (when using custom AI) and the decision whether to fire or not (both custom and base AI)." +
            "\n\n---Loadouts---\nIf you want to be able to adapt your strategy based on the situation you face, you can define" +
            " multiple loadouts for your ships. You can define different modes for your ships per loadout and cycle through them" +
            " during combat." +
            "\nAs you can only cycle loadouts for all ships, make sure your loadouts fit a theme and are consistent between ships." +
            "\nCustomize the number of available loadouts and their names in Settings.editme" +
            "\n\n---Hotkeys (in combat)---" +
            "\nNote: All modifications to modes made during combat WILL be saved (by default)." +
            "\n[NUMPAD1-7]: Cycle fire mode for corresponding weapon group (make sure Numlock is enabled)." +
            "\nTarget an ally (R-Key) to modify their modes instead." +
            "\n[${Settings.suffixHotkey()}]: Cycle fire mode suffix (group# = last pressed NUMPAD#)." +
            "\n[${Settings.infoHotkey()}]: Manually load/save modes and display info about current ship modes." +
            "\n[${Settings.loadHotkey()}]: Manually load modes for all deployed ships." +
            "\n[${Settings.resetHotkey()}]: Reset all modes back to default for current ship and loadout." +
            "\n[${Settings.cycleLoadout()}]: Cycle loadouts for all ships (on combat start, loadout 1 is loaded)." +
            "\n\n---Tips---\n - Often times, default mode with no suffix is the best option." +
            "\n - Observe how the AI behaves in combat and adjust modes based on that." +
            "\n - Consider leaving one loadout blank (all default) to give you a fallback option."
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