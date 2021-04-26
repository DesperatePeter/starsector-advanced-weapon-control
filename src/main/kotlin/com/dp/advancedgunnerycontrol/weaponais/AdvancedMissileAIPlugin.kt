package com.dp.advancedgunnerycontrol.weaponais

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.MissileAPI
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f

class AdvancedMissileAIPlugin (private var weapon: WeaponAPI) : AutofireAIPlugin {
    private var selectedTarget : MissileAPI? = null
    private var forceOff = false
    override fun advance(p0: Float) {
        var potentialTargets = CombatUtils.getMissilesWithinRange(weapon.location, weapon.range)
        potentialTargets = potentialTargets?.filter {isWithinArc(it)}
        var it : MutableIterator<MissileAPI> = potentialTargets.iterator()
        var bestTarget : MissileAPI? = null
        var priority = Float.MAX_VALUE
        it.forEach {
            val currentPriority = computeTargetPriority(it)
            if(currentPriority < priority){
                priority = currentPriority
                bestTarget = it
            }
        }
        selectedTarget = bestTarget
    }

    override fun shouldFire(): Boolean {
        return (selectedTarget != null) && (target != null) && (!forceOff)
    }

    override fun forceOff() {
        forceOff = true
    }

    override fun getTarget(): Vector2f? {
        if(null != selectedTarget) {
            val travelTime = (weapon.location - (selectedTarget!!.location )).length() / weapon.projectileSpeed
            return selectedTarget!!.location + Vector2f(selectedTarget!!.velocity.x * travelTime, selectedTarget!!.velocity.y *travelTime)
        }
        return null
    }

    override fun getTargetShip(): ShipAPI? {
        return null
    }

    override fun getWeapon(): WeaponAPI {
        return weapon
    }

    override fun getTargetMissile(): MissileAPI? {
        return selectedTarget
    }

    private fun isWithinArc(entity: CombatEntityAPI) : Boolean{
        return weapon.distanceFromArc(entity.location) <= 0.1
    }

    /**
     * @return a priority value, depending on distance, missile hp and missile damage
     *         lower value means higher priority. Will prioritize missiles that are close, have low hp and deal high dmg
     */
    private fun computeTargetPriority(missile: MissileAPI) : Float{
        var preliminaryPriority = (weapon.location - missile.location).length() // distance
        if(missile.isArmed) preliminaryPriority*=0.1f // prioritize armed missiles
        return if (missile.isGuided){ // prioritize unguided missiles
            preliminaryPriority / (missile.damage.damage + missile.empAmount*0.25f) * missile.hitpoints
        }else{
            preliminaryPriority / (missile.damage.damage + missile.empAmount*0.25f) * missile.hitpoints * 0.5f
        }
    }
}