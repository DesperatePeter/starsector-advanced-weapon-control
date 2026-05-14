package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

private fun List<WeaponAPI>.matchesSyncSystemTrigger(trigger: SyncSystemTriggerOption): Boolean {
    return when (trigger) {
        SyncSystemTriggerOption.ACCELERATED_AMMO_FEEDER ->
            any { it.type == WeaponAPI.WeaponType.BALLISTIC }
        SyncSystemTriggerOption.HIGH_ENERGY_FOCUS ->
            any { it.type == WeaponAPI.WeaponType.ENERGY }
        SyncSystemTriggerOption.LIDAR_ARRAY ->
            any { it.type == WeaponAPI.WeaponType.BALLISTIC && it.size == WeaponAPI.WeaponSize.LARGE }
        SyncSystemTriggerOption.TEMPORAL_SHELL,
        SyncSystemTriggerOption.ENTROPY_AMPLIFIER -> true
    }
}

private fun ShipAPI.canTriggerSyncSystem(): Boolean {
    val system = system ?: return false
    if (isShipSystemDisabled) return false
    if (fluxTracker?.isOverloadedOrVenting == true) return false
    if (system.isActive || system.isOn || system.isChargeup || system.isChargedown) return false
    if (system.isCoolingDown || system.isOutOfAmmo) return false
    return system.canBeActivated()
}

private fun CombatEntityAPI.syncSystemShipTarget(): ShipAPI? {
    // AGC intentionally spends sync-triggered ship systems only on non-fighter ship targets.
    // Self-buff systems can help against smaller targets, but spending them on missiles/fighters
    // is too wasteful for the current sync-trigger policy.
    val targetShip = this as? ShipAPI ?: return null
    return targetShip.takeUnless { it.isFighter }
}

private fun ShipAPI.useEntropyAmplifierForSyncTarget(targetShip: ShipAPI) {
    val previousTarget = shipTarget
    try {
        shipTarget = targetShip
        useSystem()
    } finally {
        shipTarget = previousTarget
    }
}

internal fun triggerSyncSystemForRelease(
    participants: List<WeaponAPI>,
    triggers: Set<SyncSystemTriggerOption>,
    releaseTarget: CombatEntityAPI,
): Float {
    if (triggers.isEmpty()) return 0f
    val ship = participants.firstOrNull()?.ship ?: return 0f
    val system = ship.system ?: return 0f
    val trigger = syncTriggerOrder.firstOrNull { option ->
        option in triggers &&
                option.systemId == system.id &&
                participants.matchesSyncSystemTrigger(option)
    } ?: return 0f
    if (!ship.canTriggerSyncSystem()) return 0f

    val releaseTargetShip = releaseTarget.syncSystemShipTarget() ?: return 0f
    if (trigger == SyncSystemTriggerOption.ENTROPY_AMPLIFIER) {
        ship.useEntropyAmplifierForSyncTarget(releaseTargetShip)
    } else {
        ship.useSystem()
    }
    return system.chargeUpDur.coerceAtLeast(0f)
}
