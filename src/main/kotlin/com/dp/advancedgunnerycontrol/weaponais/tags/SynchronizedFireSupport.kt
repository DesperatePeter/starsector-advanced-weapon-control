package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.shipdata.getAutofirePlugin
import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.dp.advancedgunnerycontrol.weaponais.TagBasedAI
import com.dp.advancedgunnerycontrol.weaponais.WeaponDecisionSnapshot
import com.dp.advancedgunnerycontrol.weaponais.angularDistanceFromWeapon
import com.dp.advancedgunnerycontrol.weaponais.determineIfShotWillHitBySetting
import com.dp.advancedgunnerycontrol.weaponais.effectiveCollRadius
import com.dp.advancedgunnerycontrol.weaponais.isAimable
import com.dp.advancedgunnerycontrol.weaponais.linearDistanceFromWeapon
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.loading.WeaponSpecAPI
import java.util.IdentityHashMap
import kotlin.math.PI

internal const val CUSTOM_DATA_KEY = "advanced_gunnery_control_synchronized_fire"
internal const val READY_EPSILON = 0.0001f
internal const val READY_TOLERANCE_SECONDS = 0.01f
internal const val MAX_READY_TOLERANCE_SECONDS = 0.02f
internal const val SNAPSHOT_STALE_SECONDS = 0.12f
internal const val MIN_RELEASE_WINDOW_SECONDS = 0.12f
internal const val VOLLEY_RELEASE_WINDOW_SECONDS = 0.50f
internal const val RELEASE_WINDOW_PADDING_SECONDS = 0.12f
internal const val RELEASE_ALIGNMENT_SETTLE_SECONDS = 0.03f
internal const val ANCHOR_ACTIVITY_GRACE_SECONDS = 0.04f
internal const val ANCHOR_WINDOW_TOLERANCE_SECONDS = 0.05f
internal const val AIM_POINT_ALIGNMENT_FLAT_DEGREES = 1.25f
internal const val AIM_POINT_ALIGNMENT_TARGET_FRACTION = 0.25f
internal const val AIM_POINT_ALIGNMENT_MAX_DEGREES = 3.00f
internal const val CONVERGENCE_TARGET_PRIORITY_MULTIPLIER = 0.001f
internal const val NON_CONVERGENCE_TARGET_PRIORITY_MULTIPLIER = 1000f
internal const val CONVERGENCE_TARGET_SWITCH_SCORE_RATIO = 0.85f
internal data class SyncParticipant(
    val weapon: WeaponAPI,
    val snapshot: WeaponDecisionSnapshot
)

internal data class SyncReleasePlan(
    val target: CombatEntityAPI,
    val solutionsByWeapon: Map<Int, FiringSolution>
)

internal data class SyncTargetCandidate(
    val solution: FiringSolution,
    val priority: Float,
    val timestamp: Float
)

private val burstDelayCacheBySpec = IdentityHashMap<WeaponSpecAPI, Float>()
private val autoChargeCacheBySpec = IdentityHashMap<WeaponSpecAPI, Boolean>()

internal fun now(): Float = Global.getCombatEngine()?.getTotalElapsedTime(false) ?: 0f

internal fun WeaponAPI.syncId(): Int = System.identityHashCode(this)

internal fun CombatEntityAPI.syncId(): Int = System.identityHashCode(this)

internal fun CombatEntityAPI.debugId(): String = "${javaClass.simpleName}@${syncId()}"

