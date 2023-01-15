package com.dp.advancedgunnerycontrol.utils

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.times_
import com.dp.advancedgunnerycontrol.weaponais.vectorFromAngleDeg
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.lazywizard.lazylib.ext.minus

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
 * @param entity: In relative coordinates
 * @param collRadius: Include any tolerances in here
 * @param aimPoint: Point the weapon is aiming at, deduced from current weapon facing if not provided
 */
fun determineIfShotWillHit(entity: Vector2f, collRadius: Float, weapon: WeaponAPI, aimPoint: Vector2f? = null) : Boolean{
    val p = aimPoint?.minus(weapon.location) ?: vectorFromAngleDeg(weapon.currAngle)
    val e = entity - weapon.location
    val x = (e.x * p.x + e.y * p.y) / (p.x * p.x + p.y * p.y)
    return x > 0 && MathUtils.getDistanceSquared(p.times_(x), e) < collRadius * collRadius
}

fun effectiveCollRadius(entity: CombatEntityAPI) : Float{
    return entity.collisionRadius * Settings.collisionRadiusMultiplier()
}