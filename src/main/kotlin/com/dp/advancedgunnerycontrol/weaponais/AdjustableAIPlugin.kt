package com.dp.advancedgunnerycontrol.weaponais

import com.dp.advancedgunnerycontrol.typesandvalues.FMValues
import com.dp.advancedgunnerycontrol.typesandvalues.FireMode
import com.dp.advancedgunnerycontrol.typesandvalues.Suffixes
import com.dp.advancedgunnerycontrol.typesandvalues.createSuffix
import com.dp.advancedgunnerycontrol.weaponais.suffixes.SuffixBase
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

import org.lwjgl.util.vector.Vector2f


class AdjustableAIPlugin constructor(private val baseAI: AutofireAIPlugin) : AutofireAIPlugin {

    private var activeAI = baseAI
    private var suffix = SuffixBase(baseAI.weapon)
    private var aiPlugins = FMValues.modeToPluginMap(baseAI, suffix)

    fun setSuffix(sfx : Suffixes?){
        suffix = createSuffix(sfx, baseAI.weapon)
    }

    fun switchFireMode(mode: FireMode): Boolean {
        activeAI = aiPlugins[mode] ?: baseAI

        if (isInvalid(activeAI)) {
            activeAI = baseAI; return false
        }
        return true
    }

    override fun advance(p0: Float) = activeAI.advance(p0)

    override fun shouldFire(): Boolean = activeAI.shouldFire() && (!suffix.suppressFire())

    override fun forceOff() = activeAI.forceOff()

    override fun getTarget(): Vector2f? = activeAI.target

    override fun getTargetShip(): ShipAPI? = activeAI.targetShip

    override fun getWeapon(): WeaponAPI? = activeAI.weapon

    override fun getTargetMissile(): MissileAPI? = activeAI.targetMissile
}