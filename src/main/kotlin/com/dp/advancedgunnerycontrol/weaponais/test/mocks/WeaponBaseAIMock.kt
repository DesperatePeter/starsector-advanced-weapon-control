@file:Suppress("PropertyName")

package com.dp.advancedgunnerycontrol.weaponais.test.mocks

import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f

class WeaponBaseAIMock(private val _weapon: WeaponAPI) : AutofireAIPlugin {
    var _shouldFire = false
    var _targetPoint = Vector2f(0f, 0f)
    var _targetShip : ShipAPI? = null
    set(value) {field = value; _targetMissile = null}
    var _targetMissile : MissileAPI? = null
    set(value) {field = value; _targetShip = null}

    override fun advance(p0: Float) {
    }

    override fun shouldFire(): Boolean {
        return _shouldFire
    }

    override fun forceOff() {
    }

    override fun getTarget(): Vector2f {
        return _targetPoint
    }

    override fun getTargetShip(): ShipAPI? {
        return _targetShip
    }

    override fun getWeapon(): WeaponAPI {
        return _weapon
    }

    override fun getTargetMissile(): MissileAPI? {
        return _targetMissile
    }
}