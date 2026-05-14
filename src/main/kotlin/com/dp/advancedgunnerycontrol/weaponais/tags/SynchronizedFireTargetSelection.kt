package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.fs.starfarer.api.combat.CombatEntityAPI

internal class SyncReleaseTargetSelector(
    private val targetCandidatesByWeapon: Map<Int, Map<Int, SyncTargetCandidate>>,
    private val convergenceTargetId: Int,
    private val convergenceTarget: CombatEntityAPI?,
    private val requiredTargetId: Int,
) {
    fun select(
        snapshots: List<SyncParticipant>,
        rangeFactor: Float,
    ): SyncReleasePlan? {
        val candidates = releaseTargetCandidates(snapshots)
        val triedCandidateIndexes = BooleanArray(candidates.size)
        var attempts = 0
        while (attempts < candidates.size) {
            attempts++
            val bestIndex = nextReleaseCandidateIndex(candidates, snapshots, triedCandidateIndexes)
            if (bestIndex < 0) return null

            triedCandidateIndexes[bestIndex] = true
            val candidate = candidates[bestIndex]
            val releaseSolutions = releaseSolutionsForCandidate(candidate, snapshots) ?: continue
            if (allParticipantsOnPredictedAimPoints(snapshots, releaseSolutions, rangeFactor)) {
                return SyncReleasePlan(candidate, releaseSolutions)
            }
        }
        return null
    }

    private fun releaseTargetCandidates(snapshots: List<SyncParticipant>): List<CombatEntityAPI> {
        val candidates = mutableListOf<CombatEntityAPI>()
        val candidateIds = mutableSetOf<Int>()
        val requiredTargetFilterId = requiredTargetId.takeIf { it != 0 }
        val currentConvergenceTarget = convergenceTarget
        if (currentConvergenceTarget != null && candidateIds.add(currentConvergenceTarget.syncId())) {
            candidates += currentConvergenceTarget
        }

        for (snapshot in snapshots) {
            val candidate = snapshot.snapshot.solution?.target ?: continue
            if (!requiredTargetMatchesFilter(candidate, requiredTargetFilterId)) continue
            if (candidateIds.add(candidate.syncId())) {
                candidates += candidate
            }
        }
        return candidates
    }

    private fun requiredTargetMatchesFilter(
        candidate: CombatEntityAPI,
        requiredTargetFilterId: Int?,
    ): Boolean {
        return requiredTargetFilterId == null || candidate.syncId() == requiredTargetFilterId
    }

    private fun nextReleaseCandidateIndex(
        candidates: List<CombatEntityAPI>,
        snapshots: List<SyncParticipant>,
        triedCandidateIndexes: BooleanArray,
    ): Int {
        var bestIndex = -1
        var bestScore = Int.MIN_VALUE
        for (index in candidates.indices) {
            if (triedCandidateIndexes[index]) continue
            val score = releaseCandidatePreferenceScore(candidates[index], snapshots)
            if (bestIndex < 0 || score > bestScore) {
                bestIndex = index
                bestScore = score
            }
        }
        return bestIndex
    }

    private fun releaseCandidatePreferenceScore(
        candidate: CombatEntityAPI,
        snapshots: List<SyncParticipant>,
    ): Int {
        val candidateId = candidate.syncId()
        if (candidateId == convergenceTargetId) return Int.MAX_VALUE

        var matchingSnapshots = 0
        for (snapshot in snapshots) {
            if (snapshot.snapshot.solution?.target?.syncId() == candidateId) matchingSnapshots++
        }
        return matchingSnapshots
    }

    private fun releaseSolutionsForCandidate(
        candidate: CombatEntityAPI,
        snapshots: List<SyncParticipant>,
    ): Map<Int, FiringSolution>? {
        val candidateId = candidate.syncId()
        val releaseSolutions = mutableMapOf<Int, FiringSolution>()
        for (participant in snapshots) {
            val weaponId = participant.weapon.syncId()
            val solution = participant.snapshot.solution
                ?.takeIf { it.target.syncId() == candidateId }
                ?: targetCandidatesByWeapon[weaponId]
                    ?.get(candidateId)
                    ?.solution
                ?: return null
            releaseSolutions[weaponId] = solution
        }
        return releaseSolutions
    }

    private fun allParticipantsOnPredictedAimPoints(
        snapshots: List<SyncParticipant>,
        releaseSolutions: Map<Int, FiringSolution>,
        rangeFactor: Float,
    ): Boolean {
        for (participant in snapshots) {
            val solution = releaseSolutions[participant.weapon.syncId()] ?: return false
            if (!participant.weapon.isOnTargetForSync(solution, rangeFactor)) return false
        }
        return true
    }
}
