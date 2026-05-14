package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_HIGH_EXPLOSIVE_THRESHOLD
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_KINETIC_THRESHOLD
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_THRESHOLD
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_TRIGGER_HAPPINESS
import com.dp.advancedgunnerycontrol.settings.Settings

internal fun opportunistAmmoDefinition(): EditableWeaponTagDefinition {
    val regex = Regex("Opportunist\\(A<(\\d+)%(?:,K<(\\d+)%,HE<(\\d+)%,TH<(\\d+)%)?\\)")
    fun currentDefaults(): Map<String, String> = mapOf(
        PARAM_THRESHOLD to Settings.conserveAmmo().let(::settingsPercent).coerceIn(0, 99).toString(),
        PARAM_KINETIC_THRESHOLD to opportunistKineticDefaultPercent().toString(),
        PARAM_HIGH_EXPLOSIVE_THRESHOLD to opportunistHighExplosiveDefaultPercent().toString(),
        PARAM_TRIGGER_HAPPINESS to opportunistTriggerDefaultPercent().toString(),
    )
    val parameters = listOf(
        NumberParameter(
            id = PARAM_THRESHOLD,
            label = "Ammo below",
            minValue = 0,
            maxValue = 99,
            defaultValue = currentDefaults().getValue(PARAM_THRESHOLD).toInt(),
            suffix = "%",
        ),
        NumberParameter(
            id = PARAM_KINETIC_THRESHOLD,
            label = "Kinetic",
            minValue = 0,
            maxValue = 100,
            defaultValue = opportunistKineticDefaultPercent(),
            suffix = "%",
            markHiddenChange = true,
        ),
        NumberParameter(
            id = PARAM_HIGH_EXPLOSIVE_THRESHOLD,
            label = "HE/Frag",
            minValue = 0,
            maxValue = 100,
            defaultValue = opportunistHighExplosiveDefaultPercent(),
            suffix = "%",
            markHiddenChange = true,
        ),
        NumberParameter(
            id = PARAM_TRIGGER_HAPPINESS,
            label = "Trigger",
            minValue = 10,
            maxValue = 500,
            defaultValue = opportunistTriggerDefaultPercent(),
            suffix = "%",
            markHiddenChange = true,
        )
    )
    return EditableWeaponTagDefinition(
        id = "opportunist_ammo_threshold",
        family = "Opportunist",
        templateTag = "Opportunist(A<N%)",
        acceptedTemplateTags = setOf("Opportunist(A<N%)", "ConserveAmmo"),
        parameters = parameters,
        defaultValues = currentDefaults(),
        parser = parser@{ canonicalTag ->
            val values = when {
                canonicalTag == "ConserveAmmo" -> currentDefaults()
                regex.matches(canonicalTag) -> {
                    val match = regex.matchEntire(canonicalTag) ?: return@parser null
                    val defaults = currentDefaults()
                    mapOf(
                        PARAM_THRESHOLD to match.groupValues[1],
                        PARAM_KINETIC_THRESHOLD to (
                            match.groupValues.getOrNull(2).takeUnless { it.isNullOrBlank() }
                                ?: defaults.getValue(PARAM_KINETIC_THRESHOLD)
                            ),
                        PARAM_HIGH_EXPLOSIVE_THRESHOLD to (
                            match.groupValues.getOrNull(3).takeUnless { it.isNullOrBlank() }
                                ?: defaults.getValue(PARAM_HIGH_EXPLOSIVE_THRESHOLD)
                            ),
                        PARAM_TRIGGER_HAPPINESS to (
                            match.groupValues.getOrNull(4).takeUnless { it.isNullOrBlank() }
                                ?: defaults.getValue(PARAM_TRIGGER_HAPPINESS)
                            ),
                    )
                }
                else -> return@parser null
            }
            ParsedEditableWeaponTag(
                canonicalTag = canonicalTag,
                definitionId = "opportunist_ammo_threshold",
                family = "Opportunist",
                templateTag = "Opportunist(A<N%)",
                parameterValues = values,
                exclusivityKeys = setOf("Opportunist:A")
            )
        },
        builder = { values ->
            val dynamicDefaults = currentDefaults()
            val ammoText = values[PARAM_THRESHOLD] ?: dynamicDefaults.getValue(PARAM_THRESHOLD)
            val kineticText = values[PARAM_KINETIC_THRESHOLD] ?: dynamicDefaults.getValue(PARAM_KINETIC_THRESHOLD)
            val heText = values[PARAM_HIGH_EXPLOSIVE_THRESHOLD] ?: dynamicDefaults.getValue(PARAM_HIGH_EXPLOSIVE_THRESHOLD)
            val triggerText = values[PARAM_TRIGGER_HAPPINESS] ?: dynamicDefaults.getValue(PARAM_TRIGGER_HAPPINESS)
            val ammo = ammoText.toIntOrNull()
            val kinetic = kineticText.toIntOrNull()
            val he = heText.toIntOrNull()
            val trigger = triggerText.toIntOrNull()
            val errors = mutableListOf<EditableTagValidationError>()
            if (ammo == null || ammo !in 0..99) {
                errors += EditableTagValidationError(PARAM_THRESHOLD, "Enter a whole number from 0 to 99.")
            }
            if (kinetic == null || kinetic !in 0..100) {
                errors += EditableTagValidationError(PARAM_KINETIC_THRESHOLD, "Enter a whole number from 0 to 100.")
            }
            if (he == null || he !in 0..100) {
                errors += EditableTagValidationError(PARAM_HIGH_EXPLOSIVE_THRESHOLD, "Enter a whole number from 0 to 100.")
            }
            if (trigger == null || trigger !in 10..500) {
                errors += EditableTagValidationError(PARAM_TRIGGER_HAPPINESS, "Enter a whole number from 10 to 500.")
            }
            if (errors.isNotEmpty()) {
                EditableTagBuildResult(canonicalTag = null, errors = errors)
            } else {
                val tuningSuffix = if (
                    kineticText == dynamicDefaults.getValue(PARAM_KINETIC_THRESHOLD) &&
                    heText == dynamicDefaults.getValue(PARAM_HIGH_EXPLOSIVE_THRESHOLD) &&
                    triggerText == dynamicDefaults.getValue(PARAM_TRIGGER_HAPPINESS)
                ) {
                    ""
                } else {
                    ",K<$kinetic%,HE<$he%,TH<$trigger%"
                }
                EditableTagBuildResult(
                    canonicalTag = "Opportunist(A<$ammo%$tuningSuffix)",
                    errors = emptyList()
                )
            }
        }
    )
}

