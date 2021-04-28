package com.dp.advancedgunnerycontrol.weaponais

import com.dp.advancedgunnerycontrol.Settings
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
    private var targetEntity: CombatEntityAPI? = null
    private var targetPoint: Vector2f? = null
    private var forceOff = false
    private val weapon = baseAI.weapon
    private var isBaseAIValid = false
    override fun advance(p0: Float) {
        reset()
        baseAI.advance(p0)
        if (isBaseAITargetValid(baseAI.targetShip, baseAI.targetMissile)) {
            isBaseAIValid = true
            baseAI.targetMissile?.let { targetEntity = it } ?: baseAI.targetShip?.let { targetEntity = it }
        } else if (customAIActive) {
            advanceWithCustomAI()
        }
    }

    /**
     * @return a value dependent on distance and velocity of target. Lower is better
     * If this turns out to eat too much performance, picking a pseudo-random target might be better
     * hint: angularDistanceFromWeapon should probably play a big role in this!
     */
    protected abstract fun computeTargetPriority(entity: CombatEntityAPI): Float

    /**
     * @return all enemy entities within weapon range and arc
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
        isBaseAIValid = false
        targetEntity = null
        forceOff = false
        targetPoint = null
    }

    private fun advanceWithCustomAI() {
        val potentialTargets = getRelevantEntitiesWithinRange()
        val it: Iterator<CombatEntityAPI> = potentialTargets.iterator()
        var bestTarget: CombatEntityAPI? = null
        var priority = Float.MAX_VALUE // lowest possible priority
        it.forEach {
            val currentPriority = computeTargetPriority(it)
            if (currentPriority < priority) {
                priority = currentPriority
                bestTarget = it
            }
        }
        targetEntity = bestTarget
        determinePointToAimAt()
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
    protected fun computePointToAimAt(recursionLevel: Int, tgt: CombatEntityAPI): Vector2f {
        val angularVelocityCorrectionFactor = 0.25f // only use 25% of predicted angular velocity impact
        var tgtPoint = tgt.location

        for (i in 0 until recursionLevel) {
            val travelT = linearDistanceFromWeapon(tgtPoint) / weapon.projectileSpeed
            val rotationalVelocityModifier =
                (rotateVector(tgt.velocity, tgt.angularVelocity * degToRad) - tgt.velocity) times_
                        travelT * 0.5f * angularVelocityCorrectionFactor
            val velocityOffset = (tgt.velocity + rotationalVelocityModifier) times_ travelT
            tgtPoint = tgt.location + velocityOffset
        }
        return tgtPoint
    }

    private fun determinePointToAimAt() {
        if (isBaseAIValid) {
            targetPoint = baseAI.target
            return
        }
        if (!customAIActive) {
            targetPoint = null
            return
        }
        if (!isAimable(weapon)) { // no need to compute for non-aimable weapons
            targetPoint = weapon.location
            return
        }
        if (weapon.isBeam || weapon.isBurstBeam) {
            targetPoint = targetEntity?.location
            return
        }
        targetPoint =
            targetEntity?.let { computePointToAimAt(Settings.customAIRecursionLevel, it) }
    }

    private fun rotateVector(vec: Vector2f, omega: Float): Vector2f {
        return Vector2f(vec.x * cos(omega) - vec.y * sin(omega), vec.x * sin(omega) + vec.y * cos(omega))
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

    override fun shouldFire(): Boolean {
        if (isBaseAIValid) {
            return baseAI.shouldFire()
        }
        val aimingToleranceFactor = 1.25f // if aim is up to 25% off, the weapon should still fire
        val aimingToleranceFlat = 100f
        // only fire if target will still be in range
        // TODO: Refactor
        targetEntity?.let { tgt ->
            targetPoint?.let { point ->
                return isInRange(point)
                        && (angularDistanceFromWeapon(point) * linearDistanceFromWeapon(point)
                        <= tgt.collisionRadius * aimingToleranceFactor + aimingToleranceFlat)
            }

        } ?: return false
    }

    private fun isInRange(entity: Vector2f): Boolean {
        return weapon.distanceFromArc(entity) <= 0.01f && (linearDistanceFromWeapon(entity) <= weapon.range)
    }

    companion object {
        // Math.toRadians only works on doubles, which is annoying as f***
        private const val degToRad: Float = PI.toFloat() / 180f
    }
}
