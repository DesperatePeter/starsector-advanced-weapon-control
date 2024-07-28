package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BeamAPI
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI
import java.lang.ref.WeakReference

class InterdictBeamsTag(weapon: WeaponAPI) : WeaponAITagBase(weapon) {

    private var beamsTargetingThisShip = listOf<WeakReference<BeamAPI>>()
    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        return if(isTargetBeamingThisShip(solution.target)) 0.01f else 1f
    }

    override fun shouldFire(solution: FiringSolution): Boolean = true

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false
    override fun forceFire(solution: FiringSolution?, baseDecision: Boolean): Boolean {
        return solution?.target?.let { isTargetBeamingThisShip(it) } ?: return false
    }

    override fun addFarAwayTargets(): List<CombatEntityAPI> {
        return getBeamsTargetingThisShip().mapNotNull { it.source }.toList()
    }

    override fun advance() {
        super.advance()
        fetchBeamsTargetingThisShip()
    }

    private fun fetchBeamsTargetingThisShip(){
        beamsTargetingThisShip = (Global.getCombatEngine()?.beams?.filter { it.damageTarget == weapon.ship } ?: emptyList()).map{
            WeakReference(it)
        }
    }

    private fun getBeamsTargetingThisShip(): List<BeamAPI>{
        return beamsTargetingThisShip.mapNotNull { it.get() }
    }

    private fun isTargetBeamingThisShip(target: CombatEntityAPI): Boolean{
        return getBeamsTargetingThisShip().any { it.source == target }
    }

}