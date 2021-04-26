package com.dp.advancedgunnerycontrol

import com.fs.starfarer.api.combat.*
import org.lazywizard.lazylib.combat.CombatUtils

import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f

// TODO: Common base class for fighter/missile plugin?
class AdvancedFighterAIPlugin(private var weapon: WeaponAPI) : AutofireAIPlugin {
    private var target: ShipAPI? = null
    private var forceOff = false
    override fun advance(p0: Float) {
        var potentialTargets = CombatUtils.getShipsWithinRange(weapon.location, weapon.range)
        potentialTargets = potentialTargets?.filter { it.isFighter && isWithinArc(it) }
        var it : MutableIterator<ShipAPI> = potentialTargets.iterator()
        var bestTarget : ShipAPI? = null
        var priority = Float.MAX_VALUE
        it.forEach {
            val currentPriority = computeTargetPriority(it)
            if(currentPriority < priority){
                priority = currentPriority
                bestTarget = it
            }
        }
        target = bestTarget
    }

    override fun shouldFire(): Boolean {
        return (target != null) && (getTarget() != null) && (!forceOff)
    }

    override fun forceOff() {
        forceOff = true
    }

    override fun getTarget(): Vector2f? {
        if (null != target) {
            val travelTime = (weapon.location - (target!!.location)).length() / weapon.projectileSpeed
            return target!!.location + Vector2f(target!!.velocity.x * travelTime, target!!.velocity.y * travelTime)
        }
        return null
    }

    override fun getTargetShip(): ShipAPI? {
        return target
    }

    override fun getWeapon(): WeaponAPI {
        return weapon
    }

    override fun getTargetMissile(): MissileAPI? {
        return null
    }

    private fun isWithinArc(entity: CombatEntityAPI): Boolean {
        return weapon.distanceFromArc(entity.location) <= 0.1
    }

    /**
     * @return a value dependent on distance and velocity of target. Lower is better
     * If this turns out to eat too much performance, picking a pseudo-random target might be better
     */
    private fun computeTargetPriority(entity: CombatEntityAPI): Float {
        val distance = (weapon.location - entity.location).length()
        var prelimResult = distance + entity.velocity.length()
        if ((weapon.type == WeaponAPI.WeaponType.BALLISTIC) && entity.shield.isOn) {
            return prelimResult * 0.5f
        }
        return prelimResult
    }

}