package com.dp.advancedgunnerycontrol.gui.customlists.edit

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.*


import com.fs.starfarer.api.ui.CustomPanelAPI
import java.util.Locale

internal data class CustomTagEditModalCallbacks(
    val renderTitle: (CustomPanelAPI, Float, String) -> Unit,
    val updateDraftValue: (String, String) -> Unit,
    val cycleDraftChoice: (EditableWeaponTagDefinition, ChoiceParameter, Int) -> Unit,
    val resetDraftValuesToDefaults: (EditableWeaponTagDefinition) -> Unit,
    val confirm: (CustomTagEditRenderState) -> Unit,
    val copyWeaponTag: (String) -> Unit,
    val deleteWeaponTag: (String) -> Unit,
    val copyShipMode: (String) -> Unit,
    val deleteShipMode: (String) -> Unit,
    val cancelToManager: () -> Unit,
    val closeModal: () -> Unit,
)

internal object CustomTagEditModalRenderer {
    fun render(
        dialog: CustomPanelAPI,
        dialogWidth: Float,
        dialogHeight: Float,
        state: CustomTagEditRenderState,
        isShipModeEdit: Boolean,
        buttons: MutableList<ButtonBase<*>>,
        callbacks: CustomTagEditModalCallbacks,
    ) {
        callbacks.renderTitle(
            dialog,
            dialogWidth,
            if (isShipModeEdit) "Edit Ship Mode" else "Edit Tag"
        )

        var y = CampaignGuiStyle.MODAL_PADDING +
            CampaignGuiStyle.MODAL_HEADING_HEIGHT +
            CampaignGuiStyle.MODAL_TITLE_BODY_GAP
        val widthMultiplier = CustomTagEditRowRenderer.widthMultiplierForDialogWidth(dialogWidth)
        CustomTagEditRowRenderer.renderDisplay(
            dialog,
            y,
            "Preview",
            state.preview,
            CampaignGuiStyle.MODIFIER_TEXT_COLOUR,
            widthMultiplier = widthMultiplier,
        )
        y += CampaignGuiStyle.MODAL_ROW_HEIGHT + CampaignGuiStyle.MODAL_ROW_GAP
        renderParameterRows(dialog, state, y, widthMultiplier, buttons, callbacks)
        renderFooterButtons(dialog, dialogWidth, dialogHeight, state, isShipModeEdit, buttons, callbacks)
    }

