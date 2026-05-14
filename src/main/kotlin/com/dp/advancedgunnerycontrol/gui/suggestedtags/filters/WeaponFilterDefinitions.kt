package com.dp.advancedgunnerycontrol.gui.suggestedtags.filters

import com.dp.advancedgunnerycontrol.gui.suggestedtags.filters.WeaponFilter.FilterType
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.loading.WeaponSpecAPI

internal object WeaponFilterCatalog {
    val allFilters = listOf(
        BallisticMountFilter,
        EnergyMountFilter,
        MissileMountFilter,
        HybridMountFilter,
        SynergyMountFilter,
        CompositeMountFilter,
        SmallWeaponFilter,
        MediumWeaponFilter,
        LargeWeaponFilter,
        BeamWeaponFilter,
        ProjectileWeaponFilter,
        ExplosiveDamageFilter,
        KineticDamageFilter,
        FragmentationDamageFilter,
        EnergyDamageFilter,
        AmmoOrChargesFilter,
        NoAmmoOrChargesFilter,
        EmpFilter,
        NoEmpFilter,
        PointDefenseFilter,
        NoPointDefenseFilter,
        FluxDamage0To05Filter,
        FluxDamage05To075Filter,
        FluxDamage075To10Filter,
        FluxDamage10To125Filter,
        FluxDamage125To15Filter,
        FluxDamage15PlusFilter,
        Range0To500Filter,
        Range500To1000Filter,
        Range1000To1500Filter,
        Range1500To2000Filter,
        Range2000To2500Filter,
        Range2500PlusFilter,
    )

