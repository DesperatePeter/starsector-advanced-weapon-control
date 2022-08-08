package com.dp.advancedgunnerycontrol.weaponais.test.mocks

import com.dp.advancedgunnerycontrol.weaponais.SpecificAIPluginBase
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import org.lazywizard.lazylib.combat.CombatUtils
import org.lwjgl.util.vector.Vector2f

class SpecificAIPluginBaseMock (baseAI : WeaponBaseAIMock): SpecificAIPluginBase(baseAI) {
    var _enemies: List<CombatEntityAPI> = emptyList()
    var _friendlies: List<ShipAPI> = emptyList()
    override fun computeTargetPriority(entity: CombatEntityAPI, predictedLocation: Vector2f): Float {
        return 1f
    }

    override fun getRelevantEntitiesWithinRange(): List<CombatEntityAPI> {
        return _enemies
    }

    override fun isBaseAITargetValid(ship: ShipAPI?, missile: MissileAPI?): Boolean = false

    override fun isBaseAIOverwritable(): Boolean = true

    override fun isValid(): Boolean = true

    override fun getFriendlies(): List<Pair<CombatEntityAPI, Vector2f>> {
        return addPredictedLocationToTargets(
            _friendlies.filter { it != weapon.ship }.filter {
                (it.isAlly || (it.owner == 0) || (it.owner == 100 && shouldConsiderNeutralsAsFriendlies())) && !it.isFighter
            }).filter { isInRange(it.second, it.first.collisionRadius) && isWithinArc(it.second, it.first.collisionRadius) }
    }
}