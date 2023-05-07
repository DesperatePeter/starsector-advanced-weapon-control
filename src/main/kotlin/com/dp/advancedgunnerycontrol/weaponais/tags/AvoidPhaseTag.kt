package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.dp.advancedgunnerycontrol.weaponais.computeTimeToTravel
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

class AvoidPhaseTag(weapon: WeaponAPI) : WeaponAITagBase(weapon) {

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean {
        if (entity !is ShipAPI) return true
        return entity.phaseCloak == null
    }

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float =
        if (mayBePhasedWhenShotConnects(solution)) 1000f else 1f


    override fun shouldFire(solution: FiringSolution): Boolean = !mayBePhasedWhenShotConnects(solution)

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false

    private fun mayBePhasedWhenShotConnects(solution: FiringSolution): Boolean {
        if (solution.target !is ShipAPI) return false
        if (solution.target.phaseCloak == null) return false
        val phase = solution.target.phaseCloak
        val flux = solution.target.fluxTracker
        val ttt = computeTimeToTravel(weapon, solution.aimPoint)
        return if (phase.isActive) {
            val canMaintainPhase = ((flux.currFlux + ttt * phase.fluxPerSecond) < flux.maxFlux)
            val canRePhase = phase.cooldown < ttt
            // may be phased if it can maintain phase or leave phase and re-phase until shot connects
            canMaintainPhase || canRePhase
        } else {
            val canPhase = phase.cooldownRemaining < ttt
            val isOverloadedOrVenting = flux.overloadTimeRemaining > ttt || (flux.isVenting && flux.timeToVent > ttt)
            canPhase && !isOverloadedOrVenting
        }
    }
}