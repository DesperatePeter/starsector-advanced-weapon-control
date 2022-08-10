package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.isPD
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f

class NoPDTag (weapon: WeaponAPI) : WeaponAITagBase(weapon) {
    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean {
        return (entity as? ShipAPI)?.isFighter == false
    }

    override fun isValidTarget(entity: CombatEntityAPI): Boolean {
        return entity is ShipAPI
    }

    override fun computeTargetPriorityModifier(entity: CombatEntityAPI, predictedLocation: Vector2f): Float {
        val sh = entity as? ShipAPI ?: return 10000f
        return if (sh.isFighter) 2.5f else 1f
    }

    override fun shouldFire(entity: CombatEntityAPI, predictedLocation: Vector2f): Boolean = true

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false

    override fun isValid(): Boolean {
        return isPD(weapon) && super.isValid()
    }
}