package com.dp.advancedgunnerycontrol.weaponais

// Notes:
// Initially I was unaware that the player ship's current velocity affects targeting, so some of the code here is a bit patchy
// Most of the time a "angular distance", i.e. the sin, is used instead of angles in calculations, as sin(x) ~= x for small angles

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.dp.advancedgunnerycontrol.utils.*
import com.fs.starfarer.api.combat.*
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.plus
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.*

abstract class SpecificAIPluginBase(
    val baseAI: AutofireAIPlugin,
    private val customAIActive: Boolean = Settings.enableCustomAI()
) : AutofireAIPlugin {
    protected var solution: FiringSolution? = null
    private var lastTargetEntity: CombatEntityAPI? = null
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
    protected abstract fun computeTargetPriority(solution: FiringSolution): Float

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
        determineTargetLeadingAccuracy(solution?.target, lastTargetEntity)
        lastTargetEntity = solution?.target
        solution = null
        forceOff = false
        weaponShouldFire = false
    }

    override fun advance(p0: Float) {
        lastP0 = p0
        reset()
        if (!advanceBaseAI(p0) && customAIActive && isBaseAIOverwritable()) {
            advanceWithCustomAI()
        }
    }

    protected fun advanceBaseAI(p0: Float): Boolean {
        if (Settings.forceCustomAI() && isBaseAIOverwritable()) return false
        baseAI.advance(p0)
        val ship = baseAI.targetShip
        val missile = baseAI.targetMissile
        val targetEntity = ship as? CombatEntityAPI ?: missile as? CombatEntityAPI
        if (targetEntity != null && baseAI.target != null && isBaseAITargetValid(ship, missile)) {
            solution = FiringSolution(targetEntity, baseAI.target)
            weaponShouldFire = baseAI.shouldFire()
            return true
        }
        return false
    }

    protected fun advanceWithCustomAI() {
        var potentialTargets = calculateFiringSolutions(
            getRelevantEntitiesWithinRange().filter { isHostile(it) }
        ).filter { isInRange(it.aimPoint, effectiveCollRadius(it.target)) }

        // TODO: It would be faster to get friendlies and foes in one go
        if (Settings.customAIFriendlyFireComplexity() >= 2) {
            // this is a deceptively expensive call (therefore locked behind opt-in setting)
            potentialTargets = potentialTargets.filter { !isFriendlyFire(getFriendlies(), it.aimPoint) }
        }

        solution = potentialTargets.minByOrNull { computeTargetPriority(it) }

        computeIfShouldFire(potentialTargets).let {
            weaponShouldFire = it
        }
    }

    protected open fun getFriendlies(): List<FiringSolution> {
        return calculateFiringSolutions(
            CombatUtils.getShipsWithinRange(weapon.location, weapon.range).filter { it != weapon.ship }.filter {
                (it.isAlly || (it.owner == 0) || (it.owner == 100 && shouldConsiderNeutralsAsFriendlies())) && !it.isFighter
            }).filter { isInRange(it.aimPoint, effectiveCollRadius(it.target) * Settings.customAIFriendlyFireCaution())
                && isWithinArc(it.aimPoint, effectiveCollRadius(it.target) * Settings.customAIFriendlyFireCaution()) }
    }

    protected fun determineTargetLeadingAccuracy(currentTarget: CombatEntityAPI?, lastTarget: CombatEntityAPI?) {
        if (Settings.customAIPerfectTargetLeading()) {
            currentTgtLeadAcc = 1.0f
            return
        }
        currentTgtLeadAcc = if (currentTarget == lastTarget) {
            min(currentTgtLeadAcc + 0.2f * lastP0, 1.0f)
        } else {
            weapon.ship?.mutableStats?.autofireAimAccuracy?.modifiedValue ?: 1.0f
        }
    }

    protected fun compensateTargetPointShipSpeed(tgt: Vector2f, ttt: Float): Vector2f {
        val vel = weapon.ship?.velocity ?: Vector2f(0.0f, 0.0f)
        return tgt - (vel times_ ttt)
    }

    // compensates for both player ship and target velocities
    protected fun calculateFiringSolutions(potentialTargets: List<CombatEntityAPI>): List<FiringSolution> {
        return potentialTargets.map {
            FiringSolution(it, computePointToAimAt(it))
        }
    }

    override fun forceOff() {
        baseAI.forceOff()
        forceOff = true
    }

    override fun getTarget(): Vector2f? {
        return solution?.aimPoint ?: getNeutralPosition(weapon)
    }

    override fun getWeapon(): WeaponAPI {
        return weapon
    }

    /**
     * approximate position where the target will be after travel time
     * based on player ship and target velocities.
     * In a way, this transforms points from an absolute to a "relative" frame
     * Some observations:
     * - angles are in degrees
     * - target ships can strafe, i.e. velocity direction =/= heading
     * - acceleration refers to max possible acceleration, not current acceleration
     * conclusion: Don't use acceleration or angular velocity, they are unreliable
     */
    protected fun computePointToAimAt(tgt: CombatEntityAPI): Vector2f {
        if (!isAimable(weapon)) {
            return getNeutralPosition(weapon)
        }
        var tgtPoint = tgt.location
        // no need to compute stuff for beam or non-aimable weapons
        if (weapon.isBeam || weapon.isBurstBeam) {
            return tgtPoint
        }

        for (i in 0 until Settings.customAIRecursionLevel()) {
            val travelT = computeTimeToTravel(tgtPoint)

            val velocityOffset = (tgt.velocity) times_ travelT
            tgtPoint = compensateTargetPointShipSpeed(tgt.location + velocityOffset, travelT)
        }
        return tgtPoint
    }

    protected fun computeTimeToTravel(tgt: Vector2f): Float {
        return computeTimeToTravel(weapon, tgt, (1.5f - 0.5f * currentTgtLeadAcc))
    }

    override fun getTargetShip(): ShipAPI? {
        return solution?.target as? ShipAPI
    }

    override fun getTargetMissile(): MissileAPI? {
        return solution?.target as? MissileAPI
    }

    protected fun isWithinArc(entity: CombatEntityAPI): Boolean {
        if (!isAimable(weapon)) return true
        return isWithinArc(entity.location, effectiveCollRadius(entity))
    }

    protected fun isWithinArc(position: Vector2f, radius: Float): Boolean {
        // Note: This is using an approximated angle, which should be fine as angles should be rather small
        return weapon.distanceFromArc(position) <=
                (radius / (weapon.location - position).length()) * 180f / PI * aimingToleranceFactor
    }

    protected open fun shouldConsiderNeutralsAsFriendlies(): Boolean = true

    // if aimPoint == null, the current weapon facing will be used
    protected fun isFriendlyFire(friendlies: List<FiringSolution>, aimPoint: Vector2f? = null): Boolean = when {
        weapon.spec.weaponId == "guardian" -> false // Paladin PD can shoot over friendlies
        !isAimable(weapon) -> false                 // Guided missiles can shoot over friendlies
        friendliesInDangerZone(friendlies, aimPoint).isEmpty() -> false
        else -> true
    }

    protected fun friendliesInDangerZone(friendlies: List<FiringSolution>, aimPoint: Vector2f? = null):
            List<FiringSolution> {

        return friendlies.filter {
            val isCloserThanTgt = solution?.aimPoint?.let { tp -> linearDistanceFromWeapon(it.aimPoint, weapon) < linearDistanceFromWeapon(tp, weapon)} ?: true
            determineIfShotWillHit(it.aimPoint, effectiveCollRadius(it.target) * Settings.customAIFriendlyFireCaution(), weapon, aimPoint) &&
                    isCloserThanTgt
        }
    }

    protected open fun computeIfShouldFire(potentialTargets: List<FiringSolution>): Boolean {
        if (!isAimable(weapon)) {
            return potentialTargets.isNotEmpty()
        }

        if (Settings.customAIFriendlyFireComplexity() >= 1) {
            if (isFriendlyFire(getFriendlies())) return false
        }
        // Note: In a sequence, all calculations are done on the first element before moving to the next
        potentialTargets.asSequence().filter { isInRange(it.aimPoint, effectiveCollRadius(it.target)) }.iterator().forEach {
            val effectiveCollisionRadius = effectiveCollRadius(it.target) * aimingToleranceFactor + aimingToleranceFlat
            if(determineIfShotWillHit(it.aimPoint, effectiveCollisionRadius, weapon)) return true
        }

        return false
    }

    protected fun computePointCurrentlyAimedAt() : Vector2f {
        val ship = weapon.ship ?: return Vector2f(0f,0f)
        return (vectorFromAngleDeg(weapon.currAngle) times_ weapon.range) + ship.location
    }

    /**
     * @brief compute a priority number for a target based on lin/angular distance, whether it's the ship target etc
     * @return low (>=0) number for high priority targets, high number for low priority targets
     */
    protected fun computeBasePriority(solution: FiringSolution): Float {
        return solution.aimPoint.let {
            angularDistanceFromWeapon(it, weapon) + Values.distToAngularDistEvaluationFactor * linearDistanceFromWeapon(it, weapon) + 1.5f
        }.let {
            if (lastTargetEntity == solution.target) it * 0.5f else it // incentivize sticking to one target
        } *
                (if(solution.target as? ShipAPI == weapon.ship.shipTarget) 0.05f else 1.0f) * // heavily incentivize targeting the ship target
                (if((solution.target as? ShipAPI)?.isFighter == false) 1.0f else 1.2f) // prioritize regular ships over other stuff
    }

    override fun shouldFire(): Boolean {
        return weaponShouldFire
    }

    protected fun isInRange(entity: Vector2f, radius: Float = 0f): Boolean {
        return linearDistanceFromWeapon(entity, weapon) - radius <=
                weapon.range + (0.25f * Settings.customAITriggerHappiness() * weapon.projectileFadeRange)
    }

    companion object {
        protected val aimingToleranceFactor = 1.0f * Settings.customAITriggerHappiness()
        protected val aimingToleranceFlat = 10f * Settings.customAITriggerHappiness()
    }
}
