package com.dp.advancedgunnerycontrol.typesandvalues

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.shipais.AutofireShipAI
import com.dp.advancedgunnerycontrol.weaponais.shipais.RetreatShipAI
import com.dp.advancedgunnerycontrol.weaponais.shipais.ShieldsOffShipAI
import com.dp.advancedgunnerycontrol.weaponais.shipais.VentShipAI
import com.fs.starfarer.api.combat.ShipAPI

enum class ShipModes {
    DEFAULT, FORCE_AUTOFIRE, SHIELDS_OFF, VENT, RETREAT, HELP
}

const val defaultShipMode = "DEFAULT"

 val shipModeFromString = mapOf(
    "DEFAULT" to ShipModes.DEFAULT,
    "ForceAutofire" to ShipModes.FORCE_AUTOFIRE,
    "ShieldsOff (Flux>50%)" to ShipModes.SHIELDS_OFF,
    "Vent (Flux>50%)" to ShipModes.VENT,
    "Retreat (Hull<50%)" to ShipModes.RETREAT,
     "?" to ShipModes.HELP
)

val detailedShipModeDescriptions = mapOf(
    ShipModes.DEFAULT to "Base ship AI. Recommended most of the time.",
    ShipModes.FORCE_AUTOFIRE to "Forces autofire for all weapon groups. Use this to make ships obey all modes literally (99% of the time)." +
            " Use with caution and make sure to combine with HoldFire-suffixes to prevent the ship from fluxing out.",
    ShipModes.SHIELDS_OFF to "Force turn off the shield when ship flux exceeds 50%. Make sure you have enough armor/PD to pull this off.",
    ShipModes.VENT to "Force vent when ship flux exceeds 50%. Use with caution (harpoons will wreck you)! Not recommended on primary loadout.",
    ShipModes.RETREAT to "Order a retreat command to the ship if hull < 50%. This WILL use a CP.",
    ShipModes.HELP to "---Ship AI Modes---\nThese will modify the behavior of the ship AI. They will behave like the normal ship AI, except" +
            " for the stated modifications. Note that, unless you use the ForceAutofire ship mode, AI-controlled ships won't" +
            " follow the configured modes all the time, as the ship AI will manually control/fire weapon groups." +
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
            "\n\n---Tips---\n - Often times, default mode is the best option." +
            "\n - Observe how the AI behaves in combat and adjust modes based on that." +
            "\n - Consider leaving one loadout blank (all default) to give you a fallback option."
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