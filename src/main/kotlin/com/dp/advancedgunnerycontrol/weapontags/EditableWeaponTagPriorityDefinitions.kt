package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.OPTIONAL_DAMAGE_TYPE_EXCLUSION_PATTERN
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.SIMPLE_DAMAGE_TYPE_EXCLUSION_PATTERN
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.damageTypeExclusionDefaults
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.damageTypeExclusionParameters
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.damageTypeExclusionValues
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.damageTypeExclusionsFromMatch
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.damageTypeExclusionsFromValues
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.simpleDamageTypeExclusionTagSuffix
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_PRIORITY_MULTIPLIER
import com.dp.advancedgunnerycontrol.weaponais.tags.DamageTypeExclusions

internal object EditableWeaponTagPriorityDefinitions {
    fun definitions(): List<EditableWeaponTagDefinition> = listOf(
        priorityMultiplierDefinition(
            id = "prio_small_multiplier",
            family = "PrioSmall",
            representativeTemplateTag = "PrioSmall(N)",
            acceptedTemplateTags = setOf("PrioSmall", "PrioSmall(N)", "PrioPD", "PrioritizePD", "PrioritisePD"),
        ),
        priorityMultiplierDefinition(
            id = "prio_fighter_multiplier",
            family = "PrioFighter",
            representativeTemplateTag = "PrioFighter(N)",
            acceptedTemplateTags = setOf("PrioFighter", "PrioFighter(N)"),
        ),
        priorityMultiplierDefinition(
            id = "prio_missile_multiplier",
            family = "PrioMissile",
            representativeTemplateTag = "PrioMissile(N)",
            acceptedTemplateTags = setOf("PrioMissile", "PrioMissile(N)"),
        ),
        priorityMultiplierDefinition(
            id = "prio_ship_multiplier",
            family = "PrioShip",
            representativeTemplateTag = "PrioShip(N)",
            acceptedTemplateTags = setOf("PrioShip", "PrioShips", "PrioShip(N)"),
        ),
        priorityMultiplierDefinition(
            id = "prio_focused_multiplier",
            family = "PrioFocused",
            representativeTemplateTag = "PrioFocused(N)",
            acceptedTemplateTags = setOf("PrioFocused", "PrioFocused(N)"),
        ),
        priorityMultiplierDefinition(
            id = "prio_shields_multiplier",
            family = "PrioShields",
            representativeTemplateTag = "PrioShields(N)",
            acceptedTemplateTags = setOf("PrioShields", "PrioShields(N)"),
            includeDamageTypeExclusions = true,
        ),
        priorityMultiplierDefinition(
            id = "prio_hull_multiplier",
            family = "PrioHull",
            representativeTemplateTag = "PrioHull(N)",
            acceptedTemplateTags = setOf("PrioHull", "PrioHull(N)"),
            includeDamageTypeExclusions = true,
        ),
        priorityMultiplierDefinition(
            id = "prio_close_multiplier",
            family = "PrioClose",
            representativeTemplateTag = "PrioClose(N)",
            acceptedTemplateTags = setOf("PrioClose", "PrioClose(N)"),
        ),
        priorityMultiplierDefinition(
            id = "prio_far_multiplier",
            family = "PrioFar",
            representativeTemplateTag = "PrioFar(N)",
            acceptedTemplateTags = setOf("PrioFar", "PrioFar(N)"),
        )
    )

