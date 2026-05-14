package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.tags.DamageTypeExclusions

internal object EditableWeaponTagDamageExclusionSupport {
    const val DAMAGE_TYPE_EXCLUSION_LIST_PATTERN = "(?:K|HE|F|E|B|M|P)(?:,(?:K|HE|F|E|B|M|P))*"
    const val OPTIONAL_DAMAGE_TYPE_EXCLUSION_PATTERN = "(?:,Ignore<($DAMAGE_TYPE_EXCLUSION_LIST_PATTERN)>)?"
    const val SIMPLE_DAMAGE_TYPE_EXCLUSION_PATTERN = "\\(Ignore<($DAMAGE_TYPE_EXCLUSION_LIST_PATTERN)>\\)"

    val damageTypeExclusionParameters = listOf(
        ToggleParameter(
            id = EditableWeaponTagDefinitions.PARAM_IGNORE_KINETIC_WEAPONS,
            label = "Ignore kinetic weapons",
            defaultValue = false,
            markHiddenChange = true,
        ),
        ToggleParameter(
            id = EditableWeaponTagDefinitions.PARAM_IGNORE_HIGH_EXPLOSIVE_WEAPONS,
            label = "Ignore HE weapons",
            defaultValue = false,
            markHiddenChange = true,
        ),
        ToggleParameter(
            id = EditableWeaponTagDefinitions.PARAM_IGNORE_FRAGMENTATION_WEAPONS,
            label = "Ignore frag weapons",
            defaultValue = false,
            markHiddenChange = true,
        ),
        ToggleParameter(
            id = EditableWeaponTagDefinitions.PARAM_IGNORE_ENERGY_WEAPONS,
            label = "Ignore energy weapons",
            defaultValue = false,
            markHiddenChange = true,
        ),
        ToggleParameter(
            id = EditableWeaponTagDefinitions.PARAM_IGNORE_BEAM_WEAPONS,
            label = "Ignore beam weapons",
            defaultValue = false,
            markHiddenChange = true,
        ),
        ToggleParameter(
            id = EditableWeaponTagDefinitions.PARAM_IGNORE_MISSILE_WEAPONS,
            label = "Ignore missile weapons",
            defaultValue = false,
            markHiddenChange = true,
        ),
        ToggleParameter(
            id = EditableWeaponTagDefinitions.PARAM_IGNORE_PROJECTILE_WEAPONS,
            label = "Ignore projectile weapons",
            defaultValue = false,
            markHiddenChange = true,
        ),
    )

    fun damageTypeExclusionDefaults(): Map<String, String> = mapOf(
        EditableWeaponTagDefinitions.PARAM_IGNORE_KINETIC_WEAPONS to false.toString(),
        EditableWeaponTagDefinitions.PARAM_IGNORE_HIGH_EXPLOSIVE_WEAPONS to false.toString(),
        EditableWeaponTagDefinitions.PARAM_IGNORE_FRAGMENTATION_WEAPONS to false.toString(),
        EditableWeaponTagDefinitions.PARAM_IGNORE_ENERGY_WEAPONS to false.toString(),
        EditableWeaponTagDefinitions.PARAM_IGNORE_BEAM_WEAPONS to false.toString(),
        EditableWeaponTagDefinitions.PARAM_IGNORE_MISSILE_WEAPONS to false.toString(),
        EditableWeaponTagDefinitions.PARAM_IGNORE_PROJECTILE_WEAPONS to false.toString(),
    )

    fun damageTypeExclusionValues(exclusions: DamageTypeExclusions): Map<String, String> {
        val active = exclusions.activeForCurrentSettings()
        return mapOf(
            EditableWeaponTagDefinitions.PARAM_IGNORE_KINETIC_WEAPONS to active.kinetic.toString(),
            EditableWeaponTagDefinitions.PARAM_IGNORE_HIGH_EXPLOSIVE_WEAPONS to active.highExplosive.toString(),
            EditableWeaponTagDefinitions.PARAM_IGNORE_FRAGMENTATION_WEAPONS to active.fragmentation.toString(),
            EditableWeaponTagDefinitions.PARAM_IGNORE_ENERGY_WEAPONS to active.energy.toString(),
            EditableWeaponTagDefinitions.PARAM_IGNORE_BEAM_WEAPONS to active.beam.toString(),
            EditableWeaponTagDefinitions.PARAM_IGNORE_MISSILE_WEAPONS to active.missile.toString(),
            EditableWeaponTagDefinitions.PARAM_IGNORE_PROJECTILE_WEAPONS to active.projectile.toString(),
        )
    }

    fun damageTypeExclusionsFromValues(values: Map<String, String>): DamageTypeExclusions =
        DamageTypeExclusions(
            kinetic = values[EditableWeaponTagDefinitions.PARAM_IGNORE_KINETIC_WEAPONS]?.toBooleanStrictOrNull() ?: false,
            highExplosive = values[EditableWeaponTagDefinitions.PARAM_IGNORE_HIGH_EXPLOSIVE_WEAPONS]?.toBooleanStrictOrNull()
                ?: false,
            fragmentation = values[EditableWeaponTagDefinitions.PARAM_IGNORE_FRAGMENTATION_WEAPONS]?.toBooleanStrictOrNull()
                ?: false,
            energy = values[EditableWeaponTagDefinitions.PARAM_IGNORE_ENERGY_WEAPONS]?.toBooleanStrictOrNull() ?: false,
            beam = values[EditableWeaponTagDefinitions.PARAM_IGNORE_BEAM_WEAPONS]?.toBooleanStrictOrNull() ?: false,
            missile = values[EditableWeaponTagDefinitions.PARAM_IGNORE_MISSILE_WEAPONS]?.toBooleanStrictOrNull() ?: false,
            projectile = values[EditableWeaponTagDefinitions.PARAM_IGNORE_PROJECTILE_WEAPONS]?.toBooleanStrictOrNull() ?: false,
        ).activeForCurrentSettings()

    fun damageTypeExclusionsFromMatch(rawTokens: String?): DamageTypeExclusions? =
        DamageTypeExclusions.fromTokens(rawTokens?.takeIf { it.isNotBlank() })

    fun simpleDamageTypeExclusionTagSuffix(exclusions: DamageTypeExclusions): String =
        exclusions.suffix().removePrefix(",").takeIf { it.isNotBlank() }?.let { "($it)" }.orEmpty()

    fun isParameterVisible(parameter: EditableTagParameterDefinition): Boolean =
        when (parameter.id) {
            EditableWeaponTagDefinitions.PARAM_IGNORE_ENERGY_WEAPONS -> Settings.showEnergyDamageTypeExclusionOption()
            EditableWeaponTagDefinitions.PARAM_IGNORE_MISSILE_WEAPONS -> Settings.showMissileDamageTypeExclusionOption()
            EditableWeaponTagDefinitions.PARAM_IGNORE_PROJECTILE_WEAPONS -> Settings.showProjectileDamageTypeExclusionOption()
            else -> true
        }
}
