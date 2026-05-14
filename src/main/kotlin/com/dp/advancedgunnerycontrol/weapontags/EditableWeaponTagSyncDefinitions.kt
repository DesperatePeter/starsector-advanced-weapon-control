package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_REQUIRE_SHIP_TARGET
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_TRIGGER_AMMO_FEEDER
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_TRIGGER_ENTROPY_AMPLIFIER
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_TRIGGER_HIGH_ENERGY_FOCUS
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_TRIGGER_LIDAR_ARRAY
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_TRIGGER_TEMPORAL_SHELL

internal object EditableWeaponTagSyncDefinitions {
    fun definitions(): List<EditableWeaponTagDefinition> = listOf(
        syncTargetGateDefinition(
            id = "sync_window_ship_target",
            family = "SyncWindow",
        ),
        syncTargetGateDefinition(
            id = "sync_volley_ship_target",
            family = "SyncVolley",
        ),
        syncTargetGateDefinition(
            id = "ambush_ship_target",
            family = "Ambush",
        )
    )

    private fun syncTargetGateDefinition(
        id: String,
        family: String,
    ): EditableWeaponTagDefinition {
        val triggerParameters = listOf(
            SyncSystemTriggerOption.ACCELERATED_AMMO_FEEDER to PARAM_TRIGGER_AMMO_FEEDER,
            SyncSystemTriggerOption.HIGH_ENERGY_FOCUS to PARAM_TRIGGER_HIGH_ENERGY_FOCUS,
            SyncSystemTriggerOption.LIDAR_ARRAY to PARAM_TRIGGER_LIDAR_ARRAY,
            SyncSystemTriggerOption.TEMPORAL_SHELL to PARAM_TRIGGER_TEMPORAL_SHELL,
            SyncSystemTriggerOption.ENTROPY_AMPLIFIER to PARAM_TRIGGER_ENTROPY_AMPLIFIER,
        )
        val parameters = listOf(
            ToggleParameter(
                id = PARAM_REQUIRE_SHIP_TARGET,
                label = "Require Active Ship Target",
                defaultValue = false
            )
        ) + triggerParameters.map { (trigger, parameterId) ->
            ToggleParameter(
                id = parameterId,
                label = "Trigger ${trigger.label}",
                defaultValue = false,
                markHiddenChange = true
            )
        }
        val defaults = buildMap {
            put(PARAM_REQUIRE_SHIP_TARGET, "false")
            triggerParameters.forEach { (_, parameterId) -> put(parameterId, "false") }
        }

        return EditableWeaponTagDefinition(
            id = id,
            family = family,
            templateTag = family,
            acceptedTemplateTags = setOf(family, "$family(X)"),
            parameters = parameters,
            defaultValues = defaults,
            parser = parser@{ canonicalTag ->
                val options = parseSyncTagOptions(canonicalTag)
                    ?.takeIf { it.family == family }
                    ?: return@parser null
                ParsedEditableWeaponTag(
                    canonicalTag = options.canonicalTag(),
                    definitionId = id,
                    family = family,
                    templateTag = family,
                    parameterValues = buildMap {
                        put(PARAM_REQUIRE_SHIP_TARGET, options.requireShipTarget.toString())
                        triggerParameters.forEach { (trigger, parameterId) ->
                            put(parameterId, (trigger in options.systemTriggers).toString())
                        }
                    },
                    exclusivityKeys = setOf(family)
                )
            },
            builder = { values ->
                val requireShipTarget = values[PARAM_REQUIRE_SHIP_TARGET]?.toBooleanStrictOrNull() ?: false
                val triggers = triggerParameters
                    .filter { (_, parameterId) -> values[parameterId]?.toBooleanStrictOrNull() ?: false }
                    .map { (trigger, _) -> trigger }
                    .toSet()
                EditableTagBuildResult(
                    canonicalTag = SyncTagOptions(family, requireShipTarget, triggers).canonicalTag(),
                    errors = emptyList()
                )
            }
        )
    }
}
