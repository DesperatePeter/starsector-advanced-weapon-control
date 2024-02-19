@file:Suppress("FunctionName")

package com.dp.advancedgunnerycontrol.weaponais

import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.Tags
import org.lazywizard.lazylib.CollisionUtils
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.util.*
import kotlin.math.*

// Math.toRadians only works on doubles, which is annoying....
const val degToRad: Float = PI.toFloat() / 180f

data class FiringSolution(
    val target: CombatEntityAPI,

    // Point at which the weapon should aim to hit
    // the target dead center, under the assumption
    // that target velocity is constant.
    val aimPoint: Vector2f
)

fun ShipAPI.hasPhaseCloak(): Boolean{
    return phaseCloak != null && hullSpec.hints.contains(ShipHullSpecAPI.ShipTypeHints.PHASE)
}

fun isPD(weapon: WeaponAPI): Boolean {
    if (weapon.hasAIHint(WeaponAPI.AIHints.PD) || weapon.hasAIHint(WeaponAPI.AIHints.PD_ONLY)
    ) return true
    return ((weapon.ship?.variant?.hasHullMod("pointdefenseai") == true)
            && (weapon.size == WeaponAPI.WeaponSize.SMALL) && (weapon.type != WeaponAPI.WeaponType.MISSILE))
}

fun isAimable(weapon: WeaponAPI): Boolean {
    return weapon.spec?.trackingStr?.lowercase(Locale.getDefault()) in setOf(null, "", "none") &&
            !(weapon.hasAIHint(WeaponAPI.AIHints.DO_NOT_AIM))
}

fun isInvalid(aiPlugin: AutofireAIPlugin): Boolean {
    (aiPlugin as? SpecificAIPluginBase)?.let {
        if (it.weapon.id in Settings.weaponBlacklist) return true
        return !it.isValid()
    }
    // if it's note one of my plugins it's safe to assume that it's valid (at least it's not my job)
    return false
}

fun ammoLevel(weapon: WeaponAPI): Float {
    if (!weapon.usesAmmo()) return 1.0f
    return weapon.ammo.toFloat() / weapon.maxAmmo.toFloat()
}

const val bignessFrigate = 0.5f
const val bignessDestroyer = 2f
const val bignessCruiser = 5f
const val bignessCapital = 20f
const val bignessFighter = 0.1f
fun isBig(ship: ShipAPI): Boolean {
    if (!Settings.strictBigSmall()) return true
    return bigness(ship) > bignessFrigate + 0.1f
}

fun isSmall(ship: ShipAPI): Boolean {
    if (!Settings.strictBigSmall()) return true
    return bigness(ship) < bignessCruiser - 0.1f
}

fun bigness(ship: ShipAPI): Float {
    return when (ship.hullSize) {
        ShipAPI.HullSize.FRIGATE -> bignessFrigate
        ShipAPI.HullSize.DESTROYER -> bignessDestroyer
        ShipAPI.HullSize.CRUISER -> bignessCruiser
        ShipAPI.HullSize.CAPITAL_SHIP -> bignessCapital
        else -> bignessFighter
    }
}

fun isHostile(entity: CombatEntityAPI): Boolean {
    if((entity as? ShipAPI)?.variant?.hasHullMod(HullMods.VASTBULK) == true) return false
    return entity.owner == 1
}

fun getNeutralPosition(weapon: WeaponAPI): Vector2f {
    return weapon.location + (vectorFromAngleDeg(weapon.ship.facing) times_ 100f)
}

fun isOpportuneTarget(solution: FiringSolution?, weapon: WeaponAPI): Boolean {
    val target = solution?.target as? ShipAPI ?: return false
    val p = solution.aimPoint
    if (!isOpportuneType(target, weapon)) return false
    var trackingFactor = when (weapon.spec?.trackingStr?.lowercase(Locale.getDefault())) {
        "none" -> 1.0f
        "very poor" -> 1.25f
        "poor" -> 1.5f
        "medium" -> 2.0f
        "special" -> 2.0f
        "good" -> 2.5f
        "excellent" -> 3.0f
        else -> 1.0f
    } * 0.2f * Settings.opportunistModifier()
    if (weapon.id?.contains("sabot") == true) trackingFactor *= 3
    if (target.maxSpeed > weapon.projectileSpeed * trackingFactor) return false
    val ttt = (weapon.location - p).length() / weapon.projectileSpeed
    val ammoLessModifier = if (!weapon.usesAmmo()) 1.0f else if (weapon.ammoTracker.reloadSize > 0f) 0.5f else 0.1f
    return ((p - weapon.location).length() - effectiveCollRadius(target) * ammoLessModifier + ttt * target.maxSpeed * 0.1f / ammoLessModifier) <= weapon.range * 0.95f * Settings.opportunistModifier()
}

