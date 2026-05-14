package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*

import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI

// Sync lifecycle:
// 1. observeTargetPriority records fresh per-weapon target candidates.
// 2. evaluate converges on a shared target, then waits briefly for it to settle.
// 3. openReleaseWindow snapshots the settled release target and firing solutions.
// 4. allowFire marks released weapons while the release window is open.
// 5. started weapons are protected until their burst/charge sequence is complete.
// 6. the coordinator waits for all participants to become ready before the next cycle.
internal class SyncGroupCoordinator {
    // Current weapons participating in this group/mode cycle.
    private var participantIds: Set<Int> = emptySet()
    // Weapons with the longest predicted release window; window mode uses them to keep the window open.
    private var anchorIds: Set<Int> = emptySet()
    private var releaseStartedAt = 0f
    private var releaseWindowSeconds = MIN_RELEASE_WINDOW_SECONDS
    // Target selected for the currently open release window.
    private var releaseTargetId = 0
    private var releaseTarget: CombatEntityAPI? = null
    private val releaseSolutionsByWeapon = mutableMapOf<Int, FiringSolution>()
    // Best shared target during the pre-release convergence phase.
    private val convergenceTracker = SyncTargetConvergenceTracker()
    // True after a release closes; prevents a new cycle until all participants are safely ready again.
    private var waitingForCycle = false
    private var anchorActivityObserved = false
    private var lastAnchorActivityAt = 0f
    private var cycleResetAt = -1f
    private val debugReporter = SyncDebugReporter()
    private val releasedWeapons = SyncReleasedWeaponTracker()

    // True only while the coordinator is allowing the group to fire at releaseTarget.
    var releaseOpen = false
        private set

    fun observeTargetPriority(
        weapon: WeaponAPI,
        participants: List<WeaponAPI>,
        mode: SyncFireMode,
        rangeFactor: Float,
        solution: FiringSolution,
        priority: Float,
        requiredTarget: CombatEntityAPI?
    ) {
        syncRequiredTarget(requiredTarget)
        syncParticipants(participants, mode, rangeFactor)
        convergenceTracker.observeCandidate(weapon, solution, priority)
        updateConvergenceTarget(participants, rangeFactor)
    }

    fun evaluate(
        participants: List<WeaponAPI>,
        mode: SyncFireMode,
        rangeFactor: Float,
        requiredTarget: CombatEntityAPI?,
        systemTriggers: Set<SyncSystemTriggerOption>,
    ) {
        // TagBasedAI calls this from several hooks in one frame. A broad per-frame cache is risky because
        // those hooks can observe new candidates, firing starts, and protected-sequence transitions.
        syncRequiredTarget(requiredTarget)
        syncParticipants(participants, mode, rangeFactor)
        updateReleasedStarts(participants)
        updateOpenRelease(participants, mode)

        if (releaseOpen && shouldCloseReleaseWindow(participants, mode, rangeFactor)) {
            closeRelease()
            debug(participants, mode, rangeFactor, "closed-release", true)
            return
        }

        if (waitingForCycle) {
            if (canStartNextCycle(participants, mode)) {
                clearWaiting()
                debug(participants, mode, rangeFactor, "cycle-ready", true)
            } else {
                debug(participants, mode, rangeFactor, "waiting")
            }
            return
        }

        val currentTime = now()
        if (releaseOpen || cycleResetAt == currentTime) return

        val releasePlan = selectReleaseTarget(participants, mode, rangeFactor) ?: run {
            convergenceTracker.clearPendingReleaseTarget()
            debug(participants, mode, rangeFactor, "no-release-target")
            return
        }
        if (!convergenceTracker.hasSettledReleaseTarget(releasePlan.target)) {
            debug(participants, mode, rangeFactor, "settling-release-target")
            return
        }

        openReleaseWindow(participants, mode, releasePlan, systemTriggers)
        debug(participants, mode, rangeFactor, "opened-release", true)
    }

    fun matchesReleaseTarget(target: CombatEntityAPI): Boolean {
        return releaseTargetId == 0 || releaseTargetId == target.syncId()
    }

    fun clearRequiredTarget() {
        syncRequiredTarget(null)
    }

    fun hasConvergenceTarget(): Boolean {
        return convergenceTracker.hasConvergenceTarget()
    }

    fun matchesConvergenceTarget(target: CombatEntityAPI): Boolean {
        return convergenceTracker.matchesConvergenceTarget(target)
    }

