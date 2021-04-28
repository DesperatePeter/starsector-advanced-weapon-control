@file:Suppress("FunctionName")

package com.dp.advancedgunnerycontrol.weaponais

import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f

fun isPD(weapon: WeaponAPI) : Boolean {
    return weapon.hasAIHint(WeaponAPI.AIHints.PD_ALSO) || weapon.hasAIHint(WeaponAPI.AIHints.PD)
            || weapon.hasAIHint(WeaponAPI.AIHints.PD_ONLY)
}

fun isAimable(weapon: WeaponAPI) : Boolean {
    return !weapon.hasAIHint(WeaponAPI.AIHints.DO_NOT_AIM)
}

fun isInvalid(aiPlugin : AutofireAIPlugin) : Boolean {
    return (aiPlugin as? SpecificAIPluginBase)?.let {  !it.isValid() }
        ?: return false // if it's note one of my plugins it's safe to assume that it's valid (at least it's not my job)
}

fun isHostile(entity: CombatEntityAPI) : Boolean{
    return entity.owner == 1
}

// Why doesn't Vector2f support this naturally? Note: infix and _ rather than operator in case this ever gets added
internal infix fun Vector2f.times_(d: Float): Vector2f {
    return Vector2f(d*x, d*y)
}

class Fraction(){
    var numerator : Int = 0
    // Note: 0 is used as a magic number for "invalid". I know, that's ugly, but it's easy (proper solution: validity-bool and overwrite denominator setter)
    var denominator : Int = 0
    constructor(numerator: Int, denominator: Int) : this() {
        this.numerator = numerator
        this.denominator = denominator
    }
    fun asFloat() : Float{
        if (0 == denominator) return 0f // this class isn't important enough to risk Div0 exceptions
        return numerator.toFloat()/denominator.toFloat()
    }
    fun asBool() : Boolean{ // a denominator of 0 marks this as invalid
        return 0 == denominator
    }
    fun asString() : String{
        return "$numerator/$denominator"
    }
}