package com.dp.advancedgunnerycontrol.weaponais

import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lazywizard.lazylib.CollisionUtils
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

fun WeaponAPI.isInRangeOf(point: Vector2f, threshold: Float = 1f): Boolean {
    return (location - point).length() <= threshold * range
}

fun WeaponAPI.coerceAimPointIntoArc(point: Vector2f): Vector2f {
    if (!isAimable(this) || arc >= 359.9f) return point

    val distance = linearDistanceFromWeapon(point, this)
    if (distance <= 0.01f) return point

    val arcCenter = normalizeAngleDeg((ship?.facing ?: 0f) + arcFacing)
    val aimAngle = degFromVector(point - location)
    val offsetFromArcCenter = shortestSignedAngleDeg(arcCenter, aimAngle)
    val halfArc = arc * 0.5f
    if (abs(offsetFromArcCenter) <= halfArc) return point

    val clampedAngle = arcCenter + offsetFromArcCenter.coerceIn(-halfArc, halfArc)
    return location + (vectorFromAngleDeg(clampedAngle) times_ distance)
}

/**
 * compute angular width of given entity that lies within given cone
 * @return an approximated angular width in rad
 */
fun computeWeaponConeExposureRad(weaponLoc: Vector2f, aimPoint: Vector2f, spreadDeg: Float, targetLoc: Vector2f, collRadius: Float): Float{
    // all values in this code are normalized (e.g. widths are divided by distance)
//    val dist = (weaponLoc - targetLoc).length() + 0.1f
//    val targetWidth = collRadius / dist
//    val weaponFacing = aimPoint - weaponLoc
//    weaponFacing.normaliseNoThrow()
//    val halfSpread = spreadDeg * degToRad / 2f
//    val lateralOffsetRad = abs(angularDistanceFromLine(targetLoc, weaponLoc, weaponFacing))
//    val widthOutsideCone = (lateralOffsetRad + targetWidth / 2f - halfSpread).coerceIn(0f, targetWidth)
//    return minOf(targetWidth, targetWidth - widthOutsideCone, halfSpread * 2f)
    return PolarEntityInWeaponCone(weaponLoc, aimPoint, spreadDeg, targetLoc, collRadius).getUnobstructedLength()
}

fun computeWeaponConeExposureRadWithEclipsingEntity(weaponLoc: Vector2f, aimPoint: Vector2f, spreadDeg: Float,
                                                    entityLoc: Vector2f, collRadius: Float, eclipsingLoc: Vector2f, eclipsingRadius: Float): Float{
    val entity = PolarEntityInWeaponCone(weaponLoc, aimPoint, spreadDeg, entityLoc, collRadius)
    val eclipsingEntity  = PolarEntityInWeaponCone(weaponLoc, aimPoint, spreadDeg, eclipsingLoc, eclipsingRadius)
    entity.obstructWith(eclipsingEntity)
    return entity.getUnobstructedLength()
}

data class PositionWithRadius(val location: Vector2f, val radius: Float)

fun computeWeaponConeExposureForAllies(weaponLoc: Vector2f, aimPoint: Vector2f, spreadDeg: Float,
                                     alliedObjects: List<PositionWithRadius>, otherObjects: List<PositionWithRadius>) : Float{
    val allies = alliedObjects.map { PolarEntityInWeaponCone(weaponLoc, aimPoint, spreadDeg, it.location, it.radius) }.filter { it.isValid() }
    val others = otherObjects.map { PolarEntityInWeaponCone(weaponLoc, aimPoint, spreadDeg, it.location, it.radius)  }.filter { it.isValid() }
    allies.forEach { ally ->
        others.forEach{ other ->
            ally.obstructWith(other)
        }
    }
    return allies.map { it.getUnobstructedLength() }.sum()
}

typealias FPair = Pair<Float, Float>

data class Segment(var distance: Float, var extent: FPair)

/**
 * representation of an entity (or entities) in polar coordinates, relative to given line
 * this is used to represent entities in a weapon cone
 * a coordinate of 0 means perfectly aligned with weapon facing
 *
 */