    fun releaseSolution(weapon: WeaponAPI): FiringSolution? {
        val target = releaseTarget ?: return null
        if (!releaseOpen) return null

        val weaponId = weapon.syncId()
        return freshReleaseSolution(weapon) ?: releaseSolutionsByWeapon[weaponId] ?: FiringSolution(target, target.location)
    }

    fun ambushFiringSolution(weapon: WeaponAPI, rangeFactor: Float): FiringSolution? {
        if (!releaseOpen) return null
        val solution = currentAmbushPursuitSolution(weapon) ?: return null
        if (!weapon.canEventuallyBearOnSyncTarget(solution, rangeFactor)) return null
        return solution
    }

    fun weaponCanPursueReleaseTarget(weapon: WeaponAPI, rangeFactor: Float): Boolean {
        val solution = currentAmbushPursuitSolution(weapon) ?: return false
        return weapon.canEventuallyBearOnSyncTarget(solution, rangeFactor)
    }

    fun weaponHasLostReleaseTarget(weapon: WeaponAPI, rangeFactor: Float): Boolean {
        return !weaponCanPursueReleaseTarget(weapon, rangeFactor)
    }

    private fun freshReleaseSolution(weapon: WeaponAPI): FiringSolution? {
        if (releaseTargetId == 0) return null
        val weaponId = weapon.syncId()
        val currentSolution = weapon.syncDecisionSnapshot()
            ?.solution
            ?.takeIf { it.target.syncId() == releaseTargetId }

        if (currentSolution != null) {
            releaseSolutionsByWeapon[weaponId] = currentSolution
            return currentSolution
        }

        val candidateSolution = convergenceTracker.freshCandidateSolution(
            weapon,
            releaseTargetId,
            candidateMaxAge()
        )

        if (candidateSolution != null) {
            releaseSolutionsByWeapon[weaponId] = candidateSolution
            return candidateSolution
        }

        return null
    }

    private fun currentAmbushPursuitSolution(weapon: WeaponAPI): FiringSolution? {
        val target = releaseTarget ?: return null
        if (!releaseOpen) return null
        return freshReleaseSolution(weapon) ?: FiringSolution(target, target.location)
    }

    fun isReleasedSequenceProtected(weapon: WeaponAPI, mode: SyncFireMode): Boolean {
        return releasedWeapons.isReleasedSequenceProtected(weapon, mode)
    }

    fun allowFire(
        weapon: WeaponAPI,
        participants: List<WeaponAPI>,
        mode: SyncFireMode,
        rangeFactor: Float
    ): Boolean {
        if (!releaseOpen) return false
        if (isReleasedSequenceProtected(weapon, mode)) return true
        if (!weapon.canParticipateInSync() || !weapon.hasAmmoForSync()) return false
        if (!releasedWeapons.hasStartedWeapons() && now() < releaseStartedAt) return false
        if (!releasedWeapons.hasStartedWeapons() && !canStartReleaseNow(participants, rangeFactor)) return false

        return when (mode) {
            SyncFireMode.WINDOW -> allowWindowFire(weapon)
            SyncFireMode.VOLLEY -> allowVolleyFire(weapon)
            SyncFireMode.AMBUSH -> allowAmbushFire(weapon)
        }
    }

    fun debugDecision(
        weapon: WeaponAPI,
        participants: List<WeaponAPI>,
        mode: SyncFireMode,
        rangeFactor: Float,
        reason: String
    ) {
        debugReporter.debugDecision(weapon, participants, mode, reason) {
            debugSummary(participants, rangeFactor)
        }
    }

    private fun allowWindowFire(weapon: WeaponAPI): Boolean {
        if (isAnchor(weapon) &&
            releasedWeapons.hasReleaseStarted(weapon) &&
            !weapon.isContinuousBeamForSync()
        ) {
            return false
        }
        if (!isAnchor(weapon) &&
            !releasedWeapons.hasReleaseStarted(weapon) &&
            !weapon.canStartAndFinishWithinSyncWindow(releaseStartedAt + releaseWindowSeconds)
        ) {
            return false
        }

        releasedWeapons.markReleased(weapon)
        return true
    }

