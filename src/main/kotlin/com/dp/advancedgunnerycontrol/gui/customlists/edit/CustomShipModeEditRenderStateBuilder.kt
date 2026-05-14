package com.dp.advancedgunnerycontrol.gui.customlists.edit

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

import com.dp.advancedgunnerycontrol.gui.*


import com.dp.advancedgunnerycontrol.settings.Settings

internal object CustomShipModeEditRenderStateBuilder {
    fun build(
        definition: EditableWeaponTagDefinition?,
        draftValues: Map<String, String>,
        currentModes: List<String>,
        loadModes: (Int) -> List<String>,
        editSourceGroupIndex: Int?,
        editSourceValue: String?,
        modalState: CustomListModalStateController,
    ): CustomTagEditRenderState? {
        if (definition == null) return null
        val buildResult = definition.buildCanonicalTag(draftValues)
        val preview = buildResult.canonicalTag?.let(::shipModeDisplayName)
            ?: buildResult.errors.firstOrNull()?.message
            ?: shipModeDisplayName(definition.templateTag)
        val canonicalMode = buildResult.canonicalTag
        val isManagerEdit = modalState.isManagerReturnEdit()
        val sourceMode = if (isManagerEdit) {
            modalState.managerEditSourceTag(editSourceGroupIndex, editSourceValue)
        } else {
            editSourceValue?.takeIf { it.isNotBlank() }?.let(::canonicalizeShipModeName)
        }
        val sourcePendingAddition = modalState.managerEditIsPendingAddition()
        val existingModes = existingModes(
            currentModes = currentModes,
            sourceMode = sourceMode,
            modalState = modalState,
        )
        val sourceState = CustomTagEditSourceState(
            canonicalTag = sourceMode,
            inCustomList = sourceMode != null && sourceMode in canonicalizeShipModeNames(currentModes),
            activeOnShip = sourceMode != null && isActiveOnShip(sourceMode, loadModes),
        )
        return CustomTagEditRenderState(
            definition = definition,
            fixedTagEdit = false,
            draftValues = draftValues,
            preview = preview,
            canonicalTag = canonicalMode,
            isValid = buildResult.isValid,
            editSource = null,
            isManagerEdit = isManagerEdit,
            managerEditSource = sourceMode,
            managerEditSourceIsPendingAddition = sourcePendingAddition,
            editSourceTag = sourceMode,
            existingTags = existingModes,
            confirmAvailability = availability(preview, canonicalMode, buildResult.isValid, sourceState, existingModes, CustomTagEditAction.CONFIRM),
            copyAvailability = availability(preview, canonicalMode, buildResult.isValid, sourceState, existingModes, CustomTagEditAction.COPY),
            deleteAvailability = availability(preview, canonicalMode, buildResult.isValid, sourceState, existingModes, CustomTagEditAction.DELETE),
        )
    }

    private fun existingModes(
        currentModes: List<String>,
        sourceMode: String?,
        modalState: CustomListModalStateController,
    ): Set<String> {
        val editSources = modalState.shipModeEdits().keys
        val stagedModes = modalState.stagedShipModes(
            currentModes = currentModes,
            pendingAdditions = modalState.shipModeAdditions(),
            pendingRemovals = modalState.shipModeRemovals(),
            pendingEdits = modalState.shipModeEdits(),
        )
        return canonicalizeShipModeNames(
            stagedModes.filterNot { mode ->
                mode == sourceMode ||
                    mode in editSources
            }
        ).toSet()
    }

    private fun isActiveOnShip(
        mode: String,
        loadModes: (Int) -> List<String>,
    ): Boolean {
        val canonicalMode = canonicalizeShipModeName(mode)
        for (loadoutIndex in 0 until Settings.maxLoadouts()) {
            if (canonicalMode in canonicalizeShipModeNames(loadModes(loadoutIndex))) return true
        }
        return false
    }

    private fun availability(
        preview: String,
        canonicalMode: String?,
        isValid: Boolean,
        sourceState: CustomTagEditSourceState,
        existingModes: Set<String>,
        action: CustomTagEditAction,
    ): CustomTagEditAvailability {
        val sourceMode = sourceState.canonicalTag
        if (!isValid || canonicalMode == null) {
            return CustomTagEditAvailability(enabled = false, tooltip = preview)
        }
        val displayMode = shipModeDisplayName(canonicalMode)
        val displaySource = sourceMode?.let(::shipModeDisplayName)
        if (action == CustomTagEditAction.DELETE) {
            return if (sourceMode == null) {
                CustomTagEditAvailability(enabled = false, tooltip = "Delete is only available when editing an existing custom ship mode.")
            } else if (!sourceState.inCustomList) {
                CustomTagEditAvailability(enabled = false, tooltip = "$displaySource is not in the current Custom ship-mode list.")
            } else {
                CustomTagEditAvailability(enabled = true, tooltip = "Remove $displaySource from the current Custom ship-mode list.")
            }
        }
        if (sourceMode != null && canonicalMode == sourceMode) {
            return CustomTagEditAvailability(enabled = false, tooltip = "$displayMode is unchanged.")
        }
        if (canonicalMode in existingModes) {
            return CustomTagEditAvailability(enabled = false, tooltip = "$displayMode is already in the current Custom ship-mode list.")
        }
        return when (action) {
            CustomTagEditAction.CONFIRM -> CustomTagEditAvailability(
                enabled = true,
                tooltip = if (sourceMode == null) {
                    "Add $displayMode to the current Custom ship-mode list."
                } else {
                    "Replace $displaySource with $displayMode in the current Custom ship-mode list and saved ship-mode selections."
                },
            )
            CustomTagEditAction.COPY -> CustomTagEditAvailability(
                enabled = sourceMode != null,
                tooltip = if (sourceMode == null) {
                    "Copy is only available when editing an existing custom ship mode."
                } else {
                    "Add $displayMode as a new custom ship mode without changing $displaySource."
                },
            )
            CustomTagEditAction.DELETE -> CustomTagEditAvailability(enabled = false, tooltip = "")
        }
    }
}
