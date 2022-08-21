package com.dp.advancedgunnerycontrol.weaponais.tags

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

    override fun computeTargetPriorityModifier(entity: CombatEntityAPI, predictedLocation: Vector2f): Float{
        if(mayBePhasedWhenShotConnects(entity, predictedLocation)) return 1000f
        return 1f
    }

    override fun shouldFire(entity: CombatEntityAPI, predictedLocation: Vector2f): Boolean
    = !mayBePhasedWhenShotConnects(entity, predictedLocation)

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false

    private fun mayBePhasedWhenShotConnects(entity: CombatEntityAPI, predictedLocation: Vector2f) : Boolean{
        if (entity !is ShipAPI) return false
        if (entity.phaseCloak == null) return false
        val pc = entity.phaseCloak
        val ft = entity.fluxTracker
        val ttt = computeTimeToTravel(weapon, predictedLocation)
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