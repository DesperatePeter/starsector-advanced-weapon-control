package com.dp.advancedgunnerycontrol.weaponais

import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.dp.advancedgunnerycontrol.settings.Settings
import org.lazywizard.lazylib.combat.CombatUtils
import org.lwjgl.util.vector.Vector2f

class MiningAI(baseAI: AutofireAIPlugin) : SpecificAIPluginBase(baseAI) {
    private var asteroids : MutableList<CombatEntityAPI> = mutableListOf()
    private var tgtPoint : Vector2f? = null
    private var tofire = false
    // don't care
    override fun computeTargetPriority(entity: CombatEntityAPI, predictedLocation: Vector2f): Float =
        computeBasePriority(entity, predictedLocation)

    override fun getRelevantEntitiesWithinRange(): List<CombatEntityAPI> {
        asteroids = CombatUtils.getAsteroidsWithinRange(weapon.location, weapon.range).filterNotNull().toMutableList()
        return asteroids
    }

    override fun isBaseAITargetValid(ship: ShipAPI?, missile: MissileAPI?): Boolean = false //ships and missiles are not valid!

    override fun isValid(): Boolean = true

    override fun getTargetMissile(): MissileAPI? = null
    override fun getTargetShip(): ShipAPI? = null
    override fun getTarget(): Vector2f? = tgtPoint
    override fun advance(p0: Float) {
        getRelevantEntitiesWithinRange()
        if(asteroids.isEmpty()){
            tgtPoint = null
            tofire = false
            return
        }
        val priorities = addPredictedLocationToTargets(asteroids)
        tgtPoint = priorities.minBy { computeTargetPriority(it.first, it.second) }?.second
        tofire = asteroids.any {
            (angularDistanceFromWeapon(it.location) * linearDistanceFromWeapon(it.location) <=
                    (it.collisionRadius * Settings.customAITriggerHappiness()) + 50f)
        }
        tofire
    }

    override fun isBaseAIOverwritable(): Boolean = true

    override fun shouldFire(): Boolean = tofire
}