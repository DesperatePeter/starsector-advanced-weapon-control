package com.dp.advancedgunnerycontrol.shipmodes

import com.dp.advancedgunnerycontrol.config.Values
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.shipais.AutofireShipAI
import com.dp.advancedgunnerycontrol.weaponais.shipais.ChargeShipAI
import com.dp.advancedgunnerycontrol.weaponais.shipais.CustomShipAI
import com.dp.advancedgunnerycontrol.weaponais.shipais.MultiThresholdRetreatShipAI
import com.dp.advancedgunnerycontrol.weaponais.shipais.NeverVentAI
import com.dp.advancedgunnerycontrol.weaponais.shipais.NoSystemAI
import com.dp.advancedgunnerycontrol.weaponais.shipais.PersonalityOverrideShipAI
import com.dp.advancedgunnerycontrol.weaponais.shipais.PersonalityOverrideShipModeRuntime
import com.dp.advancedgunnerycontrol.weaponais.shipais.PreAimShipAI
import com.dp.advancedgunnerycontrol.weaponais.shipais.RetreatShipAI
import com.dp.advancedgunnerycontrol.weaponais.shipais.RetreatThresholdMetric
import com.dp.advancedgunnerycontrol.weaponais.shipais.ShieldOffShipAI
import com.dp.advancedgunnerycontrol.weaponais.shipais.ShieldUpAI
import com.dp.advancedgunnerycontrol.weaponais.shipais.ShipCommandGenerator
import com.dp.advancedgunnerycontrol.weaponais.shipais.SpamSystemAI
import com.dp.advancedgunnerycontrol.weaponais.shipais.StayAwayAI
import com.dp.advancedgunnerycontrol.weaponais.shipais.StayFarAI
import com.dp.advancedgunnerycontrol.weaponais.shipais.VentShipAI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.combat.ai.BasicShipAI
import java.lang.ref.WeakReference

private fun generateCommander(parsedMode: ParsedShipMode, ship: ShipAPI): ShipCommandGenerator {
    return when (parsedMode.mode) {
        ShipModes.FORCE_AUTOFIRE -> AutofireShipAI(ship)
        ShipModes.PERSONALITY_OVERRIDE -> PersonalityOverrideShipAI(ship, parsedMode.personality ?: "Steady")
        ShipModes.PRE_AIM -> PreAimShipAI(ship)
        ShipModes.SHIELDS_OFF -> ShieldOffShipAI(ship, parsedMode.fluxThreshold ?: Settings.shieldOffThreshold())
        ShipModes.VENT -> VentShipAI(
            ship,
            parsedMode.fluxThreshold ?: Settings.ventFluxThreshold(),
            parsedMode.ventSafetyFactor ?: Settings.ventSafetyFactor(),
            parsedMode.aggressiveVent,
        )
        ShipModes.VENT_AGGRESSIVE -> VentShipAI(ship, parsedMode.fluxThreshold ?: Settings.aggressiveVentFluxThreshold(), Settings.aggressiveVentSafetyFactor(), true)
        ShipModes.RETREAT -> RetreatShipAI(ship, parsedMode.hullThreshold ?: Settings.retreatHullThreshold())
        ShipModes.CR_RETREAT -> MultiThresholdRetreatShipAI(ship, parsedMode.crThresholds.filterNotNull(), RetreatThresholdMetric.CR, parsedMode.directRetreat)
        ShipModes.HULL_RETREAT -> MultiThresholdRetreatShipAI(ship, parsedMode.hullThresholds.filterNotNull(), RetreatThresholdMetric.HULL, parsedMode.directRetreat)
        ShipModes.NO_SYSTEM -> NoSystemAI(ship)
        ShipModes.SHIELDS_UP -> ShieldUpAI(ship, parsedMode.fluxThreshold ?: 0.9f)
        ShipModes.SHIELDS_UP_PLUS -> ShieldUpAI(ship, parsedMode.fluxThreshold ?: 1.1f)
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
    // ship.resetDefaultAI()
    if (ship.customData.containsKey(Values.CUSTOM_SHIP_DATA_SHIP_AI_KEY)) {
        ship.customData.remove(Values.CUSTOM_SHIP_DATA_SHIP_AI_KEY)
    }
    val parsedShipModes = canonicalizeShipModeNames(modes).mapNotNull(::parseShipMode)
    PersonalityOverrideShipModeRuntime.syncForShipModeAssignment(ship, parsedShipModes.any { it.mode == ShipModes.PERSONALITY_OVERRIDE })
    if (!forceAssign && (parsedShipModes.any { it.mode == ShipModes.DEFAULT } || parsedShipModes.isEmpty())) return

    val baseAI = ship.shipAI ?: return

    val commanders = parsedShipModes.map { generateCommander(it, ship) }
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