    private fun allowVolleyFire(weapon: WeaponAPI): Boolean {
        if (!releasedWeapons.hasReleased(weapon)) {
            releasedWeapons.markReleased(weapon)
            return true
        }

        if (weapon.isContinuousBeamForSync() && isWithinReleaseWindow()) return true
        if (!releasedWeapons.hasReleaseStarted(weapon) && isWithinReleaseWindow()) return true

        return false
    }

    private fun allowAmbushFire(weapon: WeaponAPI): Boolean {
        releasedWeapons.markReleased(weapon)
        return true
    }

    private fun syncParticipants(participants: List<WeaponAPI>, mode: SyncFireMode, rangeFactor: Float) {
        val currentIds = participants.map { it.syncId() }.toSet()
        if (currentIds == participantIds) return

        if (releaseOpen && mode == SyncFireMode.AMBUSH) {
            participantIds = currentIds
            convergenceTracker.retainParticipants(currentIds)
            debug(participants, mode, rangeFactor, "ambush-participants-changed", true)
            return
        }

        participantIds = currentIds
        convergenceTracker.retainParticipants(currentIds)
        resetCycleState()
        cycleResetAt = now()
        debug(participants, mode, rangeFactor, "participants-changed", true)
    }

    private fun updateConvergenceTarget(participants: List<WeaponAPI>, rangeFactor: Float) {
        if (releaseOpen) return
        convergenceTracker.updateConvergenceTarget(participants, rangeFactor, candidateMaxAge())
    }

    private fun selectReleaseTarget(
        participants: List<WeaponAPI>,
        mode: SyncFireMode,
        rangeFactor: Float
    ): SyncReleasePlan? {
        if (participants.any { isReleasedSequenceProtected(it, mode) || it.isProtectedReleaseSequenceActive() }) return null
        if (participants.any { !it.isReadyForSync() }) return null

        val snapshots = collectFreshSnapshots(participants) ?: return null
        return convergenceTracker.selectReleaseTarget(snapshots, rangeFactor)
    }

    private fun canStartReleaseNow(participants: List<WeaponAPI>, rangeFactor: Float): Boolean {
        return participants.all { participant ->
            val solution = releaseSolution(participant) ?: return@all false
            participant.isReadyForSync() && participant.isOnTargetForSync(solution, rangeFactor)
        }
    }

    private fun collectFreshSnapshots(participants: List<WeaponAPI>): List<SyncParticipant>? {
        val currentTime = now()
        val maxAge = candidateMaxAge()
        val snapshots = mutableListOf<SyncParticipant>()

        for (participant in participants) {
            val snapshot = participant.syncDecisionSnapshot() ?: return null
            if (currentTime - snapshot.timestamp > maxAge) return null
            snapshots.add(SyncParticipant(participant, snapshot))
        }

        return snapshots
    }

    private fun candidateMaxAge(): Float {
        return maxOf(
            SNAPSHOT_STALE_SECONDS,
            (Global.getCombatEngine()?.elapsedInLastFrame ?: 0f) * 4f
        )
    }

    private fun openReleaseWindow(
        participants: List<WeaponAPI>,
        mode: SyncFireMode,
        releasePlan: SyncReleasePlan,
        systemTriggers: Set<SyncSystemTriggerOption>,
    ) {
        val currentTime = now()
        val maxWindow = participants.maxOfOrNull { it.expectedReleaseWindow() } ?: MIN_RELEASE_WINDOW_SECONDS
        val systemChargeDelay = triggerSyncSystemForRelease(participants, systemTriggers, releasePlan.target)

        releaseOpen = true
        waitingForCycle = false
        releaseStartedAt = currentTime + systemChargeDelay
        lastAnchorActivityAt = currentTime
        releaseTargetId = releasePlan.target.syncId()
        releaseTarget = releasePlan.target
        releaseSolutionsByWeapon.clear()
        releaseSolutionsByWeapon.putAll(releasePlan.solutionsByWeapon)
        convergenceTracker.clearPendingReleaseTarget()
        releaseWindowSeconds = if (mode == SyncFireMode.VOLLEY) VOLLEY_RELEASE_WINDOW_SECONDS else maxWindow
        anchorIds = participants
            .filter { maxWindow - it.expectedReleaseWindow() <= ANCHOR_WINDOW_TOLERANCE_SECONDS }
            .map { it.syncId() }
            .toSet()
        releasedWeapons.clear()
        anchorActivityObserved = false
    }

    private fun closeRelease() {
        releaseOpen = false
        waitingForCycle = true
    }