    private fun renderParameterRows(
        dialog: CustomPanelAPI,
        state: CustomTagEditRenderState,
        startY: Float,
        widthMultiplier: Float,
        buttons: MutableList<ButtonBase<*>>,
        callbacks: CustomTagEditModalCallbacks,
    ) {
        var y = startY
        val definition = state.definition ?: return
        EditableWeaponTagDefinitions.visibleParameters(definition, state.draftValues).forEach { parameter ->
            val disabledReason = disabledEditParameterReason(state.draftValues, parameter)
            val tooltip = editParameterTooltip(definition, parameter, disabledReason)
            when (parameter) {
                is ChoiceParameter -> {
                    val current = state.draftValues[parameter.id] ?: parameter.defaultOptionId
                    val label = parameter.options.firstOrNull { it.id == current }?.label ?: current
                    CustomTagEditRowRenderer.addBidirectionalMomentaryRow(
                        dialog = dialog,
                        y = y,
                        leftLabel = parameter.label,
                        buttonText = "$label [$current]",
                        kind = CampaignActionButtonKind.UNCOLOURED,
                        highlightTokens = listOf("[$current]"),
                        tooltip = tooltip ?: "Cycle ${parameter.label}. Left-click for next; right-click for previous.",
                        widthMultiplier = widthMultiplier,
                        buttons = buttons,
                        onLeftClick = {
                            callbacks.cycleDraftChoice(definition, parameter, 1)
                        },
                        onRightClick = {
                            callbacks.cycleDraftChoice(definition, parameter, -1)
                        },
                    )
                    y += CampaignGuiStyle.MODAL_ROW_HEIGHT + CampaignGuiStyle.MODAL_ROW_GAP
                }
                is NumberParameter -> {
                    renderIncrementor(
                        dialog,
                        definition,
                        parameter,
                        state.draftValues,
                        y,
                        tooltip,
                        disabledReason == null,
                        widthMultiplier,
                        buttons,
                        callbacks,
                    )
                    y += CampaignGuiStyle.MODAL_ROW_HEIGHT + CampaignGuiStyle.MODAL_ROW_GAP
                }
                is DecimalParameter -> {
                    renderDecimalIncrementor(
                        dialog,
                        definition,
                        parameter,
                        state.draftValues,
                        y,
                        tooltip,
                        disabledReason == null,
                        widthMultiplier,
                        buttons,
                        callbacks,
                    )
                    y += CampaignGuiStyle.MODAL_ROW_HEIGHT + CampaignGuiStyle.MODAL_ROW_GAP
                }
                is ToggleParameter -> {
                    val enabled = state.draftValues[parameter.id]?.toBooleanStrictOrNull() ?: parameter.defaultValue
                    val rowEnabled = disabledReason == null
                    CustomTagEditRowRenderer.addMomentaryRow(
                        dialog = dialog,
                        y = y,
                        leftLabel = parameter.label,
                        buttonText = if (enabled) "On" else "Off",
                        kind = if (enabled && rowEnabled) CampaignActionButtonKind.ACTIVE else CampaignActionButtonKind.UNCOLOURED,
                        buttons = buttons,
                        tooltip = tooltip ?: "Toggle ${parameter.label}.",
                        widthMultiplier = widthMultiplier,
                        enabled = rowEnabled,
                    ) {
                        callbacks.updateDraftValue(parameter.id, (!enabled).toString())
                    }
                    y += CampaignGuiStyle.MODAL_ROW_HEIGHT + CampaignGuiStyle.MODAL_ROW_GAP
                }
                is TextParameter -> {
                    val value = state.draftValues[parameter.id] ?: parameter.defaultValue
                    CustomTagEditRowRenderer.renderDisplay(
                        dialog,
                        y,
                        parameter.label,
                        "$value (text editing pending)",
                        widthMultiplier = widthMultiplier,
                    )
                    y += CampaignGuiStyle.MODAL_ROW_HEIGHT + CampaignGuiStyle.MODAL_ROW_GAP
                }
            }
        }
    }

    private fun renderFooterButtons(
        dialog: CustomPanelAPI,
        dialogWidth: Float,
        dialogHeight: Float,
        state: CustomTagEditRenderState,
        isShipModeEdit: Boolean,
        buttons: MutableList<ButtonBase<*>>,
        callbacks: CustomTagEditModalCallbacks,
    ) {
        val confirmSpec = confirmSpec(state, callbacks)
        val specs = when {
            state.isManagerEdit -> managerFooterSpecs(state, isShipModeEdit, confirmSpec, callbacks)
            isShipModeEdit -> shipModeFooterSpecs(state, confirmSpec, callbacks)
            else -> weaponTagFooterSpecs(state, confirmSpec, callbacks)
        }
        CustomListModalFooterRenderer.addEqualWidthButtons(
            dialog = dialog,
            dialogWidth = dialogWidth,
            dialogHeight = dialogHeight,
            buttons = buttons,
            specs = specs,
        )
    }

    private fun confirmSpec(
        state: CustomTagEditRenderState,
        callbacks: CustomTagEditModalCallbacks,
    ): ModalFooterButtonSpec {
        return ModalFooterButtonSpec(
            data = "custom_tag_edit_confirm",
            kind = CampaignActionButtonKind.CONFIRM,
            enabled = state.confirmAvailability.enabled,
            labelText = "Confirm",
            tooltip = state.confirmAvailability.tooltip,
            showTooltipWhileInactive = true,
        ) {
            callbacks.confirm(state)
        }
    }

    private fun managerFooterSpecs(
        state: CustomTagEditRenderState,
        isShipModeEdit: Boolean,
        confirmSpec: ModalFooterButtonSpec,
        callbacks: CustomTagEditModalCallbacks,
    ): List<ModalFooterButtonSpec> {
        return listOf(
            confirmSpec,
            defaultFooterSpec(state, isShipModeEdit, callbacks),
            ModalFooterButtonSpec(
                data = "custom_tag_edit_cancel",
                kind = CampaignActionButtonKind.CANCEL,
                labelText = "Cancel",
            ) { callbacks.cancelToManager() },
        )
    }

