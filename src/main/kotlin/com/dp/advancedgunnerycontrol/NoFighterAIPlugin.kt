package com.dp.advancedgunnerycontrol

import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f

class NoFighterAIPlugin(private var baseAI : AutofireAIPlugin) : AutofireAIPlugin {
    override fun advance(p0: Float) = baseAI.advance(p0)

    override fun shouldFire(): Boolean {
        if (null != targetShip && targetShip!!.isFighter) return false
        return baseAI.shouldFire()
    }

    override fun forceOff() = baseAI.forceOff()

    override fun getTarget(): Vector2f? = baseAI.target

    override fun getTargetShip(): ShipAPI? = baseAI.targetShip

    override fun getWeapon(): WeaponAPI = baseAI.weapon

    override fun getTargetMissile(): MissileAPI? = baseAI.targetMissile
}