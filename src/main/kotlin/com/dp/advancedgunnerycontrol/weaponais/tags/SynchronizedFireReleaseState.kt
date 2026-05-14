package com.dp.advancedgunnerycontrol.weaponais.tags

import com.fs.starfarer.api.combat.WeaponAPI

internal class SyncReleasedWeaponTracker {
    private val releasedWeaponIds = mutableSetOf<Int>()
    private val releasedWeaponStartedIds = mutableSetOf<Int>()
    private val releasedWeaponProtectionEnds = mutableMapOf<Int, Float>()

    fun clear() {
        releasedWeaponIds.clear()
        releasedWeaponStartedIds.clear()
        releasedWeaponProtectionEnds.clear()
    }

    fun hasStartedWeapons(): Boolean {
        return releasedWeaponStartedIds.isNotEmpty()
    }

    fun isReleasedSequenceProtected(weapon: WeaponAPI, mode: SyncFireMode): Boolean {
        val weaponId = weapon.syncId()
        if (!releasedWeaponIds.contains(weaponId)) return false
        if (mode == SyncFireMode.VOLLEY &&
            weapon.isAutoChargeProjectileForSync() &&
            releasedWeaponStartedIds.contains(weaponId)
        ) {
            return false
        }
        return weapon.isProtectedReleaseSequenceActive() &&
                releasedWeaponStartedIds.contains(weaponId) &&
                now() <= (releasedWeaponProtectionEnds[weaponId] ?: 0f)
    }

    fun hasReleased(weapon: WeaponAPI): Boolean {
        return releasedWeaponIds.contains(weapon.syncId())
    }

    fun hasReleaseStarted(weapon: WeaponAPI): Boolean {
        return releasedWeaponStartedIds.contains(weapon.syncId())
    }

    fun markReleased(weapon: WeaponAPI) {
        val weaponId = weapon.syncId()
        releasedWeaponIds.add(weaponId)
        releasedWeaponProtectionEnds[weaponId] = now() + weapon.expectedReleaseWindow()
    }

    fun updateReleasedStarts(participants: List<WeaponAPI>) {
        participants.forEach { participant ->
            val participantId = participant.syncId()
            if (releasedWeaponIds.contains(participantId) &&
                (!participant.isReadyForSync() || participant.isReleaseActivityActive())
            ) {
                if (releasedWeaponStartedIds.add(participantId)) {
                    releasedWeaponProtectionEnds[participantId] = now() + participant.expectedReleaseWindow()
                }
            }
        }
    }

    fun canStartNextCycle(participants: List<WeaponAPI>, mode: SyncFireMode): Boolean {
        return participants.none { isReleasedSequenceProtected(it, mode) || it.isProtectedReleaseSequenceActive() } &&
                participants.all { it.isReadyForSync() }
    }

    fun debugReleasedIds(): Set<Int> = releasedWeaponIds

    fun debugStartedIds(): Set<Int> = releasedWeaponStartedIds
}
