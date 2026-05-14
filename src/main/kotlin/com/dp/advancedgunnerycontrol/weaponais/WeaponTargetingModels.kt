package com.dp.advancedgunnerycontrol.weaponais

import com.fs.starfarer.api.combat.CombatEntityAPI
import org.lwjgl.util.vector.Vector2f

data class FiringSolution(
    val target: CombatEntityAPI,

    // Point at which the weapon should aim to hit
    // the target dead center, under the assumption
    // that target velocity is constant.
    val aimPoint: Vector2f
)