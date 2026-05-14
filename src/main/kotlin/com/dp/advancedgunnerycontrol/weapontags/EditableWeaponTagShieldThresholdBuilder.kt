package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.OPTIONAL_DAMAGE_TYPE_EXCLUSION_PATTERN
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.SIMPLE_DAMAGE_TYPE_EXCLUSION_PATTERN
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.damageTypeExclusionDefaults
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.damageTypeExclusionParameters
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.damageTypeExclusionValues
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.damageTypeExclusionsFromMatch
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.damageTypeExclusionsFromValues
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.simpleDamageTypeExclusionTagSuffix
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_TARGET_SHIELD_THRESHOLD
import com.dp.advancedgunnerycontrol.weaponais.tags.DamageTypeExclusions

internal fun shieldThresholdDefinition(
    id: String,
    family: String,
    representativeTemplateTag: String,
    acceptedTemplateTags: Set<String>,
    comparator: String,
    defaultThresholdProvider: () -> Int,
    aliasThresholds: Map<String, Int> = emptyMap(),
    exclusivityPrefix: String,
): EditableWeaponTagDefinition {
    val defaultThreshold = defaultThresholdProvider()
    val regex = Regex("${Regex.escape(family)}\\(S${Regex.escape(comparator)}(\\d+)%$OPTIONAL_DAMAGE_TYPE_EXCLUSION_PATTERN\\)")
    val simpleExclusionRegex = Regex("${Regex.escape(family)}$SIMPLE_DAMAGE_TYPE_EXCLUSION_PATTERN")
    val parameter = NumberParameter(
        id = PARAM_TARGET_SHIELD_THRESHOLD,
        label = "Target shield",
        minValue = 0,
        maxValue = 100,
        defaultValue = defaultThreshold,
        suffix = "%",
    )
    val defaults = mutableMapOf(PARAM_TARGET_SHIELD_THRESHOLD to defaultThreshold.toString())
    defaults += damageTypeExclusionDefaults()
    return EditableWeaponTagDefinition(
        id = id,
        family = family,
        templateTag = representativeTemplateTag,
        acceptedTemplateTags = acceptedTemplateTags,
        parameters = listOf(parameter) + damageTypeExclusionParameters,
        defaultValues = defaults,
        parser = parser@{ canonicalTag ->
            val damageTypeExclusions = when {
                simpleExclusionRegex.matches(canonicalTag) -> {
                    val match = simpleExclusionRegex.matchEntire(canonicalTag) ?: return@parser null
                    damageTypeExclusionsFromMatch(match.groupValues.getOrNull(1))
                        ?: return@parser null
                }
                regex.matches(canonicalTag) -> {
                    val match = regex.matchEntire(canonicalTag) ?: return@parser null
                    damageTypeExclusionsFromMatch(match.groupValues.getOrNull(2))
                        ?: return@parser null
                }
                else -> DamageTypeExclusions.NONE
            }
            val threshold = when {
                canonicalTag == family -> defaultThresholdProvider().toString()
                canonicalTag in aliasThresholds -> aliasThresholds[canonicalTag]?.toString()
                simpleExclusionRegex.matches(canonicalTag) -> defaultThresholdProvider().toString()
                regex.matches(canonicalTag) -> regex.matchEntire(canonicalTag)?.groupValues?.get(1)
                else -> null
            } ?: return@parser null
            ParsedEditableWeaponTag(
                canonicalTag = canonicalTag,
                definitionId = id,
                family = family,
                templateTag = representativeTemplateTag,
                parameterValues = buildMap {
                    put(PARAM_TARGET_SHIELD_THRESHOLD, threshold)
                    putAll(damageTypeExclusionValues(damageTypeExclusions))
                },
                exclusivityKeys = setOf(exclusivityPrefix)
            )
        },
        builder = { values ->
            val currentDefaultThreshold = defaultThresholdProvider()
            val thresholdText = values[PARAM_TARGET_SHIELD_THRESHOLD] ?: currentDefaultThreshold.toString()
            val threshold = thresholdText.toIntOrNull()
            val damageTypeExclusions = damageTypeExclusionsFromValues(values)
            val errors = mutableListOf<EditableTagValidationError>()
            if (threshold == null || threshold !in parameter.minValue..parameter.maxValue) {
                errors += EditableTagValidationError(
                    PARAM_TARGET_SHIELD_THRESHOLD,
                    "Enter a whole number from ${parameter.minValue} to ${parameter.maxValue}."
                )
            }
            if (errors.isNotEmpty()) {
                EditableTagBuildResult(canonicalTag = null, errors = errors)
            } else if (threshold == currentDefaultThreshold) {
                EditableTagBuildResult(
                    canonicalTag = "$family${simpleDamageTypeExclusionTagSuffix(damageTypeExclusions)}",
                    errors = emptyList()
                )
            } else {
                EditableTagBuildResult(
                    canonicalTag = "$family(S$comparator$threshold%${damageTypeExclusions.suffix()})",
                    errors = emptyList()
                )
            }
        }
    )
}