private fun isOpportuneType(target: ShipAPI, weapon: WeaponAPI): Boolean {
    if (weapon.spec?.primaryRoleStr?.lowercase(Locale.getDefault()) == "finisher") {
        return isDefenseless(target, weapon)
    }
    if (weapon.damageType == DamageType.HIGH_EXPLOSIVE || weapon.damageType == DamageType.FRAGMENTATION) {
        return computeShieldFactor(target, weapon) < Settings.opportunistHEThreshold()
    }
    if (weapon.damageType == DamageType.KINETIC) {
        return computeShieldFactor(target, weapon) > Settings.opportunistKineticThreshold()
    }
    return true
}

/**
 * @return [0.01...~1.0] a small value if target is unshielded, has shields off or is at high flux
 */
fun computeShieldFactor(tgtShip: CombatEntityAPI, weapon: WeaponAPI, ttt: Float = 1f): Float {
    if (tgtShip.shield == null || (tgtShip.shield.type != ShieldAPI.ShieldType.FRONT && tgtShip.shield.type != ShieldAPI.ShieldType.OMNI)) {
        return 0.01f
    }
    if (isDefenseless(tgtShip, weapon)) return 0.01f
    return computeFluxBasedShieldFactor(tgtShip) * computeShieldFacingFactor(tgtShip, weapon, ttt)
}

fun computeFluxBasedShieldFactor(tgtShip: CombatEntityAPI): Float {
    return (1.0f - ((tgtShip as? ShipAPI)?.fluxLevel ?: 1f)) * (if (tgtShip.shield?.isOn == true) 1.0f else 0.75f)
}

/**
 * @return a factor between 0.01f (shot will bypass shields) and 1f (shot will hit shields). A value in between if it's unclear
 */
fun computeShieldFacingFactor(tgtShip: CombatEntityAPI, weapon: WeaponAPI, ttt: Float): Float {
    // Note: Angles in Starsector are always 0..360, 0 means east/right
    val shield = tgtShip.shield ?: return 0.01f
    if (shield.type == ShieldAPI.ShieldType.OMNI && shield.isOff) {
        return 0.9f // Turned off omni shields means we don't know shit
    }
    val sCov = 0.5f * min(shield.arc, shield.activeArc + (ttt / shield.unfoldTime) * shield.arc)
    val flankingAngle = abs(180f - abs(weapon.currAngle - shield.facing))
    return 1.0f - min(
        1.0f,
        max(0.01f, (flankingAngle - sCov) / 15.0f)
    ) // missing the shields by 15° or more means bypassing shot
}

fun computeTimeToTravel(weapon: WeaponAPI, tgt: Vector2f, leadingFactor: Float = 1f): Float {
    return ((weapon.location - tgt).length() / (weapon.projectileSpeed * leadingFactor)) + computeRemainingChargeUpTime(
        weapon
    )
}

fun computeRemainingChargeUpTime(weapon: WeaponAPI): Float {
    return max(weapon.spec.chargeTime, weapon.spec.beamChargeupTime) * (1f - weapon.chargeLevel)
}

fun getAverageArmor(armor: ArmorGridAPI): Float {
    val horizontalCells = armor.leftOf + armor.rightOf
    val verticalCells = armor.above + armor.below
    var sum = 0f
    for (i in 0 until horizontalCells) {
        for (j in 0 until verticalCells) {
            sum += armor.getArmorFraction(i, j)
        }
    }
    return sum * armor.maxArmorInCell
}

/**
 * effective ship radius measured in armor grid cells
 */
fun computeEffectiveCellRadius(ship: ShipAPI, impactAngleDeg: Float): Float {
    val angleOffset = abs(ship.facing - impactAngleDeg) * degToRad
    val radius = ship.spriteAPI.height * abs(cos(angleOffset)) + ship.spriteAPI.width * abs(sin(angleOffset))
    return radius / ship.armorGrid.cellSize
}

/**
 * ratio of inner (i.e. unreachable) armor grid cells to total cells from given direction
 */
fun computeInnerCellRatio(ship: ShipAPI, impactAngleDeg: Float): Float {
    val cellRadius = computeEffectiveCellRadius(ship, impactAngleDeg)
    val relevantArmorDepth = 3f
    if (cellRadius <= relevantArmorDepth) return 0f
    return (cellRadius - relevantArmorDepth).pow(2) / cellRadius.pow(2)
}

