package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.shipmodes.EditableShipModeDefinitions
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_HIGH_EXPLOSIVE_THRESHOLD
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_KINETIC_THRESHOLD
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_PRIORITY_MULTIPLIER
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_TARGET_SHIELD_THRESHOLD
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_THRESHOLD
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_TRIGGER_HAPPINESS

internal fun resolvedEditableWeaponTagDefaultValues(definition: EditableWeaponTagDefinition): Map<String, String> {
    val defaults = definition.defaultValues.toMutableMap()
    when (definition.id) {
        "avoid_shield_target_threshold" ->
            defaults[PARAM_TARGET_SHIELD_THRESHOLD] = avoidShieldDefaultPercent().toString()
        "target_shield_target_threshold" ->
            defaults[PARAM_TARGET_SHIELD_THRESHOLD] = targetShieldDefaultPercent().toString()
        "avoid_shield_flux_threshold" ->
            defaults[PARAM_TARGET_SHIELD_THRESHOLD] = avoidShieldDefaultPercent().toString()
        "target_shield_flux_threshold" ->
            defaults[PARAM_TARGET_SHIELD_THRESHOLD] = targetShieldDefaultPercent().toString()
        "ship_low_shield_flux_threshold" ->
            defaults[PARAM_THRESHOLD] = settingsPercent(Settings.shieldOffThreshold()).toString()
        "ship_vent" -> {
            defaults[PARAM_THRESHOLD] = settingsPercent(Settings.ventFluxThreshold()).toString()
            defaults[EditableShipModeDefinitions.PARAM_VENT_SAFETY_FACTOR] =
                formatDecimalTagNumber(Settings.ventSafetyFactor().coerceIn(0.1f, 10f))
        }
        "opportunist_tuning" -> {
            defaults[PARAM_KINETIC_THRESHOLD] = opportunistKineticDefaultPercent().toString()
            defaults[PARAM_HIGH_EXPLOSIVE_THRESHOLD] = opportunistHighExplosiveDefaultPercent().toString()
            defaults[PARAM_TRIGGER_HAPPINESS] = opportunistTriggerDefaultPercent().toString()
        }
    }
    if (definition.parameters.any { it.id == PARAM_PRIORITY_MULTIPLIER }) {
        defaults[PARAM_PRIORITY_MULTIPLIER] = priorityMultiplierDefault().toString()
    }
    return defaults
}
