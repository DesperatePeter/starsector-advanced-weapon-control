package com.dp.advancedgunnerycontrol.weaponais

import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.combat.ArmorGridAPI
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lazywizard.lazylib.CollisionUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

fun isOpportuneTarget(
    solution: FiringSolution?,
    weapon: WeaponAPI,
    kineticThreshold: Float = Settings.opportunistKineticThreshold(),
    highExplosiveThreshold: Float = Settings.opportunistHEThreshold(),
    triggerHappinessModifier: Float = Settings.opportunistModifier(),
): Boolean {
    val target = solution?.target as? ShipAPI ?: return false
    val p = solution.aimPoint
    if (!isOpportuneType(target, weapon, kineticThreshold, highExplosiveThreshold)) return false
    var trackingFactor = when (weapon.spec?.trackingStr?.lowercase(Locale.getDefault())) {
        "none" -> 1.0f
        "very poor" -> 1.25f
        "poor" -> 1.5f
        "medium" -> 2.0f
        "special" -> 2.0f
        "good" -> 2.5f
        "excellent" -> 3.0f
        else -> 1.0f
    } * 0.2f * triggerHappinessModifier
    if (weapon.id?.contains("sabot") == true) trackingFactor *= 3
    if (target.maxSpeed > weapon.projectileSpeed * trackingFactor) return false
    val ttt = (weapon.location - p).length() / weapon.projectileSpeed
    val ammoLessModifier = if (!weapon.usesAmmo()) 1.0f else if (weapon.ammoTracker.reloadSize > 0f) 0.5f else 0.1f
    return ((p - weapon.location).length() - effectiveCollRadius(target) * ammoLessModifier + ttt * target.maxSpeed * 0.1f / ammoLessModifier) <= weapon.range * 0.95f * triggerHappinessModifier
}

private fun isOpportuneType(
    target: ShipAPI,
    weapon: WeaponAPI,
    kineticThreshold: Float,
    highExplosiveThreshold: Float,
): Boolean {
    if (weapon.spec?.primaryRoleStr?.lowercase(Locale.getDefault()) == "finisher") {
        return isDefenseless(target, weapon)
    }
    if (weapon.damageType == DamageType.HIGH_EXPLOSIVE || weapon.damageType == DamageType.FRAGMENTATION) {
        return computeShieldFactor(target, weapon) < highExplosiveThreshold
    }
    if (weapon.damageType == DamageType.KINETIC) {
        return computeShieldFactor(target, weapon) > kineticThreshold
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
    fun getWeighted(cellX: Int, cellY: Int): Float{
        val armorValue = armor.getArmorValue(cellX, cellY)
        val distance = (abs(x - cellX) * abs(x - cellX)) + (abs(y - cellY) * abs(y - cellY))
        return when{
            distance <= 2 -> armorValue
            distance <= 4 -> 0.5f * armorValue
            else -> 0f
        }
    }
    var effectiveArmor = 0f
    for(cellX in x - 2 until x + 3){
        for(cellY in y - 2 until y + 3){
            effectiveArmor += getWeighted(cellX, cellY)
        }
    }
    return effectiveArmor
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