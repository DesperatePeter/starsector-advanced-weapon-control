package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import kotlin.math.max

class NoPDWasteTag(
    weapon: WeaponAPI,
    private val wasteThreshold: Float,
    private val cleanupDamageCapOverride: Float? = null,
) : WeaponAITagBase(weapon) {
    companion object {
        private const val BEAM_PACKET_WINDOW_SECONDS = 0.5f
        private const val EPSILON = 0.0001f
    }

    override fun isValidTarget(entity: CombatEntityAPI): Boolean {
        if (entity is MissileAPI) {
            return passesWasteCheck(entity.hitpoints)
        }
        if (entity is ShipAPI && entity.isFighter) {
            return passesWasteCheck(estimateFighterEffectiveDurabilityForWeapon(weapon, entity))
        }
        return true
    }

    private fun passesWasteCheck(targetDamageRequired: Float): Boolean {
        val attackPacketDamage = estimateAttackPacketDamage()
        if (attackPacketDamage <= cleanupDamageCap()) {
            return true
        }
        if (attackPacketDamage <= EPSILON) {
            return true
        }
        val boundedRequired = targetDamageRequired.coerceAtLeast(0f)
        val wasteRatio = max(0f, attackPacketDamage - boundedRequired) / attackPacketDamage
        return wasteRatio <= wasteThreshold
    }

    private fun cleanupDamageCap(): Float = cleanupDamageCapOverride ?: Settings.noPDWasteCleanupDamageCap()

    private fun estimateAttackPacketDamage(): Float {
        val baseDamage = weapon.damage.damage.coerceAtLeast(0f)
        val packetDamage = when {
            weapon.isBurstBeam -> {
                val spec = weapon.spec
                val burstDuration = spec?.burstDuration ?: 0f
                val beamChargeupTime = spec?.beamChargeupTime ?: 0f
                val beamChargedownTime = spec?.beamChargedownTime ?: 0f
                baseDamage * (burstDuration + (beamChargeupTime / 3f) + (beamChargedownTime / 3f))
            }
            weapon.isBeam -> {
                baseDamage * BEAM_PACKET_WINDOW_SECONDS
            }
            else -> {
                baseDamage * (weapon.spec?.burstSize ?: 1).coerceAtLeast(1).toFloat()
            }
        }
        return packetDamage.coerceAtLeast(0f)
    }

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean = isValidTarget(entity)

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float = 1f

    override fun shouldFire(solution: FiringSolution): Boolean = true

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false
}
