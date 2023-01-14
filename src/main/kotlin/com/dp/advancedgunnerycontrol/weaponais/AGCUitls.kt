@file:Suppress("FunctionName")

package com.dp.advancedgunnerycontrol.weaponais

import com.dp.advancedgunnerycontrol.WeaponControlPlugin
import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.*
// Math.toRadians only works on doubles, which is annoying....
const val degToRad: Float = PI.toFloat() / 180f

fun isPD(weapon: WeaponAPI): Boolean {
    if (weapon.hasAIHint(WeaponAPI.AIHints.PD_ALSO) || weapon.hasAIHint(WeaponAPI.AIHints.PD)
        || weapon.hasAIHint(WeaponAPI.AIHints.PD_ONLY)
    ) return true
    if ((WeaponControlPlugin.determineSelectedShip(Global.getCombatEngine())?.variant?.hasHullMod("pointdefenseai") == true)
        && (weapon.size == WeaponAPI.WeaponSize.SMALL) && (weapon.type != WeaponAPI.WeaponType.MISSILE)) return true
    return false
}

fun isAimable(weapon: WeaponAPI): Boolean {
    return weapon.spec?.trackingStr?.toLowerCase() in setOf(null, "", "none") &&
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

fun ammoLevel(weapon: WeaponAPI) : Float{
    if(!weapon.usesAmmo()) return 1.0f
    return weapon.ammo.toFloat() / weapon.maxAmmo.toFloat()
}

const val bignessFrigate = 0.5f
const val bignessDestroyer = 2f
const val bignessCruiser = 5f
const val bignessCapital = 20f
const val bignessFighter = 0.1f
fun isBig(ship: ShipAPI): Boolean {
    if(!Settings.strictBigSmall()) return true
    return bigness(ship) > bignessFrigate + 0.1f
}
fun isSmall(ship: ShipAPI): Boolean {
    if(!Settings.strictBigSmall()) return true
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
    return entity.owner == 1
}

fun getNeutralPosition(weapon: WeaponAPI) : Vector2f{
    return weapon.location + (vectorFromAngleDeg(weapon.ship.facing) times_ 100f)
}

fun isOpportuneTarget(tgt : CombatEntityAPI?, predictedLocation: Vector2f?, weapon: WeaponAPI) : Boolean{
    val target = tgt as? ShipAPI ?: return false
    val p = predictedLocation ?: return false
    if(!isOpportuneType(target, weapon)) return false
    var trackingFactor = when (weapon.spec?.trackingStr?.toLowerCase()){
        "none" -> 1.0f
        "very poor" -> 1.25f
        "poor" -> 1.5f
        "medium" -> 2.0f
        "special" -> 2.0f
        "good" -> 2.5f
        "excellent" -> 3.0f
        else -> 1.0f
    } * 0.2f * Settings.opportunistModifier()
    if(weapon.id?.contains("sabot") == true) trackingFactor*=3
    if(target.maxSpeed > weapon.projectileSpeed * trackingFactor) return false
    val ttt = (weapon.location - p).length() / weapon.projectileSpeed
    val ammoLessModifier = if(!weapon.usesAmmo()) 1.0f else if (weapon.ammoTracker.reloadSize > 0f) 0.5f else 0.1f
    if(((p - weapon.location).length() - target.collisionRadius * ammoLessModifier + ttt * target.maxSpeed * 0.1f / ammoLessModifier) >
        weapon.range * 0.95f * Settings.opportunistModifier()) return false
    return true
}

private fun isOpportuneType(target : ShipAPI, weapon: WeaponAPI) : Boolean {
    if(weapon.spec?.primaryRoleStr?.toLowerCase() == "finisher"){
        return isDefenseless(target, weapon)
    }
    if(weapon.damageType == DamageType.HIGH_EXPLOSIVE || weapon.damageType == DamageType.FRAGMENTATION){
        return computeShieldFactor(target, weapon) < Settings.opportunistHEThreshold()
    }
    if(weapon.damageType == DamageType.KINETIC){
        return computeShieldFactor(target, weapon) > Settings.opportunistKineticThreshold()
    }
    return true
}
/**
 * @return [0.01...~1.0] a small value if target is unshielded, has shields off or is at high flux
 */
fun computeShieldFactor(tgtShip: CombatEntityAPI, weapon: WeaponAPI, ttt: Float = 1f) : Float{
    if(tgtShip.shield == null || (tgtShip.shield.type != ShieldAPI.ShieldType.FRONT && tgtShip.shield.type != ShieldAPI.ShieldType.OMNI)){
        return 0.01f
    }
    if( isDefenseless(tgtShip, weapon)) return 0.01f
    return computeFluxBasedShieldFactor(tgtShip) * computeShieldFacingFactor(tgtShip, weapon, ttt)
}

fun computeFluxBasedShieldFactor(tgtShip: CombatEntityAPI) : Float{
    return (1.0f - ((tgtShip as? ShipAPI)?.fluxLevel ?: 1f)) * (if (tgtShip.shield?.isOn == true) 1.0f else 0.75f)
}

/**
 * @return a factor between 0.01f (shot will bypass shields) and 1f (shot will hit shields). A value in between if it's unclear
 */
fun computeShieldFacingFactor(tgtShip: CombatEntityAPI, weapon: WeaponAPI, ttt: Float) : Float{
    // Note: Angles in Starsector are always 0..360, 0 means east/right
    val shield = tgtShip.shield ?: return 0.01f
    if(shield.type == ShieldAPI.ShieldType.OMNI && shield.isOff){
        return 0.9f // Turned off omni shields means we don't know shit
    }
    val sCov = 0.5f * min(shield.arc, shield.activeArc + (ttt/shield.unfoldTime)*shield.arc)
    val flankingAngle =abs( 180f - abs(weapon.currAngle - shield.facing))
    return 1.0f - min(1.0f, max(0.01f, (flankingAngle - sCov)/15.0f)) // missing the shields by 15° or more means bypassing shot
}

fun computeTimeToTravel(weapon: WeaponAPI, tgt: Vector2f, leadingFactor: Float = 1f): Float {
    return ((weapon.location - tgt).length() / (weapon.projectileSpeed * leadingFactor)) + computeRemainingChargeUpTime(weapon)
}

fun computeRemainingChargeUpTime(weapon: WeaponAPI): Float{
    return max(weapon.spec.chargeTime, weapon.spec.beamChargeupTime) * (1f - weapon.chargeLevel)
}

fun getAverageArmor(armor: ArmorGridAPI) : Float{
    val horizontalCells = armor.leftOf + armor.rightOf
    val verticalCells = armor.above + armor.below
    var sum = 0f
    for (i in 0 until horizontalCells){
        for (j in 0 until verticalCells){
            sum += armor.getArmorFraction(i, j)
        }
    }
    return sum * armor.maxArmorInCell
}

/**
 * effective ship radius measured in armor grid cells
 */
fun computeEffectiveCellRadius(ship: ShipAPI, impactAngleDeg: Float) : Float{
    val angleOffset = abs(ship.facing - impactAngleDeg) * degToRad
    val radius = ship.spriteAPI.height * abs(cos(angleOffset)) + ship.spriteAPI.width * abs(sin(angleOffset))
    return radius / ship.armorGrid.cellSize
}

/**
 * ratio of inner (i.e. unreachable) armor grid cells to total cells from given direction
 */
fun computeInnerCellRatio(ship: ShipAPI, impactAngleDeg: Float) : Float{
    val cellRadius = computeEffectiveCellRadius(ship, impactAngleDeg)
    val relevantArmorDepth = 3f
    if(cellRadius <= relevantArmorDepth) return 0f
    return  (cellRadius - relevantArmorDepth).pow(2) / cellRadius.pow(2)
}

fun computeOuterLayerArmorInImpactArea(weapon: WeaponAPI, ship: CombatEntityAPI, predictedLocation: Vector2f? = null) : Float{
    if (ship !is ShipAPI) return 0f
    val location = predictedLocation ?: ship.location
    val arc = 10.0f
    val impactAngle = degFromVector(location - weapon.location) - 180f
    val armor = ship.getAverageArmorInSlice(impactAngle , arc)
    val icr = computeInnerCellRatio(ship, impactAngle)
    val maxArmor = ship.armorGrid.armorRating
    val minArmor = maxArmor*icr
    return max(0f, armor - minArmor) / (maxArmor - minArmor) * maxArmor
}

fun computeWeaponEffectivenessVsArmor(weapon: WeaponAPI, armor: Float) : Float{
    var effectiveDmg = if(weapon.isBeam) weapon.damage.damage * 2.0f else weapon.damage.damage
    effectiveDmg *= when (weapon.damageType) {
        DamageType.FRAGMENTATION -> 0.25f
        DamageType.KINETIC -> 0.5f
        DamageType.HIGH_EXPLOSIVE -> 2.0f
        else -> 1.0f
    }
    return effectiveDmg / (effectiveDmg + armor)
}

fun getMaxArmor(armor: ArmorGridAPI) : Float{
    val horizontalCells = armor.leftOf + armor.rightOf
    val verticalCells = armor.above + armor.below
    return horizontalCells * verticalCells * armor.maxArmorInCell
}

private fun isDefenseless(target : CombatEntityAPI, weapon: WeaponAPI) : Boolean {
    if(target !is ShipAPI) return true
    if(target.shield == null && target.phaseCloak == null) return true
    target.fluxTracker?.let {
        if(it.isOverloadedOrVenting){
            return max(it.overloadTimeRemaining, it.timeToVent) >=
                    ((target.location - weapon.location).length()/weapon.projectileSpeed)
        }
    }
    return false
}

fun isValidPDTargetForWeapon(target: CombatEntityAPI?, weapon: WeaponAPI) : Boolean {
    if(weapon.hasAIHint(WeaponAPI.AIHints.IGNORES_FLARES) && (target as? MissileAPI)?.isFlare == true){
        return false
    }
    return (target is MissileAPI || ((target as? ShipAPI)?.isFighter == true))
}

fun vectorFromAngleDeg(angle: Float): Vector2f {
    return Vector2f(cos(angle * degToRad), sin(angle * degToRad))
}

fun degFromVector(vec: Vector2f) : Float {
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
