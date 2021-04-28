package com.dp.advancedgunnerycontrol.weaponais

import com.dp.advancedgunnerycontrol.Settings
import com.dp.advancedgunnerycontrol.Values
import com.fs.starfarer.api.combat.*
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

abstract class SpecificAIPluginBase(
    private val baseAI: AutofireAIPlugin,
    private val customAIActive: Boolean = Settings.enableCustomAI
) : AutofireAIPlugin {
    // Note: pairing targetEntity and Point could improve performance?
    private var targetEntity: CombatEntityAPI? = null
    private var lastTargetEntity: CombatEntityAPI? = null
    private var targetPoint: Vector2f? = null
    private var forceOff = false
    private val weapon = baseAI.weapon
    private var weaponShouldFire = false

    /**
     * @return a value dependent on distance and velocity of target. Lower is better
     * If this turns out to eat too much performance, picking a pseudo-random target might be better
     * hint: angularDistanceFromWeapon should probably play a big role in this!
     */
    protected abstract fun computeTargetPriority(entity: CombatEntityAPI, predictedLocation: Vector2f): Float

    /**
     * @return all enemy entities within weapon range and arc
     * hint: use CombatUtils.getXYZWithinRange(...).filterNotNull()
     */
    protected abstract fun getRelevantEntitiesWithinRange(): List<CombatEntityAPI>

    /**
     * @return true if the target selected by the baseAI matches what the weapon should target
     */
    protected abstract fun isBaseAITargetValid(ship: ShipAPI?, missile: MissileAPI?): Boolean

    /**
     * perform checks to see if this AI is compatible with the weapon
     * if this returns false, the base AI will be used instead
     */
    abstract fun isValid(): Boolean

    private fun reset() {
        lastTargetEntity = targetEntity
        targetEntity = null
        forceOff = false
        targetPoint = null
        weaponShouldFire = false
    }

    override fun advance(p0: Float) {
        reset()
        if(!advanceBaseAI(p0) && customAIActive){
            advanceWithCustomAI()
        }
    }

    private fun advanceBaseAI(p0: Float) : Boolean{
        baseAI.advance(p0)
        if (isBaseAITargetValid(baseAI.targetShip, baseAI.targetMissile)) {
            baseAI.targetMissile?.let { targetEntity = it } ?: baseAI.targetShip?.let { targetEntity = it }
            baseAI.target?.let { targetPoint = it }
            weaponShouldFire = baseAI.shouldFire()
            return true
        }
        return false
    }

    private fun advanceWithCustomAI() {
        val potentialTargets = addPredictedLocationToTargets(getRelevantEntitiesWithinRange()).filter {
            isInRange(it.second) }
        val it = potentialTargets.iterator()
        var bestTarget = Pair<CombatEntityAPI?, Vector2f?>(null, null)
        var priority = Float.MAX_VALUE // lowest possible priority
        it.forEach {
            val currentPriority = computeTargetPriority(it.first, it.second)
            if (currentPriority < priority) {
                priority = currentPriority
                bestTarget = it
            }
        }
        targetEntity = bestTarget.first
        targetPoint = bestTarget.second
        weaponShouldFire = computeIfShouldFire(potentialTargets)
    }

    private fun addPredictedLocationToTargets(potentialTargets : List<CombatEntityAPI>) : List<Pair<CombatEntityAPI, Vector2f>>{
        return potentialTargets.map {
            Pair(it, computePointToAimAt(it))
        }
    }

    override fun forceOff() {
        baseAI.forceOff()
        forceOff = true
    }

    override fun getTarget(): Vector2f? {
        return targetPoint
    }

    override fun getWeapon(): WeaponAPI {
        return weapon
    }

    /**
     * approximate position where the target will be after travel time
     * based on position, velocity and rotation
     * Note: I am somewhat unsure about this function...
     * Some observations:
     * - angles are in degrees
     * - target ships can strafe, i.e. velocity direction =/= heading
     * - acceleration refers to max possible acceleration, not current acceleration
     * conclusion: Don't use acceleration, take angular velocity with a grain of salt
     */
    protected fun computePointToAimAt(tgt: CombatEntityAPI): Vector2f {
        // val angularVelocityCorrectionFactor = 0.25f // only use 25% of predicted angular velocity impact
        var tgtPoint = tgt.location
        // no need to compute stuff for beam or non-aimable weapons
        if (weapon.isBeam || weapon.isBurstBeam || !isAimable(weapon)) {
            return tgtPoint
        }

        for (i in 0 until Settings.customAIRecursionLevel) {
            val travelT = linearDistanceFromWeapon(tgtPoint) / weapon.projectileSpeed
//            val rotationalVelocityModifier = // I believe not using this is easier and better
//                (rotateVector(tgt.velocity, tgt.angularVelocity * degToRad) - tgt.velocity) times_
//                        travelT * 0.5f * angularVelocityCorrectionFactor
            val velocityOffset = (tgt.velocity /*+ rotationalVelocityModifier*/) times_ travelT
            tgtPoint = tgt.location + velocityOffset
        }
        return tgtPoint
    }

//    private fun rotateVector(vec: Vector2f, omega: Float): Vector2f {
//        return Vector2f(vec.x * cos(omega) - vec.y * sin(omega), vec.x * sin(omega) + vec.y * cos(omega))
//    }

    override fun getTargetShip(): ShipAPI? {
        return targetEntity as? ShipAPI
    }

    override fun getTargetMissile(): MissileAPI? {
        return targetEntity as? MissileAPI
    }

    protected fun isWithinArc(entity: CombatEntityAPI): Boolean {
        return weapon.distanceFromArc(entity.location) <= 0.01f
    }

    /**
     * @return approximate angular distance of target from current weapon facing in rad
     * note: approximation works well for small values and is off by a factor of PI/2 for 180°
     */
    protected fun angularDistanceFromWeapon(entity: Vector2f): Float {
        val weaponDirection = Vector2f(cos(weapon.currAngle * degToRad), sin(weapon.currAngle * degToRad))
        val entityDirection = entity times_ (1f / entity.length())
        return (weaponDirection - entityDirection).length()
    }

    protected fun linearDistanceFromWeapon(entity: Vector2f): Float {
        return (weapon.location - entity).length()
    }

    protected fun willBeInFiringRange(entity: CombatEntityAPI) : Boolean{
        return isInRange(computePointToAimAt(entity))
    }

    private fun computeIfShouldFire(potentialTargets: List<Pair<CombatEntityAPI, Vector2f>>): Boolean{
        // Note: In a sequence, all calculations are done on the first element before moving to the next
        potentialTargets.asSequence().filter { isInRange(it.second) }.iterator().forEach{
            val tolerance = it.first.collisionRadius * aimingToleranceFactor + aimingToleranceFlat
            val lateralTargetOffset = angularDistanceFromWeapon(it.second) * linearDistanceFromWeapon(it.second)
            if (lateralTargetOffset <= tolerance) return true
        }
        return false
    }

    protected fun computePriorityGeometrically(entity: CombatEntityAPI, predictedLocation: Vector2f) : Float {
        return predictedLocation.let {
            angularDistanceFromWeapon(it) + Values.distToAngularDistEvalutionFactor *linearDistanceFromWeapon(it)
        }.let {
            if (lastTargetEntity == entity) it*0.05f else it // heavily incentivize sticking to one target
        }
    }

    override fun shouldFire(): Boolean {
        return weaponShouldFire
    }

    private fun isInRange(entity: Vector2f): Boolean {
        return weapon.distanceFromArc(entity) <= 0.01f && (linearDistanceFromWeapon(entity) <= weapon.range)
    }

    companion object {
        // Math.toRadians only works on doubles, which is annoying as f***
        protected const val degToRad: Float = PI.toFloat() / 180f
        protected val aimingToleranceFactor = 1.25f * Settings.customAITriggerHappiness// if aim is up to 25% off, the weapon should still fire
        protected val aimingToleranceFlat = 50f * Settings.customAITriggerHappiness
        // protected val aimingToleranceAccelFactor = 0.5f * Settings.customAITriggerHappiness
    }
}
