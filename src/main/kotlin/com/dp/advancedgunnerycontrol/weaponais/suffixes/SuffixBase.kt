package com.dp.advancedgunnerycontrol.weaponais.suffixes

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import kotlin.math.pow

open class SuffixBase(protected val weapon: WeaponAPI) {
    open fun shouldFire(baseDecision : Boolean, target: CombatEntityAPI? = null) : Boolean = baseDecision
    open fun modifyPriority(target: CombatEntityAPI?) : Float = 1.0f

    protected fun ammoLevel() : Float{
        if(!weapon.usesAmmo()) return 1.0f
        return weapon.ammo.toFloat() / weapon.maxAmmo.toFloat()
    }

    companion object{
        /**
         * @return a small value if target is unshielded, has shields off or is at high flux
         */
        fun computeShieldFactor(tgtShip: ShipAPI) : Float{ // todo facing
            if(tgtShip.shield == null) return 0.01f
            if(tgtShip.shield?.isOff == true) return 0.5f/(tgtShip.fluxLevel.pow(2) + 0.01f)
            if(tgtShip.shield?.isOn == true) return 1f/(tgtShip.fluxLevel.pow(2) + 0.01f)
            return 1.0f
        }
    }
}