    private fun clearWaiting() {
        waitingForCycle = false
        releasedWeapons.clear()
        releaseTargetId = 0
        releaseTarget = null
        releaseSolutionsByWeapon.clear()
        convergenceTracker.clearConvergence()
        anchorIds = emptySet()
        anchorActivityObserved = false
        cycleResetAt = now()
    }

    private fun resetCycleState() {
        releaseOpen = false
        waitingForCycle = false
        releasedWeapons.clear()
        releaseTargetId = 0
        releaseTarget = null
        releaseSolutionsByWeapon.clear()
        convergenceTracker.clearConvergence()
        anchorIds = emptySet()
        anchorActivityObserved = false
    }

    private fun syncRequiredTarget(requiredTarget: CombatEntityAPI?) {
        if (!convergenceTracker.syncRequiredTarget(requiredTarget)) return
        resetCycleState()
        cycleResetAt = now()
    }

    private fun updateReleasedStarts(participants: List<WeaponAPI>) {
        releasedWeapons.updateReleasedStarts(participants)
    }

    private fun updateOpenRelease(participants: List<WeaponAPI>, mode: SyncFireMode) {
        if (!releaseOpen || mode != SyncFireMode.WINDOW) return

        val currentTime = now()
        val activeAnchor = participants.any { participant ->
            anchorIds.contains(participant.syncId()) && participant.isReleaseActivityActive()
        }
        if (activeAnchor) {
            anchorActivityObserved = true
            lastAnchorActivityAt = currentTime
        }
    }

    private fun shouldCloseReleaseWindow(
        participants: List<WeaponAPI>,
        mode: SyncFireMode,
        rangeFactor: Float
    ): Boolean {
        val currentTime = now()
        if (mode == SyncFireMode.VOLLEY) {
            return currentTime - releaseStartedAt >= releaseWindowSeconds
        }
        if (mode == SyncFireMode.AMBUSH) {
            return shouldEndAmbush(participants, rangeFactor)
        }

        val reachedPredictedWindow = currentTime - releaseStartedAt >= releaseWindowSeconds
        val anchorWentQuiet = anchorActivityObserved &&
                currentTime - lastAnchorActivityAt >= ANCHOR_ACTIVITY_GRACE_SECONDS
        val noAnchorObserved = !anchorActivityObserved && reachedPredictedWindow

        return reachedPredictedWindow || anchorWentQuiet || noAnchorObserved
    }

    private fun shouldEndAmbush(participants: List<WeaponAPI>, rangeFactor: Float): Boolean {
        if (releaseTarget == null) return true
        return participants.none { participant ->
            participant.canParticipateInSync() &&
                    weaponCanPursueReleaseTarget(participant, rangeFactor)
        }
    }

    private fun canStartNextCycle(participants: List<WeaponAPI>, mode: SyncFireMode): Boolean {
        return releasedWeapons.canStartNextCycle(participants, mode)
    }

    private fun debug(
        participants: List<WeaponAPI>,
        mode: SyncFireMode,
        rangeFactor: Float,
        reason: String,
        force: Boolean = false
    ) {
        debugReporter.debugEvent(participants, mode, reason, force) {
            debugSummary(participants, rangeFactor)
        }
    }

    private fun debugSummary(participants: List<WeaponAPI>, rangeFactor: Float): String {
        val target = releaseTarget
        val participantSummary = participants.joinToString(";") { participant ->
            participant.debugSyncState(
                participant.syncDecisionSnapshot(),
                releaseSolution(participant),
                rangeFactor
            )
        }
        return "open=$releaseOpen waiting=$waitingForCycle " +
                "target=${target?.debugId() ?: "none"} " +
                "converge=${convergenceTracker.debugTarget()?.debugId() ?: "none"} " +
                "convergeScore=${fmt(convergenceTracker.debugScore())} " +
                "window=${fmt(releaseWindowSeconds)} elapsed=${fmt(now() - releaseStartedAt)} " +
                "anchors=$anchorIds released=${releasedWeapons.debugReleasedIds()} " +
                "started=${releasedWeapons.debugStartedIds()} " +
                "participants=[$participantSummary]"
    }

    private fun isAnchor(weapon: WeaponAPI): Boolean {
        return anchorIds.contains(weapon.syncId())
    }

    private fun isWithinReleaseWindow(): Boolean {
        return releaseOpen && now() - releaseStartedAt <= releaseWindowSeconds
    }
}
