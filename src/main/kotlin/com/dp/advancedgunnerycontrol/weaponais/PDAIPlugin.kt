package com.dp.advancedgunnerycontrol.weaponais

import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f

class PDAIPlugin(private var baseAI : AutofireAIPlugin) : AutofireAIPlugin {
    override fun advance(p0: Float) = baseAI.advance(p0)

    override fun shouldFire(): Boolean {
        if (null != targetMissile) return true
        if (null != targetShip && targetShip!!.isFighter) return true
        return false
    }

    override fun forceOff() = baseAI.forceOff()

    override fun getTarget(): Vector2f? = baseAI.target

    override fun getTargetShip(): ShipAPI? = baseAI.targetShip

    override fun getWeapon(): WeaponAPI = baseAI.weapon

    override fun getTargetMissile(): MissileAPI? = baseAI.targetMissile
}