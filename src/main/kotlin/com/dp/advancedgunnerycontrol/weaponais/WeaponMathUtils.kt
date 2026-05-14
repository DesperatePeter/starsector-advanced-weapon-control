package com.dp.advancedgunnerycontrol.weaponais

import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

// Math.toRadians only works on doubles, which is annoying....
const val degToRad: Float = PI.toFloat() / 180f

fun Vector2f.normaliseNoThrow(): Vector2f{
    try {
        normalise()
    }catch (e: IllegalStateException){
        y = 1f
        x = 1f
    }
    return this
}

operator fun Vector2f.times(other: Vector2f): Float{
    return x * other.x + y * other.y
}

fun vectorFromAngleDeg(angle: Float): Vector2f {
    return Vector2f(cos(angle * degToRad), sin(angle * degToRad))
}

fun degFromVector(vec: Vector2f): Float {
    return atan2(vec.y, vec.x) / degToRad
}

fun normalizeAngleDeg(angle: Float): Float {
    return ((angle % 360f) + 360f) % 360f
}

fun shortestSignedAngleDeg(from: Float, to: Float): Float {
    return ((to - from + 540f) % 360f) - 180f
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