fun computeOuterLayerArmorInImpactArea(
    weapon: WeaponAPI,
    ship: CombatEntityAPI,
    predictedLocation: Vector2f? = null
): Float {
    if (ship !is ShipAPI) return 0f
    val location = predictedLocation ?: ship.location
    val arc = 10.0f
    val impactAngle = degFromVector(location - weapon.location) - 180f
    val armor = ship.getAverageArmorInSlice(impactAngle, arc)
    val icr = computeInnerCellRatio(ship, impactAngle)
    val maxArmor = ship.armorGrid.armorRating
    val minArmor = maxArmor * icr
    return max(0f, armor - minArmor) / (maxArmor - minArmor) * maxArmor
}

fun computeWeaponEffectivenessVsArmor(weapon: WeaponAPI, armor: Float): Float {
    var effectiveDmg = if (weapon.isBeam) weapon.damage.damage * 2.0f else weapon.damage.damage
    effectiveDmg *= when (weapon.damageType) {
        DamageType.FRAGMENTATION -> 0.25f
        DamageType.KINETIC -> 0.5f
        DamageType.HIGH_EXPLOSIVE -> 2.0f
        else -> 1.0f
    }
    return effectiveDmg / (effectiveDmg + armor)
}

fun getMaxArmor(armor: ArmorGridAPI): Float {
    val horizontalCells = armor.leftOf + armor.rightOf
    val verticalCells = armor.above + armor.below
    return horizontalCells * verticalCells * armor.maxArmorInCell
}

/**
 * returns the location where the shot is predicted to impact, transformed to be relative to the current
 * target position, under the assumption that the target is approximately circular
 * @param predictedLocation: location where the target will be at the time the shot is estimated to connect
 */
fun predictImpactLocationInTgtCoordinates(target: CombatEntityAPI, firingWeapon: WeaponAPI, predictedLocation: Vector2f? = null): Vector2f{
    val locationAtImpactTime = predictedLocation ?: target.location
    val positionDelta = locationAtImpactTime - target.location
    val vecToTarget = locationAtImpactTime - firingWeapon.location
    // vec from weapon to target bound
    vecToTarget.scale(1f - (target.collisionRadius / vecToTarget.length()))
    val predictedPointOnCollisionCircle = firingWeapon.location + vecToTarget - positionDelta
    return CollisionUtils.getCollisionPoint(predictedPointOnCollisionCircle, target.location, target) ?: predictedPointOnCollisionCircle
}

fun predictEffectiveArmorAtImpact(target: CombatEntityAPI, firingWeapon: WeaponAPI, predictedLocation: Vector2f? = null) : Float{
    val armor = (target as? ShipAPI)?.armorGrid ?: return 0f
    val impactLocation = predictImpactLocationInTgtCoordinates(target, firingWeapon, predictedLocation)
    val (x, y) = armor.getCellAtLocation(impactLocation) ?: return armor.armorRating
    return max(computeEffectiveArmorAroundIndex(armor, x, y), armor.armorRating * 0.05f)
}

fun computeEffectiveArmorAroundIndex(armor: ArmorGridAPI, x: Int, y: Int) : Float{
    fun getWeighted(x2: Int, y2: Int): Float{
        val a = armor.getArmorValue(x2, y2)
        val distance = (abs(x - x2) * abs(x - x2)) + (abs(y - y2) * abs(y - y2))
        return when{
            distance <= 2 -> a
            distance <= 4 -> 0.5f * a
            else -> 0f
        }
    }
    var toReturn = 0f
    for(x2 in x - 2 until x + 3){
        for(y2 in y - 2 until y + 3){
            toReturn += getWeighted(x2, y2)
        }
    }
    return toReturn
}
private fun isDefenseless(target: CombatEntityAPI, weapon: WeaponAPI): Boolean {
    if (target !is ShipAPI) return true
    if (target.shield == null && target.phaseCloak == null) return true
    target.fluxTracker?.let {
        if (it.isOverloadedOrVenting) {
            return max(it.overloadTimeRemaining, it.timeToVent) >=
                    ((target.location - weapon.location).length() / weapon.projectileSpeed)
        }
    }
    return false
}

fun isValidPDTargetForWeapon(target: CombatEntityAPI?, weapon: WeaponAPI): Boolean {
    if (weapon.hasAIHint(WeaponAPI.AIHints.IGNORES_FLARES) && (target as? MissileAPI)?.isFlare == true) {
        return false
    }
    return (target is MissileAPI || ((target as? ShipAPI)?.isFighter == true))
}

fun vectorFromAngleDeg(angle: Float): Vector2f {
    return Vector2f(cos(angle * degToRad), sin(angle * degToRad))
}

fun degFromVector(vec: Vector2f): Float {
    return atan2(vec.y, vec.x) / degToRad
}

fun mapBooleanToSpecificString(boolValue: Boolean, trueString: String, falseString: String): String {
    return if (boolValue) {
        trueString
    } else {
        falseString
    }
}

