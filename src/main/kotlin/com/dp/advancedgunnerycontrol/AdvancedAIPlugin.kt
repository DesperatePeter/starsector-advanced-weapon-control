package com.dp.advancedgunnerycontrol

import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.launcher.ModManager

import org.lwjgl.util.vector.Vector2f



class AdvancedAIPlugin constructor(
    private var assignedWeapon: WeaponAPI, private var baseAI: AutofireAIPlugin =
        ModManager.getInstance().pickWeaponAIPlugin(assignedWeapon))
    : AutofireAIPlugin {
    var fireMode = FireMode.DEFAULT
        set(value) {
            field = value
            activeAI = when(value){
                FireMode.DEFAULT -> baseAI
                FireMode.PD -> pdAI
                FireMode.MISSILE -> missileAI
                FireMode.FIGHTER -> fighterAI
            }
        }

    private val fighterAI = AdvancedFighterAIPlugin(assignedWeapon)
    private val pdAI = PDAIPlugin(baseAI)
    private val missileAI = AdvancedMissileAIPlugin(assignedWeapon)
    private var activeAI = baseAI

    override fun advance(p0: Float) = activeAI.advance(p0)

    override fun shouldFire(): Boolean = activeAI.shouldFire()

    override fun forceOff() = activeAI.forceOff()

    override fun getTarget(): Vector2f? = activeAI.target

    override fun getTargetShip(): ShipAPI? = activeAI.targetShip

    override fun getWeapon(): WeaponAPI? {
        return assignedWeapon
    }
    override fun getTargetMissile(): MissileAPI? = activeAI.targetMissile
}