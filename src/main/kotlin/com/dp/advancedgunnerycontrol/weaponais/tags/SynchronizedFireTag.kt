package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*

import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.dp.advancedgunnerycontrol.weaponais.determineUniversalShipTarget
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI

/**
 * Coordinates SyncWindow, SyncVolley, and Ambush releases for weapons in the
 * same group.
 *
 * High-level flow:
 * 1. Gather sync participants from current TagBasedAI weapon snapshots.
 * 2. Select or preserve a converged release target.
 * 3. Wait until participants are ready, in range, and aligned.
 * 4. Open a release window; protect already-started charge/burst/beam sequences.
 * 5. Optionally trigger the matching ship system before release.
 *
 * Do not broadly memoize evaluate(): target candidates and protected release
 * state can legitimately differ between TagBasedAI hooks in the same frame.
 */
enum class SyncFireMode {
    WINDOW,
    VOLLEY,
    AMBUSH
}

class SynchronizedFireTag(
    weapon: WeaponAPI,
    val mode: SyncFireMode = SyncFireMode.WINDOW,
    private val rangeFactor: Float = 1f,
    internal val requireShipTarget: Boolean = false,
    internal val systemTriggers: Set<SyncSystemTriggerOption> = emptySet(),
) : WeaponAITagBase(weapon) {
    override fun isValidTarget(entity: CombatEntityAPI): Boolean {
        return requiredTargetMatchesOrInactive(entity) && super.isValidTarget(entity)
    }

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean {
        if (requireShipTarget && selectedRequiredTarget() == null) return true
        if (!requiredTargetMatchesOrInactive(entity)) return false
        val coordinator = coordinator()
        if (mode == SyncFireMode.AMBUSH) {
            if (!coordinator.releaseOpen) return false
            return coordinator.matchesReleaseTarget(entity) ||
                    coordinator.weaponHasLostReleaseTarget(weapon, rangeFactor)
        }
        return coordinator.releaseOpen && coordinator.matchesReleaseTarget(entity)
    }

    override fun overrideBaseFireDecision(solution: FiringSolution?, baseDecision: Boolean): Boolean {
        solution ?: return false
        val requiredTarget = selectedRequiredTarget()
        if (requireShipTarget && requiredTarget == null) return false
        if (requiredTarget != null && solution.target !== requiredTarget) return false
        val participants = synchronizedWeapons()
        if (participants.isEmpty()) return false

        val coordinator = coordinator()
        coordinator.evaluate(participants, mode, rangeFactor, requiredTarget, systemTriggers)

        if (coordinator.isReleasedSequenceProtected(weapon, mode)) return true
        if (mode == SyncFireMode.AMBUSH &&
            coordinator.releaseOpen &&
            !coordinator.matchesReleaseTarget(solution.target)
        ) {
            return weapon.canParticipateInSync() &&
                    weapon.hasAmmoForSync() &&
                    coordinator.weaponHasLostReleaseTarget(weapon, rangeFactor)
        }
        if (mode == SyncFireMode.AMBUSH &&
            coordinator.releaseOpen &&
            coordinator.matchesReleaseTarget(solution.target) &&
            !weapon.isOnTargetForSync(solution, rangeFactor)
        ) {
            return false
        }
        return coordinator.releaseOpen &&
                coordinator.matchesReleaseTarget(solution.target) &&
                weapon.canParticipateInSync() &&
                weapon.hasAmmoForSync()
    }

    override fun observeFiringDecision(solution: FiringSolution, baseDecision: Boolean) {
        val requiredTarget = selectedRequiredTarget()
        if (requireShipTarget && requiredTarget == null) return
        if (requiredTarget != null && solution.target !== requiredTarget) return
        val participants = synchronizedWeapons()
        if (participants.isEmpty()) return

        coordinator().evaluate(participants, mode, rangeFactor, requiredTarget, systemTriggers)
    }

    override fun overrideFiringSolution(): FiringSolution? {
        val requiredTarget = selectedRequiredTarget()
        if (requireShipTarget && requiredTarget == null) return null
        val participants = synchronizedWeapons()
        if (participants.isEmpty()) return null

        val coordinator = coordinator()
        coordinator.evaluate(participants, mode, rangeFactor, requiredTarget, systemTriggers)
        if (mode == SyncFireMode.AMBUSH) {
            return coordinator.ambushFiringSolution(weapon, rangeFactor)
        }
        return coordinator.releaseSolution(weapon)
    }

    override fun shouldFire(solution: FiringSolution): Boolean {
        val requiredTarget = selectedRequiredTarget()
        if (requireShipTarget && requiredTarget == null) return true
        if (requiredTarget != null && solution.target !== requiredTarget) return false
        val participants = synchronizedWeapons()
        if (participants.isEmpty()) return true

        val coordinator = coordinator()
        coordinator.evaluate(participants, mode, rangeFactor, requiredTarget, systemTriggers)

        if (coordinator.isReleasedSequenceProtected(weapon, mode)) return true

        if (mode == SyncFireMode.AMBUSH &&
            coordinator.releaseOpen &&
            !coordinator.matchesReleaseTarget(solution.target)
        ) {
            val shouldAllowOtherTarget = weapon.canParticipateInSync() &&
                    weapon.hasAmmoForSync() &&
                    coordinator.weaponHasLostReleaseTarget(weapon, rangeFactor)
            coordinator.debugDecision(
                weapon,
                participants,
                mode,
                rangeFactor,
                if (shouldAllowOtherTarget) "allowed-ambush-lost-target" else "blocked-ambush-focus"
            )
            return shouldAllowOtherTarget
        }
        if (mode == SyncFireMode.AMBUSH &&
            coordinator.releaseOpen &&
            coordinator.matchesReleaseTarget(solution.target) &&
            coordinator.weaponHasLostReleaseTarget(weapon, rangeFactor)
        ) {
            coordinator.debugDecision(weapon, participants, mode, rangeFactor, "blocked-ambush-target-lost")
            return false
        }

        if (!coordinator.releaseOpen ||
            !coordinator.matchesReleaseTarget(solution.target) ||
            !weapon.canParticipateInSync() ||
            !weapon.hasAmmoForSync()
        ) {
            coordinator.debugDecision(weapon, participants, mode, rangeFactor, "blocked-precheck")
            return false
        }

        if (!coordinator.allowFire(weapon, participants, mode, rangeFactor)) {
            coordinator.debugDecision(weapon, participants, mode, rangeFactor, "blocked-allowFire")
            return false
        }

        coordinator.debugDecision(weapon, participants, mode, rangeFactor, "allowed")
        return true
    }

    override fun isSynchronizedReleaseActive(solution: FiringSolution): Boolean {
        val requiredTarget = selectedRequiredTarget()
        if (requireShipTarget && requiredTarget == null) return false
        if (requiredTarget != null && solution.target !== requiredTarget) return false
        val participants = synchronizedWeapons()
        if (participants.isEmpty()) return false

        val coordinator = coordinator()
        coordinator.evaluate(participants, mode, rangeFactor, requiredTarget, systemTriggers)
        return coordinator.isReleasedSequenceProtected(weapon, mode) ||
                (coordinator.releaseOpen && coordinator.matchesReleaseTarget(solution.target))
    }

    override fun onFireAllowed(solution: FiringSolution) {
        val coordinator = coordinator()
        if ((coordinator.isReleasedSequenceProtected(weapon, mode) || coordinator.releaseOpen) &&
            coordinator.matchesReleaseTarget(solution.target)
        ) {
            weapon.setForceFireOneFrame(true)
        }
    }

    override fun advance() {
        val requiredTarget = selectedRequiredTarget()
        if (requireShipTarget && requiredTarget == null) return
        val participants = synchronizedWeapons()
        if (participants.isEmpty()) return

        val coordinator = coordinator()
        coordinator.evaluate(participants, mode, rangeFactor, requiredTarget, systemTriggers)
    }

    override fun computeTargetPriorityModifierForGroupTargetChoice(solution: FiringSolution): Float = 1f

    override fun observeTargetPriority(solution: FiringSolution, priority: Float) {
        val requiredTarget = selectedRequiredTarget()
        if (requireShipTarget && requiredTarget == null) return
        if (requiredTarget != null && solution.target !== requiredTarget) return
        val participants = synchronizedWeapons()
        if (participants.isEmpty()) return

        coordinator().observeTargetPriority(weapon, participants, mode, rangeFactor, solution, priority, requiredTarget)
    }

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        val requiredTarget = requiredShipTargetOrNull()
        if (requireShipTarget) {
            if (requiredTarget == null) return 1f
            if (solution.target !== requiredTarget) return NON_CONVERGENCE_TARGET_PRIORITY_MULTIPLIER
        }
        val coordinator = coordinator()
        if (coordinator.releaseOpen) {
            if (mode == SyncFireMode.AMBUSH) {
                return when {
                    coordinator.matchesReleaseTarget(solution.target) &&
                            coordinator.weaponCanPursueReleaseTarget(weapon, rangeFactor) -> 0.01f
                    coordinator.matchesReleaseTarget(solution.target) -> 100f
                    coordinator.weaponCanPursueReleaseTarget(weapon, rangeFactor) -> 100f
                    else -> 1f
                }
            }
            return if (coordinator.matchesReleaseTarget(solution.target)) 0.01f else 100f
        }
        return if (coordinator.hasConvergenceTarget()) {
            if (coordinator.matchesConvergenceTarget(solution.target)) {
                CONVERGENCE_TARGET_PRIORITY_MULTIPLIER
            } else {
                NON_CONVERGENCE_TARGET_PRIORITY_MULTIPLIER
            }
        } else {
            1f
        }
    }

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false

    private fun requiredShipTargetOrNull(): CombatEntityAPI? {
        return weapon.ship?.determineUniversalShipTarget()
    }

    private fun selectedRequiredTarget(): CombatEntityAPI? {
        if (!requireShipTarget) return null
        return requiredShipTargetOrNull() ?: run {
            coordinator().clearRequiredTarget()
            null
        }
    }

    private fun requiredTargetMatchesOrInactive(entity: CombatEntityAPI): Boolean {
        val requiredTarget = selectedRequiredTarget() ?: return true
        return entity === requiredTarget
    }

    private fun synchronizedWeapons(): List<WeaponAPI> {
        val group = weapon.ship?.getWeaponGroupFor(weapon)
            ?: return listOf(weapon).filter {
                it.canParticipateInSync() && it.hasSyncTag(mode, requireShipTarget, systemTriggers)
            }

        return group.weaponsCopy.filter { participant ->
            participant.canParticipateInSync() && participant.hasSyncTag(mode, requireShipTarget, systemTriggers)
        }
    }

    private fun coordinator(): SyncGroupCoordinator {
        val ship = weapon.ship
        val groupIndex = ship?.weaponGroupsCopy?.indexOf(ship.getWeaponGroupFor(weapon)) ?: -1
        val triggerKey = syncTriggerOrder
            .filter { it in systemTriggers }
            .joinToString(",") { it.token }
        val key = "${System.identityHashCode(ship ?: weapon)}:$groupIndex:$mode:$requireShipTarget:$triggerKey"
        return coordinators().getOrPut(key) { SyncGroupCoordinator() }
    }

    private fun coordinators(): MutableMap<String, SyncGroupCoordinator> {
        val engine = Global.getCombatEngine() ?: return mutableMapOf()
        return synchronizedCoordinatorStore(engine)
    }

    private fun synchronizedCoordinatorStore(engine: CombatEngineAPI): MutableMap<String, SyncGroupCoordinator> {
        val raw = engine.customData[CUSTOM_DATA_KEY]
        val existing = raw as? MutableMap<*, *>
        if (existing != null && existing.all { (key, value) ->
                key is String && value is SyncGroupCoordinator
            }
        ) {
            @Suppress("UNCHECKED_CAST")
            return existing as MutableMap<String, SyncGroupCoordinator>
        }

        val created = mutableMapOf<String, SyncGroupCoordinator>()
        engine.customData[CUSTOM_DATA_KEY] = created
        return created
    }


}
