package com.dp.advancedgunnerycontrol.weaponais

import com.dp.advancedgunnerycontrol.WeaponControlBasePlugin
import com.fs.starfarer.api.combat.*
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.cos
import kotlin.math.sin

abstract class SpecificAIPluginBase (private val baseAI : AutofireAIPlugin, private val customAIActive : Boolean = WeaponControlBasePlugin.enableCustomAI): AutofireAIPlugin{
    private var targetEntity: CombatEntityAPI? = null
    private var forceOff = false
    private val weapon = baseAI.weapon
    private var isBaseAIValid = false
    override fun advance(p0: Float) {
        isBaseAIValid = false
        targetEntity = null
        baseAI.advance(p0)
        if (isBaseAITargetValid(baseAI.targetShip, baseAI.targetMissile)){
            isBaseAIValid = true
            baseAI.targetMissile?.let { targetEntity = it } ?: baseAI.targetShip?.let { targetEntity = it }
        }else if(customAIActive){
            advanceWithCustomAI()
        }
    }

    private fun advanceWithCustomAI(){
        var potentialTargets = getRelevantEntitiesWithinRange()
        var it : Iterator<CombatEntityAPI> = potentialTargets.iterator()
        var bestTarget : CombatEntityAPI? = null
        var priority = Float.MAX_VALUE // lowest possible priority
        it.forEach {
            val currentPriority = computeTargetPriority(it)
            if(currentPriority < priority){
                priority = currentPriority
                bestTarget = it
            }
        }
        targetEntity = bestTarget
    }

    override fun forceOff() {
        baseAI.forceOff()
        forceOff = true
    }

    override fun getTarget(): Vector2f? {
        if(isBaseAIValid){
            return baseAI.target
        }
        if(!customAIActive){
            return null
        }
        if(weapon.isBeam || weapon.isBurstBeam){
            return targetEntity?.location
        }

        val targetLocation = targetEntity?.location ?: weapon.location // distance 0 if target null
        val travelTime = (weapon.location - targetLocation).length() / weapon.projectileSpeed
//        var pointToAimAt : Vector2f? = null
//        //target.
//        target?.let { // point = target.location + target.velocity * travelTime
//
//        }
//        return pointToAimAt // will be null if target is null
        targetEntity?.let { return computePointToAimAt(it, travelTime) } ?: return null

    }

    override fun getWeapon(): WeaponAPI {
        return weapon
    }

    /**
     * approximate position where the target will be after travel time
     * based on position, velocity, acceleration and rotation
     * Note: I am somewhat unsure about this function...
     * Possibly re-estimate the time to travel after the first iteration and do a second one, but that seems overkill
     */
    private fun computePointToAimAt(tgt : CombatEntityAPI, travelT: Float) : Vector2f{
        val acceleration = (tgt as? ShipAPI)?.acceleration ?: (tgt as? MissileAPI)?.acceleration ?: 0f
        val rotationalVelocityModifier = (rotateVector(tgt.velocity, tgt.angularVelocity) - tgt.velocity) times travelT * 0.5f
        val estimatedVelocity = (tgt.velocity + rotationalVelocityModifier)
        val estimatedDirection = estimatedVelocity times 1f/estimatedVelocity.length() // normalise-method looks strange, so...
        val accelerationOffset = estimatedDirection times acceleration *travelT *travelT * 0.5f
        val velocityOffset = (tgt.velocity + rotationalVelocityModifier) times travelT
        return tgt.location + velocityOffset + accelerationOffset
    }

    private fun rotateVector(vec: Vector2f, omega : Float) : Vector2f{
        return Vector2f(vec.x* cos(omega) - vec.y* sin(omega), vec.x * sin(omega) + vec.y * cos(omega))
    }

    override fun getTargetShip(): ShipAPI? {
        return targetEntity as? ShipAPI
    }

    override fun getTargetMissile(): MissileAPI? {
        return targetEntity as? MissileAPI
    }

    protected fun isWithinArc(entity: CombatEntityAPI): Boolean {
        return weapon.distanceFromArc(entity.location) <= 0.01f
    }

    override fun shouldFire(): Boolean {
        if (isBaseAIValid){
            return baseAI.shouldFire()
        }
        // only fire if target will still be in range
        target?.let { return isInRange(it) } ?: return false
    }

    private fun isInRange(entity: Vector2f) : Boolean{
        return weapon.distanceFromArc(entity) <= 0.01f && ((weapon.location - entity).length() <= weapon.range)
    }

    /**
     * @return a value dependent on distance and velocity of target. Lower is better
     * If this turns out to eat too much performance, picking a pseudo-random target might be better
     */
    protected abstract fun computeTargetPriority(entity: CombatEntityAPI): Float

    /**
     * @return all enemy entities within weapon range and arc
     */
    protected abstract fun getRelevantEntitiesWithinRange() : List<CombatEntityAPI>

    /**
     * @return true if the target selected by the baseAI matches what the weapon should target
     */
    protected abstract fun isBaseAITargetValid(ship : ShipAPI?, missile : MissileAPI?) : Boolean
}

private infix fun Vector2f.times(d: Float): Vector2f {
    return Vector2f(d*x, d*y)
}
