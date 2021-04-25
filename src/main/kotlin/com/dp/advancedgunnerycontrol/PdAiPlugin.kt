package com.dp.advancedgunnerycontrol

import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.launcher.ModManager

import org.lwjgl.util.vector.Vector2f



class PdAiPlugin constructor(
    private var assignedWeapon: WeaponAPI, baseAI: AutofireAIPlugin =
        ModManager.getInstance().pickWeaponAIPlugin(assignedWeapon)
) : AutofireAIPlugin {
    private var baseAI = baseAI
    var fireMode = FireMode.DEFAULT

    override fun advance(p0: Float) {
        return baseAI.advance(p0)
    }

    private fun shouldFireInPdMode(): Boolean {
        if (null != targetMissile) return true
        if (null != targetShip && targetShip!!.isFighter) return true
        return false
    }

    private fun shouldFireInMissileMode(): Boolean {
        if (null != targetMissile) return true
        return false
    }

    private fun shouldFireInFighterMode(): Boolean {
        if (null != targetShip && targetShip!!.isFighter) return true
        return false
    }

    override fun shouldFire(): Boolean {
        if (!baseAI.shouldFire()) return false
        return when (fireMode) {
            FireMode.DEFAULT -> baseAI.shouldFire()
            FireMode.PD -> shouldFireInPdMode()
            FireMode.MISSILE -> shouldFireInMissileMode()
            FireMode.FIGHTER -> shouldFireInFighterMode()
        }
    }

    override fun forceOff() {
        return baseAI.forceOff()
    }

    override fun getTarget(): Vector2f? {
        return baseAI.target
    }

    override fun getTargetShip(): ShipAPI? {
        return baseAI.targetShip
    }

    override fun getWeapon(): WeaponAPI? {
        return baseAI.weapon
    }

    override fun getTargetMissile(): MissileAPI? {
        return baseAI.targetMissile
    }
}