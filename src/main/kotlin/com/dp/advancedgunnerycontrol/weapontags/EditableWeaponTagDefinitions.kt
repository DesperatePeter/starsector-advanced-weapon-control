package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.isParameterVisible

object EditableWeaponTagDefinitions {
    const val PARAM_FLUX_METRIC = "fluxMetric"
    const val PARAM_THRESHOLD = "threshold"
    const val PARAM_TARGET_SHIELD_THRESHOLD = "targetShieldThreshold"
    const val PARAM_TOTAL_FLUX_CAP = "totalFluxCap"
    const val PARAM_PRIORITY_MULTIPLIER = "priorityMultiplier"
    const val PARAM_KINETIC_THRESHOLD = "kineticThreshold"
    const val PARAM_HIGH_EXPLOSIVE_THRESHOLD = "highExplosiveThreshold"
    const val PARAM_TRIGGER_HAPPINESS = "triggerHappiness"
    const val PARAM_CLEANUP_DAMAGE_CAP = "cleanupDamageCap"
    const val PARAM_REQUIRE_SHIP_TARGET = "requireShipTarget"
    const val PARAM_TRIGGER_AMMO_FEEDER = "triggerAmmoFeeder"
    const val PARAM_TRIGGER_HIGH_ENERGY_FOCUS = "triggerHighEnergyFocus"
    const val PARAM_TRIGGER_LIDAR_ARRAY = "triggerLidarArray"
    const val PARAM_TRIGGER_TEMPORAL_SHELL = "triggerTemporalShell"
    const val PARAM_TRIGGER_ENTROPY_AMPLIFIER = "triggerEntropyAmplifier"
    const val PARAM_IGNORE_IF_BEAMED = "ignoreIfBeamed"
    const val PARAM_BEAM_WINDOW = "beamWindow"
    const val PARAM_IGNORE_KINETIC_WEAPONS = "ignoreKineticWeapons"
    const val PARAM_IGNORE_HIGH_EXPLOSIVE_WEAPONS = "ignoreHighExplosiveWeapons"
    const val PARAM_IGNORE_FRAGMENTATION_WEAPONS = "ignoreFragmentationWeapons"
    const val PARAM_IGNORE_ENERGY_WEAPONS = "ignoreEnergyWeapons"
    const val PARAM_IGNORE_BEAM_WEAPONS = "ignoreBeamWeapons"
    const val PARAM_IGNORE_MISSILE_WEAPONS = "ignoreMissileWeapons"
    const val PARAM_IGNORE_PROJECTILE_WEAPONS = "ignoreProjectileWeapons"

    private val definitionByTemplateCache = mutableMapOf<String, EditableWeaponTagDefinition?>()
    private val parsedByRawTagCache = mutableMapOf<String, ParsedEditableWeaponTag?>()

    val definitions: List<EditableWeaponTagDefinition> =
        EditableWeaponTagFluxDefinitions.definitions() +
            EditableWeaponTagThresholdDefinitions.definitions() +
            EditableWeaponTagPriorityDefinitions.definitions() +
            EditableWeaponTagSyncDefinitions.definitions()

    fun allTemplateTags(): List<String> = definitions.map { it.templateTag }

    fun clearCaches() {
        definitionByTemplateCache.clear()
        parsedByRawTagCache.clear()
    }

    fun definitionForTemplate(templateTag: String): EditableWeaponTagDefinition? {
        if (definitionByTemplateCache.containsKey(templateTag)) {
            return definitionByTemplateCache[templateTag]
        }
        val canonicalTemplate = tagNameToRegexName(templateTag)
        val definition = definitions.firstOrNull {
            canonicalTemplate in it.acceptedTemplateTags || templateTag in it.acceptedTemplateTags
        }
        definitionByTemplateCache[templateTag] = definition
        return definition
    }

    fun definitionById(id: String): EditableWeaponTagDefinition? =
        definitions.firstOrNull { it.id == id }

    fun parse(tag: String): ParsedEditableWeaponTag? {
        if (parsedByRawTagCache.containsKey(tag)) {
            return parsedByRawTagCache[tag]
        }
        return parseUncached(tag).also { parsedByRawTagCache[tag] = it }
    }

    private fun parseUncached(tag: String): ParsedEditableWeaponTag? {
        val canonicalTag = canonicalizeWeaponTagName(tag)
        return definitions.firstNotNullOfOrNull { it.parse(canonicalTag) }
    }

    fun isEditable(tag: String): Boolean = parse(tag) != null || definitionForTemplate(tag) != null

    fun visibleParameters(definition: EditableWeaponTagDefinition): List<EditableTagParameterDefinition> =
        definition.parameters.filter(::isParameterVisible)

    fun visibleParameters(
        definition: EditableWeaponTagDefinition,
        parameterValues: Map<String, String>,
    ): List<EditableTagParameterDefinition> =
        visibleParameters(definition).filter { parameter ->
            if (parameter.id == PARAM_TOTAL_FLUX_CAP) {
                val metric = parameterValues[PARAM_FLUX_METRIC]
                    ?: defaultValuesFor(definition)[PARAM_FLUX_METRIC]
                    ?: return@filter true
                return@filter totalFluxCapAppliesToMetric(definition, metric)
            }
            true
        }

    fun displayName(tag: String): String = displayNameForEditableWeaponTag(tag)

    fun areMutuallyExclusive(tag: String, existingTag: String): Boolean {
        val parsedTag = parse(tag) ?: return false
        val parsedExistingTag = parse(existingTag) ?: return false
        if (parsedTag.definitionId != parsedExistingTag.definitionId) return false
        return parsedTag.exclusivityKeys.any { it in parsedExistingTag.exclusivityKeys }
    }

    fun sharesEditableDefinition(tag: String, existingTag: String): Boolean {
        val parsedTag = parse(tag) ?: return false
        val parsedExistingTag = parse(existingTag) ?: return false
        return parsedTag.definitionId == parsedExistingTag.definitionId
    }

    fun buildCanonicalTag(templateTag: String, parameterValues: Map<String, String>): EditableTagBuildResult {
        val definition = definitionForTemplate(templateTag)
            ?: return EditableTagBuildResult(
                canonicalTag = null,
                errors = listOf(EditableTagValidationError("", "No editable tag definition exists for $templateTag."))
            )
        return definition.buildCanonicalTag(parameterValues)
    }

    fun defaultValuesFor(definition: EditableWeaponTagDefinition): Map<String, String> =
        resolvedEditableWeaponTagDefaultValues(definition)


}

fun clearEditableWeaponTagDefinitionCaches() {
    EditableWeaponTagDefinitions.clearCaches()
}