class PolarEntityInWeaponCone(weaponLoc: Vector2f, aimPoint: Vector2f, spreadDeg: Float, targetLoc: Vector2f, radius: Float){
    companion object{

        enum class OverlapType{COMPLETE_BLOCK, NO_OVERLAP, PARTIAL_LEFT, PARTIAL_RIGHT, CENTER, ERROR}

        fun obstructionType(entity: FPair, obstruction: FPair): OverlapType{
            return when{
                obstruction.first > obstruction.second || entity.first > entity.second -> OverlapType.ERROR
                // obstruction completely blocks
                // e      |-------|
                // o   |-------------|
                obstruction.first <= entity.first && obstruction.second >= entity.second -> OverlapType.COMPLETE_BLOCK
                // no overlap
                // e |-------|
                // o           |--------|
                obstruction.first >= entity.second -> OverlapType.NO_OVERLAP
                // no overlap
                // e            |-------|
                // o |--------|
                obstruction.second <= entity.first -> OverlapType.NO_OVERLAP
                // partial overlap
                // e |-------|
                // o      |--------|
                entity.first <= obstruction.first && entity.second >= obstruction.first && obstruction.second >= entity.second -> OverlapType.PARTIAL_RIGHT
                // partial overlap
                // e     |-------|
                // o |--------|
                obstruction.first <= entity.first && obstruction.second >= entity.first && entity.second >= obstruction.second -> OverlapType.PARTIAL_LEFT
                // obstruction in middle
                // e |----------------|
                // o    |--------|
                else -> OverlapType.CENTER
            }
        }
        fun computeExtendInCone(weaponLoc: Vector2f, aimPoint: Vector2f, spreadDeg: Float, targetLoc: Vector2f, radius: Float): Segment{
            val dist = (weaponLoc - targetLoc).length() + 0.01f
            val targetFacing = (targetLoc - weaponLoc).times_(1f / dist)
            val weaponFacing = (aimPoint - weaponLoc).normaliseNoThrow()
            val width = radius / dist
            val xDir = Vector2f(weaponFacing.y, -weaponFacing.x) // arbitrary direction orthogonal to weapon facing
            val offset = targetFacing * xDir
            val halfSpread = spreadDeg * degToRad / 2f
            val left = (offset - width/2f).coerceIn(-halfSpread, halfSpread)
            val right = (offset + width/2f).coerceIn(-halfSpread, halfSpread)
            return Segment(dist, Pair(left, right))
        }

        fun obstruct(entity: Segment, obstruction: Segment): List<Segment>{
            if(obstruction.distance > entity.distance) return listOf(entity)
            return when(obstructionType(entity.extent, obstruction.extent)){
                OverlapType.COMPLETE_BLOCK-> emptyList()
                OverlapType.NO_OVERLAP -> listOf(entity)
                OverlapType.PARTIAL_RIGHT -> listOf(Segment(entity.distance, FPair(entity.extent.first, obstruction.extent.first)))
                OverlapType.PARTIAL_LEFT -> listOf(Segment(entity.distance, FPair(obstruction.extent.second, entity.extent.second))) // listOf(FPair(obstruction.second, entity.second))
                OverlapType.CENTER -> listOf(
                    Segment(entity.distance, FPair(entity.extent.first, obstruction.extent.first)),
                    Segment(entity.distance, FPair(obstruction.extent.second, entity.extent.second))
                )
                OverlapType.ERROR -> {
                    // Global.getLogger(this::class.java).error("Invalid Overlap type")
                    // throw IllegalStateException("Polar Entity with invalid edges")
                    emptyList()
                }
            }
        }
    }

    fun isValid(): Boolean{
        return originalSegment.extent.second - originalSegment.extent.first > 0f
    }

    // starting and ending point in polar coordinates that is within cone

    private val originalSegment = computeExtendInCone(weaponLoc, aimPoint, spreadDeg, targetLoc, radius)
    private var segments = listOf(originalSegment)

    fun obstructWith(obstruction: PolarEntityInWeaponCone){
        val newExtent = mutableListOf<Segment>()
        segments.forEach {
            newExtent.addAll(obstruct(it, obstruction.originalSegment))
        }
        segments = newExtent
    }

    fun getUnobstructedLength(): Float{
        val unobstructedLength = segments.map { it.extent.second - it.extent.first }.sum()
        return unobstructedLength
    }

}

fun getNeutralPosition(weapon: WeaponAPI): Vector2f {
    return weapon.location + (vectorFromAngleDeg(weapon.ship.facing) times_ 100f)
}

fun computeTimeToTravel(weapon: WeaponAPI, targetPoint: Vector2f, leadingFactor: Float = 1f): Float {
    return ((weapon.location - targetPoint).length() / (weapon.projectileSpeed * leadingFactor)) + computeRemainingChargeUpTime(
        weapon
    )
}

fun computeRemainingChargeUpTime(weapon: WeaponAPI): Float {
    return max(weapon.spec.chargeTime, weapon.spec.beamChargeupTime) * (1f - weapon.chargeLevel)
}

/**
 * @return approximate angular distance of target from current weapon facing in rad
 * note: approximation works well for small values and is off by a factor of PI/2 for 180°
 * @param entity: Relative coordinates (velocity-compensated)
 */
fun angularDistanceFromWeapon(entity: Vector2f, weapon: WeaponAPI): Float {
    val weaponDirection = vectorFromAngleDeg(weapon.currAngle)
    return angularDistanceFromLine(entity, weapon.location, weaponDirection)
}

/**
 * @return approximate angular distance of target from given line in rad
 * note: approximation works well for small values and is off by a factor of PI/2 for 180°
 * @param lineDirection: normalized direction vector (use vectorFromAngleDeg)
 */
fun angularDistanceFromLine(entity: Vector2f, lineOrigin: Vector2f, lineDirection: Vector2f): Float{
    val entityDirection = (entity - lineOrigin).normaliseNoThrow()
    return (entityDirection - lineDirection).length()
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
    val shotVector = aimPoint?.minus(weapon.location) ?: vectorFromAngleDeg(weapon.currAngle).times_(5000f)

    val bounds = entity.exactBounds ?:
        return determineIfShotWillHit(predictedEntityPosition, fallbackCollRadius, weapon, aimPoint)

    bounds.update(predictedEntityPosition, entity.facing)
    val shotStart = weapon.location
    val shotEnd = weapon.location + shotVector
    return bounds.segments?.any { segment ->
        val segmentStart = segment.p1
        val segmentEnd = segment.p2
        CollisionUtils.getCollisionPoint(shotStart, shotEnd, segmentStart, segmentEnd) != null
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