internal fun WeaponAPI.debugSyncState(
    snapshot: WeaponDecisionSnapshot?,
    releaseSolution: FiringSolution?,
    rangeFactor: Float
): String {
    val snapshotAge = snapshot?.let { now() - it.timestamp }
    val onReleaseTarget = releaseSolution?.let { isOnTargetForSync(it, rangeFactor) }
    val tagNames = ((getAutofirePlugin() as? TagBasedAI)?.tags ?: emptyList())
        .joinToString("+") { it.javaClass.simpleName }
    return "${id}@${syncId()}{" +
            "ready=${isReadyForSync()}," +
            "ammo=${if (usesAmmo()) "$ammo/$maxAmmo req=${requiredAmmoForSyncStart()}" else "na"}," +
            "win=${fmt(expectedReleaseWindow())}," +
            "cd=${fmt(cooldownRemaining)}," +
            "charge=${fmt(chargeLevel)}," +
            "firing=$isFiring," +
            "burst=$isInBurst," +
            "burstRem=${fmt(burstFireTimeRemaining)}," +
            "snap=${snapshot != null}," +
            "snapAge=${snapshotAge?.let { fmt(it) } ?: "na"}," +
            "base=${snapshot?.baseDecision ?: "na"}," +
            "snapTarget=${snapshot?.solution?.target?.debugId() ?: "none"}," +
            "onRelease=${onReleaseTarget ?: "na"}," +
            "tags=$tagNames" +
            "}"
}

internal fun WeaponAPI.canParticipateInSync(): Boolean {
    if (isDisabled || isPermanentlyDisabled || isForceDisabled || isDecorative) return false
    if (!usesAmmo() || ammo > 0) return true

    return regeneratesAmmoForSync()
}

internal fun WeaponAPI.hasAmmoForSync(): Boolean {
    return !usesAmmo() || ammo >= requiredAmmoForSyncStart()
}

internal fun WeaponAPI.regeneratesAmmoForSync(): Boolean {
    val tracker = ammoTracker ?: return false
    return tracker.ammoPerSecond > READY_EPSILON
}

internal fun WeaponAPI.requiredAmmoForSyncStart(): Int {
    if (!usesAmmo()) return 0
    if (isBeam || !regeneratesAmmoForSync()) return 1

    val burstSize = (spec?.burstSize ?: 1).coerceAtLeast(1)
    return burstSize.coerceAtMost(maxAmmo.coerceAtLeast(1))
}

internal fun WeaponAPI.isReadyForSync(): Boolean {
    val readyTolerance = maxOf(
        READY_TOLERANCE_SECONDS,
        (Global.getCombatEngine()?.elapsedInLastFrame ?: 0f) * 0.5f
    ).coerceAtMost(MAX_READY_TOLERANCE_SECONDS)
    return canParticipateInSync() &&
            hasAmmoForSync() &&
            cooldownRemaining <= readyTolerance &&
            !isFiring &&
            !isInBurst
}

internal fun WeaponAPI.isOnTargetForSync(solution: FiringSolution, rangeFactor: Float): Boolean {
    val targetRadius = effectiveCollRadius(solution.target)
    val distance = linearDistanceFromWeapon(solution.aimPoint, this)
    val triggerHappiness = Settings.customAITriggerHappiness()
    val rangeAllowance = range * rangeFactor
    if (distance - targetRadius > rangeAllowance) return false
    if (!isAimable(this)) return true

    val effectiveCollisionRadius = targetRadius * triggerHappiness + 10f * triggerHappiness
    val arcAllowance = effectiveCollisionRadius / maxOf(distance, 1f) * 180f / PI.toFloat()
    val centralAimAllowance = maxOf(
        AIM_POINT_ALIGNMENT_FLAT_DEGREES,
        AIM_POINT_ALIGNMENT_TARGET_FRACTION * arcAllowance
    ).coerceAtMost(AIM_POINT_ALIGNMENT_MAX_DEGREES)
    val centralAimDistance = angularDistanceFromWeapon(solution.aimPoint, this) * 180f / PI.toFloat()
    return distanceFromArc(solution.aimPoint) <= arcAllowance &&
            centralAimDistance <= centralAimAllowance &&
            determineIfShotWillHitBySetting(
                solution.target,
                solution.aimPoint,
                effectiveCollisionRadius,
                this
            )
}

internal fun WeaponAPI.canEventuallyBearOnSyncTarget(solution: FiringSolution, rangeFactor: Float): Boolean {
    val targetRadius = effectiveCollRadius(solution.target)
    val distance = linearDistanceFromWeapon(solution.aimPoint, this)
    val rangeAllowance = range * rangeFactor
    if (distance - targetRadius > rangeAllowance) return false
    if (!isAimable(this)) return true

    val triggerHappiness = Settings.customAITriggerHappiness()
    val effectiveCollisionRadius = targetRadius * triggerHappiness + 10f * triggerHappiness
    val arcAllowance = effectiveCollisionRadius / maxOf(distance, 1f) * 180f / PI.toFloat()
    return distanceFromArc(solution.aimPoint) <= arcAllowance
}

