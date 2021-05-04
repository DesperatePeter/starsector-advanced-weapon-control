package com.dp.advancedgunnerycontrol.weaponais

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.fs.starfarer.api.combat.*
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.*

abstract class SpecificAIPluginBase(
    private val baseAI: AutofireAIPlugin,
    private val customAIActive: Boolean = Settings.enableCustomAI()
) : AutofireAIPlugin {
    private var targetEntity: CombatEntityAPI? = null
    private var lastTargetEntity: CombatEntityAPI? = null
    private var targetPoint: Vector2f? = null
    private var forceOff = false
    private val weapon = baseAI.weapon
    private var weaponShouldFire = false
    private var currentTgtLeadAcc = 1.0f
    private var lastP0 = 0.0f

    /**
     * @return a value dependent on distance and velocity of target. Lower is better
     * If this turns out to eat too much performance, picking a pseudo-random target might be better
     * hint: use computeBasePriority
     * simply use return 0f if isBaseAIOverwritable = false
     */
    protected abstract fun computeTargetPriority(entity: CombatEntityAPI, predictedLocation: Vector2f): Float

    /**
     * @return all enemy entities within weapon range and arc
     * hint: use something like:
     * CombatUtils.getXYZWithinRange(...).filterNotNull()
     * simply use emptyList() if isBaseAIOverwritable = false
     */
    protected abstract fun getRelevantEntitiesWithinRange(): List<CombatEntityAPI>

    /**
     * @return true if the target selected by the baseAI matches what the weapon should target
     */
    protected abstract fun isBaseAITargetValid(ship: ShipAPI?, missile: MissileAPI?): Boolean

    /**
     * @return true if the weapon should try to acquire a new target using custom AI if base AI fails
     */
    protected abstract fun isBaseAIOverwritable(): Boolean

    /**
     * perform checks to see if this AI is compatible with the weapon
     * if this returns false, the base AI will be used instead
     */
    abstract fun isValid(): Boolean

    /**
     * gets called at start of every advancement frame
     */
    private fun reset() {
        determineTargetLeadingAccuracy(targetEntity, lastTargetEntity)
        lastTargetEntity = targetEntity
        targetEntity = null
        forceOff = false
        targetPoint = null
        weaponShouldFire = false
    }

    override fun advance(p0: Float) {
        lastP0 = p0
        reset()
        if (!advanceBaseAI(p0) && customAIActive) {
            advanceWithCustomAI()
        }
    }

    protected fun advanceBaseAI(p0: Float): Boolean {
        if(Settings.forceCustomAI() && isBaseAIOverwritable()) return false
        baseAI.advance(p0)
        if (isBaseAITargetValid(baseAI.targetShip, baseAI.targetMissile)) {
            baseAI.targetMissile?.let { targetEntity = it } ?: baseAI.targetShip?.let { targetEntity = it }
            baseAI.target?.let { targetPoint = it }
            weaponShouldFire = baseAI.shouldFire()
            return true
        }
        return false
    }

    protected fun advanceWithCustomAI() {

        var potentialTargets = addPredictedLocationToTargets(
            getRelevantEntitiesWithinRange().filter { isWithinArc(it) && isHostile(it) }
        ).filter { isInRange(it.second) }

        // TODO: It would be faster to get friendlies and foes in one go
        if (Settings.customAIFriendlyFireComplexity() >= 2) {
            // this is a deceptively expensive call (therefore locked behind opt-in setting)
            potentialTargets = potentialTargets.filter { !isFriendlyFire(it.second, getFriendlies()) }
        }
        val bestTarget = potentialTargets.minBy {
            computeTargetPriority(it.first, it.second)
        }

        targetEntity = bestTarget?.first
        targetPoint = bestTarget?.second
        weaponShouldFire = computeIfShouldFire(potentialTargets)
    }

    protected fun getFriendlies(): List<Pair<CombatEntityAPI, Vector2f>> {
        return addPredictedLocationToTargets(
            CombatUtils.getShipsWithinRange(weapon.location, weapon.range).filter {
                (it.isAlly || (it.owner == 0)) && isWithinArc(it) && !it.isFighter
            }).filter { isInRange(it.second) }
    }

    protected fun determineTargetLeadingAccuracy(currentTarget: CombatEntityAPI?, lastTarget: CombatEntityAPI?) {
        if (Settings.customAIPerfectTargetLeading()) {
            currentTgtLeadAcc = 1.0f
            return
        }
        currentTgtLeadAcc = if (currentTarget == lastTarget) {
            min(currentTgtLeadAcc + 0.2f*lastP0, 1.0f)
        } else {
            weapon.ship?.mutableStats?.autofireAimAccuracy?.modifiedValue ?: 1.0f
        }
    }

    protected fun addPredictedLocationToTargets(potentialTargets: List<CombatEntityAPI>): List<Pair<CombatEntityAPI, Vector2f>> {
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
        var tgtPoint = tgt.location
        // no need to compute stuff for beam or non-aimable weapons
        if (weapon.isBeam || weapon.isBurstBeam || !isAimable(weapon)) {
            return tgtPoint
        }

        for (i in 0 until Settings.customAIRecursionLevel()) {
            val travelT =
                linearDistanceFromWeapon(tgtPoint) / (weapon.projectileSpeed * (1.5f - 0.5f * currentTgtLeadAcc))

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
        val distance = entity - weapon.location
        val entityDirection = distance times_ (1f / distance.length())
        return (weaponDirection - entityDirection).length()
    }

    protected fun isFriendlyFire(aimPoint: Vector2f, friendlies: List<Pair<CombatEntityAPI, Vector2f>>): Boolean {
        return friendliesInDangerZone(aimPoint, friendlies).isNotEmpty()
    }

    protected fun friendliesInDangerZone(
        aimPoint: Vector2f,
        friendlies: List<Pair<CombatEntityAPI, Vector2f>>
    ): List<Pair<CombatEntityAPI, Vector2f>> {
        return friendlies.filter {
            abs(angularDistanceFromWeapon(aimPoint) - angularDistanceFromWeapon(it.second)) * linearDistanceFromWeapon(
                it.second
            ) <=
                    it.first.collisionRadius * Settings.customAIFriendlyFireCaution()
        }
    }

    protected fun linearDistanceFromWeapon(entity: Vector2f): Float {
        return (weapon.location - entity).length()
    }

    protected fun computeIfShouldFire(potentialTargets: List<Pair<CombatEntityAPI, Vector2f>>): Boolean {
        // Note: In a sequence, all calculations are done on the first element before moving to the next
        potentialTargets.asSequence().filter { isInRange(it.second) }.iterator().forEach {
            val tolerance = it.first.collisionRadius * aimingToleranceFactor + aimingToleranceFlat
            val lateralTargetOffset = angularDistanceFromWeapon(it.second) * linearDistanceFromWeapon(it.second)
            if (lateralTargetOffset <= tolerance) return true
        }
        if (Settings.customAIFriendlyFireComplexity() >= 1) {
            if (isFriendlyFire(vectorFromAngleDeg(weapon.currAngle), getFriendlies())) return false
        }
        return false
    }

    protected fun computeBasePriority(entity: CombatEntityAPI, predictedLocation: Vector2f): Float {
        return predictedLocation.let {
            angularDistanceFromWeapon(it) + Values.distToAngularDistEvalutionFactor * linearDistanceFromWeapon(it)
        }.let {
            if (lastTargetEntity == entity) it * 0.05f else it // heavily incentivize sticking to one target
        }
    }

    override fun shouldFire(): Boolean {
        return weaponShouldFire
    }

    protected fun isInRange(entity: Vector2f): Boolean {
        return weapon.distanceFromArc(entity) <= 0.01f && (linearDistanceFromWeapon(entity) <= weapon.range)
    }

    companion object {
        protected val aimingToleranceFactor =
            1.0f * Settings.customAITriggerHappiness()// if aim is up to 25% off, the weapon should still fire
        protected val aimingToleranceFlat = 10f * Settings.customAITriggerHappiness()
    }
}