    private fun shipModeFooterSpecs(
        state: CustomTagEditRenderState,
        confirmSpec: ModalFooterButtonSpec,
        callbacks: CustomTagEditModalCallbacks,
    ): List<ModalFooterButtonSpec> {
        return listOf(
            confirmSpec,
            ModalFooterButtonSpec(
                data = "custom_ship_mode_edit_copy",
                kind = CampaignActionButtonKind.SAVE,
                enabled = state.copyAvailability.enabled,
                labelText = "Copy",
                tooltip = state.copyAvailability.tooltip,
                showTooltipWhileInactive = true,
            ) {
                state.canonicalTag?.let(callbacks.copyShipMode)
            },
            defaultFooterSpec(state, isShipModeEdit = true, callbacks),
            ModalFooterButtonSpec(
                data = "custom_ship_mode_edit_delete",
                kind = CampaignActionButtonKind.LOAD,
                enabled = state.deleteAvailability.enabled,
                labelText = "Delete",
                tooltip = state.deleteAvailability.tooltip,
                showTooltipWhileInactive = true,
            ) {
                state.editSourceTag?.let(callbacks.deleteShipMode)
            },
            ModalFooterButtonSpec(
                data = "custom_ship_mode_edit_cancel",
                kind = CampaignActionButtonKind.CANCEL,
                labelText = "Cancel",
            ) { callbacks.closeModal() },
        )
    }

    private fun weaponTagFooterSpecs(
        state: CustomTagEditRenderState,
        confirmSpec: ModalFooterButtonSpec,
        callbacks: CustomTagEditModalCallbacks,
    ): List<ModalFooterButtonSpec> {
        return listOf(
            confirmSpec,
            ModalFooterButtonSpec(
                data = "custom_tag_edit_copy",
                kind = CampaignActionButtonKind.SAVE,
                enabled = state.copyAvailability.enabled,
                labelText = "Copy",
                tooltip = state.copyAvailability.tooltip,
                showTooltipWhileInactive = true,
            ) {
                state.canonicalTag?.let(callbacks.copyWeaponTag)
            },
            defaultFooterSpec(state, isShipModeEdit = false, callbacks),
            ModalFooterButtonSpec(
                data = "custom_tag_edit_delete",
                kind = CampaignActionButtonKind.LOAD,
                enabled = state.deleteAvailability.enabled,
                labelText = "Delete",
                tooltip = state.deleteAvailability.tooltip,
                showTooltipWhileInactive = true,
            ) {
                state.editSource?.second?.let(callbacks.deleteWeaponTag)
            },
            ModalFooterButtonSpec(
                data = "custom_tag_edit_cancel",
                kind = CampaignActionButtonKind.CANCEL,
                labelText = "Cancel",
            ) { callbacks.closeModal() },
        )
    }

    private fun defaultFooterSpec(
        state: CustomTagEditRenderState,
        isShipModeEdit: Boolean,
        callbacks: CustomTagEditModalCallbacks,
    ): ModalFooterButtonSpec {
        return ModalFooterButtonSpec(
            data = if (isShipModeEdit) "custom_ship_mode_edit_default" else "custom_tag_edit_default",
            kind = CampaignActionButtonKind.UNCOLOURED,
            enabled = state.definition != null,
            labelText = "Default",
            tooltip = "Reset editable values to this tag or ship mode's defaults.",
            showTooltipWhileInactive = true,
        ) {
            state.definition?.let(callbacks.resetDraftValuesToDefaults)
        }
    }