    private fun priorityMultiplierDefinition(
        id: String,
        family: String,
        representativeTemplateTag: String,
        acceptedTemplateTags: Set<String>,
        includeDamageTypeExclusions: Boolean = false,
    ): EditableWeaponTagDefinition {
        val defaultMultiplier = priorityMultiplierDefault()
        val regex = Regex("${Regex.escape(family)}\\((\\d+)${
            if (includeDamageTypeExclusions) OPTIONAL_DAMAGE_TYPE_EXCLUSION_PATTERN else ""
        }\\)")
        val simpleExclusionRegex = Regex("${Regex.escape(family)}$SIMPLE_DAMAGE_TYPE_EXCLUSION_PATTERN")
        val parameters = mutableListOf<EditableTagParameterDefinition>(
            NumberParameter(
                id = PARAM_PRIORITY_MULTIPLIER,
                label = "Priority multiplier",
                minValue = 1,
                maxValue = 10000,
                defaultValue = defaultMultiplier,
            )
        )
        if (includeDamageTypeExclusions) {
            parameters += damageTypeExclusionParameters
        }
        val defaults = mutableMapOf(PARAM_PRIORITY_MULTIPLIER to defaultMultiplier.toString())
        if (includeDamageTypeExclusions) {
            defaults += damageTypeExclusionDefaults()
        }

        return EditableWeaponTagDefinition(
            id = id,
            family = family,
            templateTag = representativeTemplateTag,
            acceptedTemplateTags = acceptedTemplateTags,
            parameters = parameters,
            defaultValues = defaults,
            parser = parser@{ canonicalTag ->
                val exclusions = if (includeDamageTypeExclusions && simpleExclusionRegex.matches(canonicalTag)) {
                    val match = simpleExclusionRegex.matchEntire(canonicalTag) ?: return@parser null
                    damageTypeExclusionsFromMatch(match.groupValues.getOrNull(1))
                        ?: return@parser null
                } else {
                    DamageTypeExclusions.NONE
                }
                val multiplier = when {
                    canonicalTag == family -> priorityMultiplierDefault().toString()
                    includeDamageTypeExclusions && simpleExclusionRegex.matches(canonicalTag) ->
                        priorityMultiplierDefault().toString()
                    regex.matches(canonicalTag) -> regex.matchEntire(canonicalTag)?.groupValues?.get(1)
                    else -> null
                } ?: return@parser null
                val parsedExclusions = if (regex.matches(canonicalTag)) {
                    val match = regex.matchEntire(canonicalTag) ?: return@parser null
                    damageTypeExclusionsFromMatch(match.groupValues.getOrNull(2))
                        ?: return@parser null
                } else {
                    exclusions
                }
                ParsedEditableWeaponTag(
                    canonicalTag = canonicalTag,
                    definitionId = id,
                    family = family,
                    templateTag = representativeTemplateTag,
                    parameterValues = buildMap {
                        put(PARAM_PRIORITY_MULTIPLIER, multiplier)
                        if (includeDamageTypeExclusions) {
                            putAll(damageTypeExclusionValues(parsedExclusions))
                        }
                    },
                    exclusivityKeys = setOf(family)
                )
            },
            builder = { values ->
                val multiplierText = values[PARAM_PRIORITY_MULTIPLIER] ?: priorityMultiplierDefault().toString()
                val multiplier = multiplierText.toIntOrNull()
                val damageTypeExclusions = damageTypeExclusionsFromValues(values)
                val errors = mutableListOf<EditableTagValidationError>()
                if (multiplier == null || multiplier !in 1..10000) {
                    errors += EditableTagValidationError(
                        PARAM_PRIORITY_MULTIPLIER,
                        "Enter a whole number from 1 to 10000."
                    )
                }

                if (errors.isNotEmpty()) {
                    EditableTagBuildResult(canonicalTag = null, errors = errors)
                } else if (includeDamageTypeExclusions && multiplier == priorityMultiplierDefault()) {
                    EditableTagBuildResult(
                        canonicalTag = "$family${simpleDamageTypeExclusionTagSuffix(damageTypeExclusions)}",
                        errors = emptyList()
                    )
                } else {
                    EditableTagBuildResult(
                        canonicalTag = "$family($multiplier${if (includeDamageTypeExclusions) damageTypeExclusions.suffix() else ""})",
                        errors = emptyList()
                    )
                }
            }
        )
    }
}
