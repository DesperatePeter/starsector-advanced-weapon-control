@file:Suppress("FunctionName")

package com.dp.advancedgunnerycontrol.weaponais

import com.dp.advancedgunnerycontrol.Settings
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Math.toRadians only works on doubles, which is annoying as f***
const val degToRad: Float = PI.toFloat() / 180f

fun isPD(weapon: WeaponAPI): Boolean {
    return weapon.hasAIHint(WeaponAPI.AIHints.PD_ALSO) || weapon.hasAIHint(WeaponAPI.AIHints.PD)
            || weapon.hasAIHint(WeaponAPI.AIHints.PD_ONLY)
}

fun isAimable(weapon: WeaponAPI): Boolean {
    return !weapon.hasAIHint(WeaponAPI.AIHints.DO_NOT_AIM)
}

fun isInvalid(aiPlugin: AutofireAIPlugin): Boolean {
    (aiPlugin as? SpecificAIPluginBase)?.let {
        if (it.weapon.id in Settings.weaponBlacklist) return true
        return !it.isValid()
    }
    // if it's note one of my plugins it's safe to assume that it's valid (at least it's not my job)
    return false
}

fun bigness(ship: ShipAPI?) : Float{
    return when(ship?.hullSize){
        ShipAPI.HullSize.FRIGATE -> 0.5f
        ShipAPI.HullSize.DESTROYER -> 2f
        ShipAPI.HullSize.CRUISER -> 5f
        ShipAPI.HullSize.CAPITAL_SHIP -> 20f
        else -> 0.1f
    }
}

fun isHostile(entity: CombatEntityAPI): Boolean {
    return entity.owner == 1
}

fun vectorFromAngleDeg(degs: Float): Vector2f {
    return Vector2f(cos(degs * degToRad), sin(degs * degToRad))
}

// Why doesn't Vector2f support this naturally? Note: infix and _ rather than operator in case this ever gets added
internal infix fun Vector2f.times_(d: Float): Vector2f {
    return Vector2f(d * x, d * y)
}

class Fraction() {
    var numerator: Int = 0

    // Note: 0 is used as a magic number for "invalid". I know, that's ugly, but it's easy (proper solution: validity-bool and overwrite denominator setter)
    var denominator: Int = 0

    constructor(numerator: Int, denominator: Int) : this() {
        this.numerator = numerator
        this.denominator = denominator
    }

    fun asFloat(): Float {
        if (0 == denominator) return 0f // this class isn't important enough to risk Div0 exceptions
        return numerator.toFloat() / denominator.toFloat()
    }

    fun asBool(): Boolean { // a denominator of 0 marks this as invalid
        return 0 == denominator
    }

    fun asString(): String {
        return "$numerator/$denominator"
    }
}