package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.dp.advancedgunnerycontrol.weaponais.computeTimeToTravel
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f

class PhaseTag(weapon: WeaponAPI) : WeaponAITagBase(weapon) {

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean {
        if (entity !is ShipAPI) return true
        return entity.phaseCloak == null
    }

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float{
        if(mayBePhasedWhenShotConnects(solution)) return 1000f
        return 1f
    }

    override fun shouldFire(solution: FiringSolution): Boolean
    = !mayBePhasedWhenShotConnects(solution)

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false

    private fun mayBePhasedWhenShotConnects(solution: FiringSolution) : Boolean{
        if (solution.targetEntity !is ShipAPI) return false
        if (solution.targetEntity.phaseCloak == null) return false
        val pc = solution.targetEntity.phaseCloak
        val ft = solution.targetEntity.fluxTracker
        val ttt = computeTimeToTravel(weapon, solution.targetPoint)
        if (!pc.isActive && (
                    (pc.cooldownRemaining > ttt)
                    || (ft.overloadTimeRemaining > ttt)
                    || (ft.isVenting && ft.timeToVent > ttt)
                    )
        ) return false
        if (pc.isActive && ((ft.currFlux + ttt*pc.fluxPerSecond) > ft.maxFlux) && (pc.cooldown > ttt)) return false
        return true
    }
}