    private fun renderIncrementor(
        dialog: CustomPanelAPI,
        definition: EditableWeaponTagDefinition,
        parameter: NumberParameter,
        draftValues: Map<String, String>,
        y: Float,
        tooltip: String?,
        enabled: Boolean,
        widthMultiplier: Float,
        buttons: MutableList<ButtonBase<*>>,
        callbacks: CustomTagEditModalCallbacks,
    ) {
        val current = draftValues[parameter.id]?.toIntOrNull() ?: parameter.defaultValue
        val isPriorityMultiplier = parameter.id == EditableWeaponTagDefinitions.PARAM_PRIORITY_MULTIPLIER
        val label = "${parameter.label}: $current${parameter.suffix}"
        CustomTagEditRowRenderer.renderDeltaButtons(
            dialog = dialog,
            y = y,
            leftLabel = label,
            dataPrefix = "custom_tag_number:${definition.id}:${parameter.id}",
            tooltipLabel = parameter.label,
            tooltip = tooltip,
            buttons = buttons,
            widthMultiplier = widthMultiplier,
            deltas = if (isPriorityMultiplier) listOf(-1000, -100, 100, 1000) else listOf(-10, -1, 1, 10),
            enabled = enabled,
        ) { delta ->
            callbacks.updateDraftValue(parameter.id, (current + delta).coerceIn(parameter.minValue, parameter.maxValue).toString())
        }
    }

    private fun renderDecimalIncrementor(
        dialog: CustomPanelAPI,
        definition: EditableWeaponTagDefinition,
        parameter: DecimalParameter,
        draftValues: Map<String, String>,
        y: Float,
        tooltip: String?,
        enabled: Boolean,
        widthMultiplier: Float,
        buttons: MutableList<ButtonBase<*>>,
        callbacks: CustomTagEditModalCallbacks,
    ) {
        val current = draftValues[parameter.id]?.toFloatOrNull() ?: parameter.defaultValue
        CustomTagEditRowRenderer.renderDecimalDeltaButtons(
            dialog = dialog,
            y = y,
            leftLabel = "${parameter.label}: ${formatDecimalParameterValue(current, parameter)}${parameter.suffix}",
            dataPrefix = "custom_tag_decimal:${definition.id}:${parameter.id}",
            tooltipLabel = parameter.label,
            tooltip = tooltip,
            buttons = buttons,
            widthMultiplier = widthMultiplier,
            minorStepLabel = formatDecimalParameterValue(parameter.minorStep, parameter),
            majorStepLabel = formatDecimalParameterValue(parameter.majorStep, parameter),
            minorStep = parameter.minorStep,
            majorStep = parameter.majorStep,
            enabled = enabled,
        ) { delta ->
            val updated = (current + delta).coerceIn(parameter.minValue, parameter.maxValue)
            callbacks.updateDraftValue(parameter.id, formatDecimalParameterValue(updated, parameter))
        }
    }

    private fun editParameterTooltip(
        definition: EditableWeaponTagDefinition,
        parameter: EditableTagParameterDefinition,
        disabledReason: String?,
    ): String? {
        val base = editableTagParameterTooltip(definition, parameter)
        return listOfNotNull(base, disabledReason).takeIf { it.isNotEmpty() }?.joinToString("\n")
    }

    private fun disabledEditParameterReason(
        draftValues: Map<String, String>,
        parameter: EditableTagParameterDefinition,
    ): String? {
        if (parameter.id == EditableWeaponTagDefinitions.PARAM_IGNORE_IF_BEAMED) {
            val metric = draftValues[EditableWeaponTagDefinitions.PARAM_FLUX_METRIC] ?: "TF"
            return if (metric == "SF") null else "Only available for soft-flux HoldFire."
        }
        if (parameter.id == EditableWeaponTagDefinitions.PARAM_BEAM_WINDOW) {
            val metric = draftValues[EditableWeaponTagDefinitions.PARAM_FLUX_METRIC] ?: "TF"
            if (metric != "SF") return "Only available for soft-flux HoldFire."
            val enabled = draftValues[EditableWeaponTagDefinitions.PARAM_IGNORE_IF_BEAMED]
                ?.toBooleanStrictOrNull() == true
            return if (enabled) null else "Enable Ignore if beamed to edit this value."
        }
        val thresholdIndex = parameter.id
            .takeIf { it.startsWith("threshold") && it.length > "threshold".length }
            ?.removePrefix("threshold")
            ?.toIntOrNull()
            ?: return null
        val enabledId = "enabledThreshold$thresholdIndex"
        val enabled = draftValues[enabledId]?.toBooleanStrictOrNull() ?: true
        return if (enabled) null else "Enable threshold $thresholdIndex to edit this value."
    }

