package com.dp.advancedgunnerycontrol.weaponais.tags

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.WeaponAPI
import java.util.Locale

internal const val DEBUG_SYNC = false
internal const val DEBUG_SYNC_INTERVAL_SECONDS = 0.25f

internal fun fmt(value: Float): String = String.format(Locale.US, "%.2f", value)

internal fun WeaponAPI.isInterestingSyncDebugWeapon(): Boolean {
    return id == "pulselaser" || id == "tachyonlance"
}

internal class SyncDebugReporter {
    private var nextDebugAt = 0f
    private val nextDecisionDebugByWeapon = mutableMapOf<Int, Float>()

    fun debugDecision(
        weapon: WeaponAPI,
        participants: List<WeaponAPI>,
        mode: SyncFireMode,
        reason: String,
        summary: () -> String,
    ) {
        if (!shouldDebug(participants)) return
        val currentTime = now()
        val weaponId = weapon.syncId()
        val nextAllowedAt = nextDecisionDebugByWeapon[weaponId] ?: 0f
        if (currentTime < nextAllowedAt) return

        nextDecisionDebugByWeapon[weaponId] = currentTime + DEBUG_SYNC_INTERVAL_SECONDS
        Global.getLogger(SynchronizedFireTag::class.java).info(
            "[AGC_SYNC_DEBUG] t=${fmt(currentTime)} mode=$mode weapon=${weapon.id} decision=$reason " +
                    summary()
        )
    }

    fun debugEvent(
        participants: List<WeaponAPI>,
        mode: SyncFireMode,
        reason: String,
        force: Boolean = false,
        summary: () -> String,
    ) {
        if (!shouldDebug(participants)) return
        val currentTime = now()
        if (!force && currentTime < nextDebugAt) return

        nextDebugAt = currentTime + DEBUG_SYNC_INTERVAL_SECONDS
        Global.getLogger(SynchronizedFireTag::class.java).info(
            "[AGC_SYNC_DEBUG] t=${fmt(currentTime)} mode=$mode event=$reason " +
                    summary()
        )
    }

    private fun shouldDebug(participants: List<WeaponAPI>): Boolean {
        return DEBUG_SYNC && participants.any { it.isInterestingSyncDebugWeapon() }
    }
}
