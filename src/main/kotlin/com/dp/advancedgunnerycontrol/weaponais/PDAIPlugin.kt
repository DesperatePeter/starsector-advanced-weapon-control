package com.dp.advancedgunnerycontrol.weaponais

import com.fs.starfarer.api.combat.*
import org.lwjgl.util.vector.Vector2f

class PDAIPlugin(baseAI : AutofireAIPlugin) : SpecificAIPluginBase(baseAI, false) {
    override fun computeTargetPriority(entity: CombatEntityAPI): Float {
        return 0f
    }

    override fun getRelevantEntitiesWithinRange(): List<CombatEntityAPI> {
        return emptyList()
    }

    override fun isBaseAITargetValid(ship: ShipAPI?, missile: MissileAPI?): Boolean {
        if (null != missile) return true
        ship?.let { return it.isFighter }
        return false
    }

//    override fun advance(p0: Float) = baseAI.advance(p0)
//
//    override fun shouldFire(): Boolean {
//        if (null != targetMissile) return true
//        if (null != targetShip && targetShip!!.isFighter) return true
//        return false
//    }
//
//    override fun forceOff() = baseAI.forceOff()
//
//    override fun getTarget(): Vector2f? = baseAI.target
//
//    override fun getTargetShip(): ShipAPI? = baseAI.targetShip
//
//    override fun getWeapon(): WeaponAPI = baseAI.weapon
//
//    override fun getTargetMissile(): MissileAPI? = baseAI.targetMissile

    override fun isValid() : Boolean{
        return isPD(weapon) && isAimable(weapon)
    }
}