    private fun editableTagParameterTooltip(
        definition: EditableWeaponTagDefinition,
        parameter: EditableTagParameterDefinition,
    ): String? {
        shipModeParameterTooltip(definition, parameter)?.let { return it }
        return when (parameter.id) {
            EditableWeaponTagDefinitions.PARAM_FLUX_METRIC ->
                "Choose total, soft, or hard flux to check."
            EditableWeaponTagDefinitions.PARAM_THRESHOLD ->
                editableTagThresholdTooltip(definition)
            EditableWeaponTagDefinitions.PARAM_TARGET_SHIELD_THRESHOLD ->
                editableTagTargetShieldThresholdTooltip(definition)
            EditableWeaponTagDefinitions.PARAM_TOTAL_FLUX_CAP ->
                "Safety cap: this tag stops applying above this total flux."
            EditableWeaponTagDefinitions.PARAM_PRIORITY_MULTIPLIER ->
                "Higher values make matching targets more preferred."
            EditableWeaponTagDefinitions.PARAM_KINETIC_THRESHOLD ->
                "Kinetic shots prefer shields above this shield factor. Values are percentages; 50 means 0.50."
            EditableWeaponTagDefinitions.PARAM_HIGH_EXPLOSIVE_THRESHOLD ->
                "HE/frag shots prefer shields below this shield factor. Values are percentages; 15 means 0.15."
            EditableWeaponTagDefinitions.PARAM_TRIGGER_HAPPINESS ->
                "Higher values make Opportunist more willing to fire."
            EditableWeaponTagDefinitions.PARAM_CLEANUP_DAMAGE_CAP ->
                "Ignore AvoidPD waste checks for weapons at or below this estimated attack-packet damage."
            EditableWeaponTagDefinitions.PARAM_IGNORE_IF_BEAMED ->
                "Soft-flux hold is ignored briefly after this ship is hit by an enemy beam. Max TF still applies."
            EditableWeaponTagDefinitions.PARAM_BEAM_WINDOW ->
                "How long the beam exception stays active after the last enemy beam hit."
            EditableWeaponTagDefinitions.PARAM_IGNORE_KINETIC_WEAPONS ->
                "When enabled, this tag does not affect kinetic weapons."
            EditableWeaponTagDefinitions.PARAM_IGNORE_HIGH_EXPLOSIVE_WEAPONS ->
                "When enabled, this tag does not affect high-explosive weapons."
            EditableWeaponTagDefinitions.PARAM_IGNORE_FRAGMENTATION_WEAPONS ->
                "When enabled, this tag does not affect fragmentation weapons."
            EditableWeaponTagDefinitions.PARAM_IGNORE_ENERGY_WEAPONS ->
                "When enabled, this tag does not affect energy weapons."
            EditableWeaponTagDefinitions.PARAM_IGNORE_BEAM_WEAPONS ->
                "When enabled, this tag does not affect beam or burst-beam weapons."
            EditableWeaponTagDefinitions.PARAM_IGNORE_MISSILE_WEAPONS ->
                "When enabled, this tag does not affect missile-slot weapons."
            EditableWeaponTagDefinitions.PARAM_IGNORE_PROJECTILE_WEAPONS ->
                "When enabled, this tag does not affect non-beam, non-missile projectile weapons."
            EditableWeaponTagDefinitions.PARAM_REQUIRE_SHIP_TARGET ->
                "When enabled, this tag only works while the ship has an active ship target."
            EditableWeaponTagDefinitions.PARAM_TRIGGER_AMMO_FEEDER ->
                syncSystemTriggerTooltip("Accelerated Ammo Feeder")
            EditableWeaponTagDefinitions.PARAM_TRIGGER_HIGH_ENERGY_FOCUS ->
                syncSystemTriggerTooltip("High Energy Focus")
            EditableWeaponTagDefinitions.PARAM_TRIGGER_LIDAR_ARRAY ->
                "Triggers Lidar Array as the volley is prepared. Volleys can still fire if Lidar Array is unavailable."
            EditableWeaponTagDefinitions.PARAM_TRIGGER_TEMPORAL_SHELL ->
                syncSystemTriggerTooltip("Temporal Shell")
            EditableWeaponTagDefinitions.PARAM_TRIGGER_ENTROPY_AMPLIFIER ->
                "Triggers Entropy Amplifier just before a volley at a ship target. Volleys can still fire if it is unavailable."
            else -> fallbackEditParameterTooltip(parameter)
        }
    }

