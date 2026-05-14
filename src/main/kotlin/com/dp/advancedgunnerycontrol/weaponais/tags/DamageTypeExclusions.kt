package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.WeaponAPI

data class DamageTypeExclusions(
    val kinetic: Boolean = false,
    val highExplosive: Boolean = false,
    val fragmentation: Boolean = false,
    val energy: Boolean = false,
    val beam: Boolean = false,
    val missile: Boolean = false,
    val projectile: Boolean = false,
) {
    fun ignores(weapon: WeaponAPI): Boolean {
        val active = activeForCurrentSettings()
        val isBeamWeapon = weapon.isBeam || weapon.isBurstBeam
        val isMissileWeapon = weapon.type == WeaponAPI.WeaponType.MISSILE
        if (active.beam && isBeamWeapon) return true
        if (active.missile && isMissileWeapon) return true
        if (active.projectile && !isBeamWeapon && !isMissileWeapon) return true
        return when (weapon.damageType) {
            DamageType.KINETIC -> active.kinetic
            DamageType.HIGH_EXPLOSIVE -> active.highExplosive
            DamageType.FRAGMENTATION -> active.fragmentation
            DamageType.ENERGY -> active.energy
            else -> false
        }
    }

    fun suffix(): String {
        val active = activeForCurrentSettings()
        if (active == NONE) return ""
        val tokens = mutableListOf<String>()
        if (active.kinetic) tokens += KINETIC_TOKEN
        if (active.highExplosive) tokens += HIGH_EXPLOSIVE_TOKEN
        if (active.fragmentation) tokens += FRAGMENTATION_TOKEN
        if (active.energy) tokens += ENERGY_TOKEN
        if (active.beam) tokens += BEAM_TOKEN
        if (active.missile) tokens += MISSILE_TOKEN
        if (active.projectile) tokens += PROJECTILE_TOKEN
        return ",Ignore<${tokens.joinToString(",")}>"
    }

    fun activeForCurrentSettings(): DamageTypeExclusions = copy(
        energy = energy && Settings.showEnergyDamageTypeExclusionOption(),
        missile = missile && Settings.showMissileDamageTypeExclusionOption(),
        projectile = projectile && Settings.showProjectileDamageTypeExclusionOption(),
    )

    companion object {
        const val KINETIC_TOKEN = "K"
        const val HIGH_EXPLOSIVE_TOKEN = "HE"
        const val FRAGMENTATION_TOKEN = "F"
        const val ENERGY_TOKEN = "E"
        const val BEAM_TOKEN = "B"
        const val MISSILE_TOKEN = "M"
        const val PROJECTILE_TOKEN = "P"

        val NONE = DamageTypeExclusions()

        fun fromTokens(rawTokens: String?): DamageTypeExclusions? {
            val tokens = rawTokens
                ?.takeIf { it.isNotBlank() }
                ?.split(',')
                ?.map { it.trim().uppercase() }
                ?.filter { it.isNotBlank() }
                ?: return NONE
            var kinetic = false
            var highExplosive = false
            var fragmentation = false
            var energy = false
            var beam = false
            var missile = false
            var projectile = false
            for (token in tokens) {
                when (token) {
                    KINETIC_TOKEN -> kinetic = true
                    HIGH_EXPLOSIVE_TOKEN -> highExplosive = true
                    FRAGMENTATION_TOKEN -> fragmentation = true
                    ENERGY_TOKEN -> energy = true
                    BEAM_TOKEN -> beam = true
                    MISSILE_TOKEN -> missile = true
                    PROJECTILE_TOKEN -> projectile = true
                    else -> return null
                }
            }
            return DamageTypeExclusions(kinetic, highExplosive, fragmentation, energy, beam, missile, projectile)
        }
    }
}