internal fun opportunistTuningDefinition(): EditableWeaponTagDefinition {
    val regex = Regex("Opportunist\\(K<(\\d+)%,HE<(\\d+)%,TH<(\\d+)%\\)")
    fun currentDefaults(): Map<String, String> = mapOf(
        PARAM_KINETIC_THRESHOLD to opportunistKineticDefaultPercent().toString(),
        PARAM_HIGH_EXPLOSIVE_THRESHOLD to opportunistHighExplosiveDefaultPercent().toString(),
        PARAM_TRIGGER_HAPPINESS to opportunistTriggerDefaultPercent().toString(),
    )
    val parameters = listOf(
        NumberParameter(
            id = PARAM_KINETIC_THRESHOLD,
            label = "Kinetic",
            minValue = 0,
            maxValue = 100,
            defaultValue = opportunistKineticDefaultPercent(),
            suffix = "%",
            markHiddenChange = true
        ),
        NumberParameter(
            id = PARAM_HIGH_EXPLOSIVE_THRESHOLD,
            label = "HE/Frag",
            minValue = 0,
            maxValue = 100,
            defaultValue = opportunistHighExplosiveDefaultPercent(),
            suffix = "%",
            markHiddenChange = true
        ),
        NumberParameter(
            id = PARAM_TRIGGER_HAPPINESS,
            label = "Trigger",
            minValue = 10,
            maxValue = 500,
            defaultValue = opportunistTriggerDefaultPercent(),
            suffix = "%",
            markHiddenChange = true
        )
    )
    val defaults = currentDefaults()

    return EditableWeaponTagDefinition(
        id = "opportunist_tuning",
        family = "Opportunist",
        templateTag = "Opportunist(K<N%,HE<N%,TH<N%)",
        acceptedTemplateTags = setOf("Opportunist", "Opportunist(K<N%,HE<N%,TH<N%)"),
        parameters = parameters,
        defaultValues = defaults,
        parser = parser@{ canonicalTag ->
            val values = when {
                canonicalTag == "Opportunist" -> currentDefaults()
                regex.matches(canonicalTag) -> {
                    val match = regex.matchEntire(canonicalTag) ?: return@parser null
                    mapOf(
                        PARAM_KINETIC_THRESHOLD to match.groupValues[1],
                        PARAM_HIGH_EXPLOSIVE_THRESHOLD to match.groupValues[2],
                        PARAM_TRIGGER_HAPPINESS to match.groupValues[3],
                    )
                }
                else -> return@parser null
            }
            ParsedEditableWeaponTag(
                canonicalTag = canonicalTag,
                definitionId = "opportunist_tuning",
                family = "Opportunist",
                templateTag = "Opportunist",
                parameterValues = values,
                exclusivityKeys = setOf("Opportunist")
            )
        },
        builder = { values ->
            val dynamicDefaults = currentDefaults()
            val kineticText = values[PARAM_KINETIC_THRESHOLD] ?: dynamicDefaults.getValue(PARAM_KINETIC_THRESHOLD)
            val heText = values[PARAM_HIGH_EXPLOSIVE_THRESHOLD] ?: dynamicDefaults.getValue(PARAM_HIGH_EXPLOSIVE_THRESHOLD)
            val triggerText = values[PARAM_TRIGGER_HAPPINESS] ?: dynamicDefaults.getValue(PARAM_TRIGGER_HAPPINESS)
            val kinetic = kineticText.toIntOrNull()
            val he = heText.toIntOrNull()
            val trigger = triggerText.toIntOrNull()
            val errors = mutableListOf<EditableTagValidationError>()
            if (kinetic == null || kinetic !in 0..100) {
                errors += EditableTagValidationError(PARAM_KINETIC_THRESHOLD, "Enter a whole number from 0 to 100.")
            }
            if (he == null || he !in 0..100) {
                errors += EditableTagValidationError(PARAM_HIGH_EXPLOSIVE_THRESHOLD, "Enter a whole number from 0 to 100.")
            }
            if (trigger == null || trigger !in 10..500) {
                errors += EditableTagValidationError(PARAM_TRIGGER_HAPPINESS, "Enter a whole number from 10 to 500.")
            }

            if (errors.isNotEmpty()) {
                EditableTagBuildResult(canonicalTag = null, errors = errors)
            } else {
                val canonicalTag = if (
                    kineticText == dynamicDefaults.getValue(PARAM_KINETIC_THRESHOLD) &&
                    heText == dynamicDefaults.getValue(PARAM_HIGH_EXPLOSIVE_THRESHOLD) &&
                    triggerText == dynamicDefaults.getValue(PARAM_TRIGGER_HAPPINESS)
                ) {
                    "Opportunist"
                } else {
                    "Opportunist(K<$kinetic%,HE<$he%,TH<$trigger%)"
                }
                EditableTagBuildResult(canonicalTag = canonicalTag, errors = emptyList())
            }
        }
    )
}