    private fun shipModeParameterTooltip(
        definition: EditableWeaponTagDefinition,
        parameter: EditableTagParameterDefinition,
    ): String? = when (definition.id) {
        "ship_personality_override" -> "Choose the AI personality to use."
        "ship_low_shield_flux_threshold" -> "Lower shields when flux is above this percent."
        "ship_shield_up_flux_threshold" -> "Raise shields when flux is below this percent."
        "ship_shield_up_plus_flux_threshold" -> "Keep shields raised while flux is below this percent."
        "ship_vent" -> when (parameter.id) {
            EditableShipModeDefinitions.PARAM_THRESHOLD -> "Vent when flux is above this percent."
            "safetyFactor" -> "Higher values require safer venting opportunities."
            "aggressive" -> "Vent more aggressively and back off less while venting."
            else -> null
        }
        "ship_cr_retreat_thresholds" -> multiThresholdShipModeTooltip(parameter, "CR")
        "ship_hull_retreat_thresholds" -> multiThresholdShipModeTooltip(parameter, "hull")
        else -> null
    }

    private fun multiThresholdShipModeTooltip(
        parameter: EditableTagParameterDefinition,
        metric: String,
    ): String? = when {
        parameter.id.startsWith("enabledThreshold") -> "Toggle this $metric retreat threshold."
        parameter.id.startsWith("threshold") -> "Retreat when $metric falls below this percent."
        parameter.id == "directRetreat" -> "Order retreat directly when any enabled threshold is reached."
        else -> null
    }

    private fun fallbackEditParameterTooltip(parameter: EditableTagParameterDefinition): String? = when (parameter) {
        is ChoiceParameter -> "Choose ${parameter.label}."
        is ToggleParameter -> "Toggle ${parameter.label}."
        is NumberParameter -> "Adjust ${parameter.label}."
        is DecimalParameter -> "Adjust ${parameter.label}."
        is TextParameter -> null
    }

    private fun syncSystemTriggerTooltip(systemName: String): String {
        return "Triggers $systemName just before the synchronised volley fires. Volleys can still fire if $systemName is unavailable."
    }

    private fun editableTagThresholdTooltip(definition: EditableWeaponTagDefinition): String? = when (definition.id) {
        "hold_fire_flux_threshold" -> "Stops firing above this flux percent."
        "force_fire_flux_threshold" -> "Forces firing below this flux percent."
        "avoid_shield_flux_threshold" -> "Avoids shield targets above this flux percent."
        "target_shield_flux_threshold" -> "Prefers shield targets above this flux percent."
        "pd_flux_threshold" -> "PD mode activates above this flux percent."
        "opportunist_ammo_threshold" -> "Use Opportunist behaviour when ammo is below this percent."
        "pd_ammo_threshold" -> "Use PD-only targeting when ammo is below this percent."
        "no_pd_waste_threshold" -> "Avoids PD targets above this damage-waste percent."
        "no_pd_health_threshold" -> "Avoids PD targets below this hitpoint value."
        "avoid_armor_threshold" -> "Minimum armour effectiveness needed to fire at armour."
        "panic_hull_threshold" -> "Panic firing starts below this hull percent."
        "range_threshold" -> "Only targets within this percent of weapon range."
        "low_rof_threshold" -> "Higher values reduce rate of fire more."
        else -> null
    }

    private fun editableTagTargetShieldThresholdTooltip(definition: EditableWeaponTagDefinition): String? = when {
        definition.family == "AvoidShield" -> "Require target shield factor below this percent."
        definition.family == "TargetShield" -> "Require target shield factor above this percent."
        else -> "Adjust the target shield-factor threshold."
    }

    private fun formatDecimalParameterValue(value: Float, parameter: DecimalParameter): String {
        var rendered = String.format(Locale.US, "%.${parameter.decimalPlaces}f", value)
        if (rendered.contains('.')) {
            while (rendered.endsWith("0")) {
                rendered = rendered.dropLast(1)
            }
            if (rendered.endsWith(".")) {
                rendered = rendered.dropLast(1)
            }
        }
        return rendered
    }
}