// Why doesn't Vector2f support this naturally? Note: infix and _ rather than operator in case this ever gets added
internal infix fun Vector2f.times_(d: Float): Vector2f {
    return Vector2f(d * x, d * y)
}

fun ShipAPI.determineUniversalShipTarget(): ShipAPI?{
    shipTarget?.let { return it }
    if(this.isStationModule){
        return this.parentStation?.determineUniversalShipTarget()
    }
    (aiFlags?.getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET) as? ShipAPI)?.let { return it }
    return null
}

fun WeaponAPI.sizeAsFloat(): Float{
    return when(size){
        WeaponAPI.WeaponSize.SMALL -> 1f
        WeaponAPI.WeaponSize.MEDIUM -> 2f
        WeaponAPI.WeaponSize.LARGE -> 4f
        null -> 0f
    }
}


/**
 * @return approximate angular distance of target from current weapon facing in rad
 * note: approximation works well for small values and is off by a factor of PI/2 for 180°
 * @param entity: Relative coordinates (velocity-compensated)
 */
fun angularDistanceFromWeapon(entity: Vector2f, weapon: WeaponAPI): Float {
    val weaponDirection = vectorFromAngleDeg(weapon.currAngle)
    val distance = entity - weapon.location
    val entityDirection = distance times_ (1f / distance.length())
    return (weaponDirection - entityDirection).length()
}

fun linearDistanceFromWeapon(entity: Vector2f, weapon: WeaponAPI): Float {
    return (weapon.location - entity).length()
}

/**
 * @param predictedEntityPosition In time-adjusted relative coordinates (i.e. where the ship will be when the shot arrives,
 *               adjusted for both the firing ships and target velocity, taking into consideration travel time)
 * @param collRadius Include any tolerances in here
 * @param aimPoint Point the weapon is aiming at, deduced from current weapon facing if not provided
 *
 * The calculation is done by finding the minimum point of function f(x)=distSquared(aimPoint * x, entity).
 * f(x) describes the distance between the target and the projectile along the projectile path.
 * We know f(x) is a positive quadratic function, so it has one minimum.
 * Then we simply check if the minimum distance is inside the entity collision radius.
 *
 */
fun determineIfShotWillHit(
    predictedEntityPosition: Vector2f,
    collRadius: Float,
    weapon: WeaponAPI,
    aimPoint: Vector2f? = null
): Boolean {
    val p = aimPoint?.minus(weapon.location) ?: vectorFromAngleDeg(weapon.currAngle)
    val e = predictedEntityPosition - weapon.location
    // Note: While this formula has been obtained by solving the equation mentioned in the description,
    // it can also be interpreted as the vector product (p_transposed * e), divided by the square of the length of p
    val minimum = (e.x * p.x + e.y * p.y) / (p.x * p.x + p.y * p.y)
    return minimum > 0 && MathUtils.getDistanceSquared(p.times_(minimum), e) < collRadius * collRadius
}

/**
 * this function is very similar to the other overload of determineIfShotWillHit, but uses exact bounds rather
 * than a collision radius
 * Will fall back to previously mentioned overload if target has no bounds
 */
fun determineIfShotWillHit(
    entity: CombatEntityAPI,
    predictedEntityPosition: Vector2f,
    fallbackCollRadius: Float,
    weapon: WeaponAPI,
    aimPoint: Vector2f? = null
): Boolean {
    val p = aimPoint?.minus(weapon.location) ?: vectorFromAngleDeg(weapon.currAngle).times_(5000f)

    val bounds = entity.exactBounds ?:
        return determineIfShotWillHit(predictedEntityPosition, fallbackCollRadius, weapon, aimPoint)

    bounds.update(predictedEntityPosition, entity.facing)
    val p1 = weapon.location
    val p2 = weapon.location + p
    return bounds.segments?.any { segment ->
        val e1 = segment.p1
        val e2 = segment.p2
        CollisionUtils.getCollisionPoint(p1, p2, e1, e2) != null
    } ?: false
}

fun determineIfShotWillHitBySetting(
    entity: CombatEntityAPI,
    predictedEntityPosition: Vector2f,
    collRadius: Float,
    weapon: WeaponAPI,
    aimPoint: Vector2f? = null,
    useBounds: Boolean = Settings.useExactBoundsForFiringDecision()
) : Boolean {
    return if(useBounds){
        determineIfShotWillHit(entity, predictedEntityPosition, collRadius, weapon, aimPoint)
    } else {
        determineIfShotWillHit(predictedEntityPosition, collRadius, weapon, aimPoint)
    }
}
fun effectiveCollRadius(entity: CombatEntityAPI): Float {
    return entity.collisionRadius * Settings.collisionRadiusMultiplier()
}