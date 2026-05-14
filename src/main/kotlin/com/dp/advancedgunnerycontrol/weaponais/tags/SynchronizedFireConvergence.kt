package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.shipdata.getAutofirePlugin
import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.dp.advancedgunnerycontrol.weaponais.TagBasedAI
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI

internal class SyncTargetConvergenceTracker {
    private var requiredTargetId = 0
    private var convergenceTargetId = 0
    private var convergenceTarget: CombatEntityAPI? = null
    private var convergenceTargetScore = Float.POSITIVE_INFINITY
    private var pendingReleaseTargetId = 0
    private var pendingReleaseReadySince = 0f
    private val targetCandidatesByWeapon = mutableMapOf<Int, MutableMap<Int, SyncTargetCandidate>>()

    fun syncRequiredTarget(requiredTarget: CombatEntityAPI?): Boolean {
        val nextRequiredTargetId = requiredTarget?.syncId() ?: 0
        if (nextRequiredTargetId == requiredTargetId) return false

        requiredTargetId = nextRequiredTargetId
        targetCandidatesByWeapon.clear()
        clearConvergence()
        return true
    }

    fun observeCandidate(weapon: WeaponAPI, solution: FiringSolution, priority: Float) {
        targetCandidatesByWeapon
            .getOrPut(weapon.syncId()) { mutableMapOf() }[solution.target.syncId()] =
            SyncTargetCandidate(solution, priority, now())
    }

    fun retainParticipants(participantIds: Set<Int>) {
        targetCandidatesByWeapon.keys.retainAll(participantIds)
    }

    fun clearConvergence() {
        convergenceTargetId = 0
        convergenceTarget = null
        convergenceTargetScore = Float.POSITIVE_INFINITY
        clearPendingReleaseTarget()
    }

    fun hasConvergenceTarget(): Boolean {
        return convergenceTargetId != 0 && convergenceTarget != null
    }

    fun matchesConvergenceTarget(target: CombatEntityAPI): Boolean {
        return convergenceTargetId != 0 && convergenceTargetId == target.syncId()
    }

    fun updateConvergenceTarget(
        participants: List<WeaponAPI>,
        rangeFactor: Float,
        maxAge: Float,
    ) {
        val currentTime = now()
        val participantIds = participants.map { it.syncId() }.toSet()
        retainParticipants(participantIds)
        targetCandidatesByWeapon.values.forEach { candidates ->
            candidates.entries.removeIf { currentTime - it.value.timestamp > maxAge }
        }

        val requiredTargetFilterId = requiredTargetId.takeIf { it != 0 }
        val seenTargetIds = mutableSetOf<Int>()
        var best: ScoredConvergenceTarget? = null
        var current: ScoredConvergenceTarget? = null

        for (candidates in targetCandidatesByWeapon.values) {
            for (targetId in candidates.keys) {
                if (!seenTargetIds.add(targetId)) continue
                if (requiredTargetFilterId != null && targetId != requiredTargetFilterId) continue

                val scored = scoreCommonTarget(targetId, participants, rangeFactor) ?: continue
                if (scored.targetId == convergenceTargetId) {
                    current = scored
                }
                if (best == null || scored.score < best!!.score) {
                    best = scored
                }
            }
        }

        if (best == null) {
            clearConvergence()
            return
        }

        val shouldSwitch = current == null ||
                best.targetId == convergenceTargetId ||
                best.score < current.score * CONVERGENCE_TARGET_SWITCH_SCORE_RATIO

        val chosen = if (shouldSwitch) best else current!!
        if (convergenceTargetId != chosen.targetId) clearPendingReleaseTarget()
        convergenceTargetId = chosen.targetId
        convergenceTarget = chosen.target
        convergenceTargetScore = chosen.score
    }

    fun selectReleaseTarget(
        snapshots: List<SyncParticipant>,
        rangeFactor: Float,
    ): SyncReleasePlan? {
        return SyncReleaseTargetSelector(
            targetCandidatesByWeapon = targetCandidatesByWeapon,
            convergenceTargetId = convergenceTargetId,
            convergenceTarget = convergenceTarget,
            requiredTargetId = requiredTargetId,
        ).select(snapshots, rangeFactor)
    }

    fun freshCandidateSolution(
        weapon: WeaponAPI,
        releaseTargetId: Int,
        maxAge: Float,
    ): FiringSolution? {
        val currentTime = now()
        return targetCandidatesByWeapon[weapon.syncId()]
            ?.get(releaseTargetId)
            ?.takeIf { currentTime - it.timestamp <= maxAge }
            ?.solution
    }

    fun hasSettledReleaseTarget(target: CombatEntityAPI): Boolean {
        val currentTime = now()
        val targetId = target.syncId()
        if (pendingReleaseTargetId != targetId) {
            pendingReleaseTargetId = targetId
            pendingReleaseReadySince = currentTime
            return false
        }

        return currentTime - pendingReleaseReadySince >= RELEASE_ALIGNMENT_SETTLE_SECONDS
    }

    fun clearPendingReleaseTarget() {
        pendingReleaseTargetId = 0
        pendingReleaseReadySince = 0f
    }

    fun debugTarget(): CombatEntityAPI? = convergenceTarget

    fun debugScore(): Float = convergenceTargetScore

    private fun scoreCommonTarget(
        targetId: Int,
        participants: List<WeaponAPI>,
        rangeFactor: Float,
    ): ScoredConvergenceTarget? {
        var target: CombatEntityAPI? = null
        var score = 0f

        participants.forEach { participant ->
            val candidate = targetCandidatesByWeapon[participant.syncId()]?.get(targetId)
                ?: return null
            if (!participant.canEventuallyBearOnSyncTarget(candidate.solution, rangeFactor)) return null
            if (!participant.isValidSynchronizedTarget(candidate.solution)) return null

            target = candidate.solution.target
            score += candidate.priority
        }

        return ScoredConvergenceTarget(targetId, target ?: return null, score)
    }

    private fun WeaponAPI.isValidSynchronizedTarget(solution: FiringSolution): Boolean {
        return ((getAutofirePlugin() as? TagBasedAI)?.tags ?: emptyList())
            .filterNot { it is SynchronizedFireTag }
            .all { it.isValidSynchronizedTarget(solution) }
    }

    private data class ScoredConvergenceTarget(
        val targetId: Int,
        val target: CombatEntityAPI,
        val score: Float,
    )
}
