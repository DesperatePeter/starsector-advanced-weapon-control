package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI

class ReduceRoFTag(weapon: WeaponAPI, factor: Float) : WeaponAITagBase(weapon) {

    private val totalCooldown = weapon.cooldown * factor
    private var timeElapsedSinceFired = 0f

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean = false

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float = 1f

    override fun shouldFire(solution: FiringSolution): Boolean {
        val dt = Global.getCombatEngine().elapsedInLastFrame
        timeElapsedSinceFired += dt
        if(weapon.isFiring){
            timeElapsedSinceFired = 0f
        }
        return timeElapsedSinceFired > totalCooldown
    }

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false
}