// Release activity and protected sequence detection
internal fun WeaponAPI.isReleaseActivityActive(): Boolean {
    return isFiring || isInBurst
}

internal fun WeaponAPI.requiresProtectedReleaseWindow(): Boolean {
    val spec = spec ?: return false
    if (isBeam) {
        return isBurstBeam ||
                spec.isBurstBeam ||
                spec.beamChargeupTime > READY_EPSILON ||
                spec.burstDuration > READY_EPSILON
    }

    return requiresFullCharge() ||
            spec.chargeTime > READY_EPSILON ||
            spec.burstSize > 1
}

internal fun WeaponAPI.isProtectedReleaseSequenceActive(): Boolean {
    return requiresProtectedReleaseWindow() && isReleaseActivityActive()
}

internal fun WeaponAPI.isContinuousBeamForSync(): Boolean {
    return isBeam && !isBurstBeam && !requiresProtectedReleaseWindow()
}

internal fun WeaponSpecAPI.burstDelayForSync(): Float {
    return burstDelayCacheBySpec.getOrPut(this) {
        runCatching {
            val method = javaClass.getMethod("getBurstDelay")
            (method.invoke(this) as? Number)?.toFloat() ?: 0f
        }.getOrDefault(0f)
    }
}

internal fun WeaponSpecAPI.isAutoChargeForSync(): Boolean {
    return autoChargeCacheBySpec.getOrPut(this) {
        runCatching {
            val method = javaClass.getMethod("isAutoCharge")
            method.invoke(this) == true
        }.getOrDefault(false)
    }
}

internal fun WeaponAPI.isAutoChargeProjectileForSync(): Boolean {
    return !isBeam && spec?.isAutoChargeForSync() == true
}

internal fun WeaponSpecAPI.projectileBurstDurationForSync(): Float {
    val projectileCount = burstSize.coerceAtLeast(1)
    return (projectileCount - 1).coerceAtLeast(0) * burstDelayForSync()
}

internal fun WeaponSpecAPI.beamActiveDurationForSync(): Float {
    if (burstDuration > READY_EPSILON) return burstDuration
    return VOLLEY_RELEASE_WINDOW_SECONDS
}

internal fun WeaponAPI.expectedReleaseWindow(): Float {
    if (isContinuousBeamForSync()) return VOLLEY_RELEASE_WINDOW_SECONDS

    val spec = spec ?: return MIN_RELEASE_WINDOW_SECONDS
    val firingSequence = if (isBeam) {
        spec.beamChargeupTime + spec.beamActiveDurationForSync()
    } else {
        spec.chargeTime + spec.projectileBurstDurationForSync()
    }

    return maxOf(MIN_RELEASE_WINDOW_SECONDS, firingSequence + RELEASE_WINDOW_PADDING_SECONDS)
}

internal fun WeaponAPI.canStartAndFinishWithinSyncWindow(releaseEndsAt: Float): Boolean {
    if (isContinuousBeamForSync()) return true
    return releaseEndsAt - now() >= expectedReleaseWindow() - ANCHOR_WINDOW_TOLERANCE_SECONDS
}

internal fun WeaponAPI.hasSyncTag(
    mode: SyncFireMode,
    requireShipTarget: Boolean,
    systemTriggers: Set<SyncSystemTriggerOption>,
): Boolean {
    return (getAutofirePlugin() as? TagBasedAI)?.tags?.any { tag ->
        tag is SynchronizedFireTag &&
            tag.mode == mode &&
            tag.requireShipTarget == requireShipTarget &&
            tag.systemTriggers == systemTriggers
    } == true
}

internal fun WeaponAPI.syncDecisionSnapshot(): WeaponDecisionSnapshot? {
    return ((getAutofirePlugin() as? TagBasedAI)?.decisionSnapshot)
        ?.takeIf { it.weapon === this }
}
