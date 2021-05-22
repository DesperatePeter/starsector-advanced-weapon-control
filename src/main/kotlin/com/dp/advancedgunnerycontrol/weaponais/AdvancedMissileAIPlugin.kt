package com.dp.advancedgunnerycontrol.weaponais

import com.dp.advancedgunnerycontrol.weaponais.suffixes.SuffixBase
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.MissileAPI
import org.lazywizard.lazylib.combat.CombatUtils
import org.lwjgl.util.vector.Vector2f

class AdvancedMissileAIPlugin(baseAI: AutofireAIPlugin, suffix: SuffixBase) : SpecificAIPluginBase(baseAI, suffix=suffix) {
    override fun computeTargetPriority(entity: CombatEntityAPI, predictedLocation: Vector2f): Float {
        val missile = (entity as? MissileAPI) ?: return Float.MAX_VALUE
        return computeBasePriority(entity, predictedLocation).let {
            it * (missile.hitpoints + 10.0f) / (missile.damageAmount + 50.0f) // prioritize high dmg low hp missiles
        }.let { // don't prioritize flares if weapon ignores flares
            if (weapon.hasAIHint(WeaponAPI.AIHints.IGNORES_FLARES) && missile.isFlare) {
                Float.MAX_VALUE
            } else {
                it
            }
        }
    }

    override fun getRelevantEntitiesWithinRange(): List<CombatEntityAPI> {
        return CombatUtils.getMissilesWithinRange(weapon.location, weapon.range + 200f).filterNotNull()
    }

    override fun isBaseAITargetValid(ship: ShipAPI?, missile: MissileAPI?): Boolean {
        return (null != missile)
    }

    override fun isBaseAIOverwritable(): Boolean = true

    override fun isValid(): Boolean {
        return isPD(weapon)
    }

}
