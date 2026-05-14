package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.SIMPLE_DAMAGE_TYPE_EXCLUSION_PATTERN
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.damageTypeExclusionDefaults
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.damageTypeExclusionParameters
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.damageTypeExclusionValues
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.damageTypeExclusionsFromMatch
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.damageTypeExclusionsFromValues
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.simpleDamageTypeExclusionTagSuffix
import com.dp.advancedgunnerycontrol.weaponais.tags.DamageTypeExclusions

internal fun simpleDamageExclusionDefinitions(): List<EditableWeaponTagDefinition> = listOf(
    simpleDamageExclusionDefinition(
        id = "target_phase_damage_type_exclusions",
        family = "TargetPhase",
    ),
    simpleDamageExclusionDefinition(
        id = "avoid_phased_damage_type_exclusions",
        family = "AvoidPhased",
    ),
    simpleDamageExclusionDefinition(
        id = "prio_wounded_damage_type_exclusions",
        family = "PrioWounded",
    ),
)

private fun simpleDamageExclusionDefinition(
    id: String,
    family: String,
): EditableWeaponTagDefinition {
    val regex = Regex("${Regex.escape(family)}$SIMPLE_DAMAGE_TYPE_EXCLUSION_PATTERN")
    val defaults = damageTypeExclusionDefaults()
    return EditableWeaponTagDefinition(
        id = id,
        family = family,
        templateTag = family,
        acceptedTemplateTags = setOf(family),
        parameters = damageTypeExclusionParameters,
        defaultValues = defaults,
        parser = parser@{ canonicalTag ->
            val exclusions = when {
                canonicalTag == family -> DamageTypeExclusions.NONE
                regex.matches(canonicalTag) -> {
                    val match = regex.matchEntire(canonicalTag) ?: return@parser null
                    damageTypeExclusionsFromMatch(match.groupValues.getOrNull(1))
                        ?: return@parser null
                }
                else -> return@parser null
            }
            ParsedEditableWeaponTag(
                canonicalTag = canonicalTag,
                definitionId = id,
                family = family,
                templateTag = family,
                parameterValues = damageTypeExclusionValues(exclusions),
                exclusivityKeys = setOf(family)
            )
        },
        builder = { values ->
            val exclusions = damageTypeExclusionsFromValues(values)
            EditableTagBuildResult(
                canonicalTag = "$family${simpleDamageTypeExclusionTagSuffix(exclusions)}",
                errors = emptyList()
            )
        }
    )
}