    private object BallisticMountFilter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = weaponSpec.mountType == WeaponAPI.WeaponType.BALLISTIC
        override fun type(): FilterType = FilterType.MOUNT_TYPE
        override fun name(): String = "Ballistic Mount"
    }

    private object EnergyMountFilter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = weaponSpec.mountType == WeaponAPI.WeaponType.ENERGY
        override fun type(): FilterType = FilterType.MOUNT_TYPE
        override fun name(): String = "Energy Mount"
    }

    private object MissileMountFilter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = weaponSpec.mountType == WeaponAPI.WeaponType.MISSILE
        override fun type(): FilterType = FilterType.MOUNT_TYPE
        override fun name(): String = "Missile Mount"
    }

    private object HybridMountFilter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = weaponSpec.mountType == WeaponAPI.WeaponType.HYBRID
        override fun type(): FilterType = FilterType.MOUNT_TYPE
        override fun name(): String = "Hybrid Mount"
    }

    private object SynergyMountFilter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = weaponSpec.mountType == WeaponAPI.WeaponType.SYNERGY
        override fun type(): FilterType = FilterType.MOUNT_TYPE
        override fun name(): String = "Synergy Mount"
    }

    private object CompositeMountFilter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = weaponSpec.mountType == WeaponAPI.WeaponType.COMPOSITE
        override fun type(): FilterType = FilterType.MOUNT_TYPE
        override fun name(): String = "Composite Mount"
    }

    private object SmallWeaponFilter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = weaponSpec.size == WeaponAPI.WeaponSize.SMALL
        override fun type(): FilterType = FilterType.SIZE
        override fun name(): String = "Small"
    }

    private object MediumWeaponFilter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = weaponSpec.size == WeaponAPI.WeaponSize.MEDIUM
        override fun type(): FilterType = FilterType.SIZE
        override fun name(): String = "Medium"
    }

    private object LargeWeaponFilter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = weaponSpec.size == WeaponAPI.WeaponSize.LARGE
        override fun type(): FilterType = FilterType.SIZE
        override fun name(): String = "Large"
    }

    private object BeamWeaponFilter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = weaponSpec.isBeam
        override fun type(): FilterType = FilterType.FIRE_FORM
        override fun name(): String = "Beam"
    }

    private object ProjectileWeaponFilter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = !weaponSpec.isBeam
        override fun type(): FilterType = FilterType.FIRE_FORM
        override fun name(): String = "Projectile"
    }

    private object Range0To500Filter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = rangeIn(weaponSpec, 0f, 500f)
        override fun type(): FilterType = FilterType.RANGE
        override fun name(): String = "Range [0, 500]"
    }

    private object Range500To1000Filter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = rangeIn(weaponSpec, 500f, 1000f)
        override fun type(): FilterType = FilterType.RANGE
        override fun name(): String = "Range [500, 1000]"
    }

    private object Range1000To1500Filter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = rangeIn(weaponSpec, 1000f, 1500f)
        override fun type(): FilterType = FilterType.RANGE
        override fun name(): String = "Range [1000, 1500]"
    }

    private object Range1500To2000Filter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = rangeIn(weaponSpec, 1500f, 2000f)
        override fun type(): FilterType = FilterType.RANGE
        override fun name(): String = "Range [1500, 2000]"
    }

    private object Range2000To2500Filter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = rangeIn(weaponSpec, 2000f, 2500f)
        override fun type(): FilterType = FilterType.RANGE
        override fun name(): String = "Range [2000, 2500]"
    }

    private object Range2500PlusFilter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = weaponSpec.maxRange >= 2500f
        override fun type(): FilterType = FilterType.RANGE
        override fun name(): String = "Range [2500+]"
    }

    private object FluxDamage0To05Filter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = fluxPerDamageIn(weaponSpec, 0f, 0.5f)
        override fun type(): FilterType = FilterType.FLUX_DAMAGE
        override fun name(): String = "Flux/Damage [0.0, 0.5]"
    }

    private object FluxDamage05To075Filter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = fluxPerDamageIn(weaponSpec, 0.5f, 0.75f)
        override fun type(): FilterType = FilterType.FLUX_DAMAGE
        override fun name(): String = "Flux/Damage [0.5, 0.75]"
    }

    private object FluxDamage075To10Filter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = fluxPerDamageIn(weaponSpec, 0.75f, 1.0f)
        override fun type(): FilterType = FilterType.FLUX_DAMAGE
        override fun name(): String = "Flux/Damage [0.75, 1.0]"
    }

    private object FluxDamage10To125Filter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = fluxPerDamageIn(weaponSpec, 1.0f, 1.25f)
        override fun type(): FilterType = FilterType.FLUX_DAMAGE
        override fun name(): String = "Flux/Damage [1.0, 1.25]"
    }

    private object FluxDamage125To15Filter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = fluxPerDamageIn(weaponSpec, 1.25f, 1.5f)
        override fun type(): FilterType = FilterType.FLUX_DAMAGE
        override fun name(): String = "Flux/Damage [1.25, 1.5]"
    }

    private object FluxDamage15PlusFilter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = fluxPerDamageAtLeast(weaponSpec, 1.5f)
        override fun type(): FilterType = FilterType.FLUX_DAMAGE
        override fun name(): String = "Flux/Damage [1.5+]"
    }

    private object AmmoOrChargesFilter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = hasAmmoOrCharges(weaponSpec)
        override fun type(): FilterType = FilterType.AMMO
        override fun name(): String = "Ammo/Charges"
    }

    private object NoAmmoOrChargesFilter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = !hasAmmoOrCharges(weaponSpec)
        override fun type(): FilterType = FilterType.AMMO
        override fun name(): String = "No Ammo/Charges"
    }

    private object EmpFilter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = hasEmp(weaponSpec)
        override fun type(): FilterType = FilterType.EMP
        override fun name(): String = "EMP"
    }

    private object NoEmpFilter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = !hasEmp(weaponSpec)
        override fun type(): FilterType = FilterType.EMP
        override fun name(): String = "No EMP"
    }

    private object PointDefenseFilter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = isPointDefense(weaponSpec)
        override fun type(): FilterType = FilterType.POINT_DEFENSE
        override fun name(): String = "PD"
    }

    private object NoPointDefenseFilter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = !isPointDefense(weaponSpec)
        override fun type(): FilterType = FilterType.POINT_DEFENSE
        override fun name(): String = "No PD"
    }

    private object ExplosiveDamageFilter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = weaponSpec.damageType == DamageType.HIGH_EXPLOSIVE
        override fun type(): FilterType = FilterType.DAMAGE_TYPE
        override fun name(): String = "Explosive"
    }

    private object KineticDamageFilter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = weaponSpec.damageType == DamageType.KINETIC
        override fun type(): FilterType = FilterType.DAMAGE_TYPE
        override fun name(): String = "Kinetic"
    }

    private object FragmentationDamageFilter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = weaponSpec.damageType == DamageType.FRAGMENTATION
        override fun type(): FilterType = FilterType.DAMAGE_TYPE
        override fun name(): String = "Fragmentation"
    }

    private object EnergyDamageFilter : WeaponFilter() {
        override fun matches(weaponSpec: WeaponSpecAPI): Boolean = weaponSpec.damageType == DamageType.ENERGY
        override fun type(): FilterType = FilterType.DAMAGE_TYPE
        override fun name(): String = "Energy Damage"
    }

    private fun rangeIn(weaponSpec: WeaponSpecAPI, minInclusive: Float, maxExclusive: Float): Boolean {
        val value = weaponSpec.maxRange
        return value.isFinite() && value >= minInclusive && value < maxExclusive
    }

    private fun fluxPerDamageIn(weaponSpec: WeaponSpecAPI, minInclusive: Float, maxExclusive: Float): Boolean {
        val value = weaponSpec.derivedStats.fluxPerDam
        return value.isFinite() && value >= minInclusive && value < maxExclusive
    }

    private fun fluxPerDamageAtLeast(weaponSpec: WeaponSpecAPI, minInclusive: Float): Boolean {
        val value = weaponSpec.derivedStats.fluxPerDam
        return value.isFinite() && value >= minInclusive
    }

    private fun hasAmmoOrCharges(weaponSpec: WeaponSpecAPI): Boolean = weaponSpec.usesAmmo()

    private fun hasEmp(weaponSpec: WeaponSpecAPI): Boolean =
        weaponSpec.derivedStats.empPerShot > 0f || weaponSpec.derivedStats.empPerSecond > 0f

    private fun isPointDefense(weaponSpec: WeaponSpecAPI): Boolean =
        weaponSpec.aiHints.any { hint ->
            hint == WeaponAPI.AIHints.PD ||
                hint == WeaponAPI.AIHints.PD_ONLY ||
                hint == WeaponAPI.AIHints.PD_ALSO
        }
}
