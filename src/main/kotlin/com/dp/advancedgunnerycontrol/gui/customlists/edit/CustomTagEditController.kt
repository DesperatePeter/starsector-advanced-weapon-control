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

import com.dp.advancedgunnerycontrol.gui.*

import com.dp.advancedgunnerycontrol.gui.session.CustomListModalBindings

import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI

internal class CustomTagEditController(
    private val state: CustomListModalStateController,
    private val contextController: CustomListContextController,
    private val bindings: CustomListModalBindings,
    private val buttons: MutableList<ButtonBase<*>>,
    private val activePersistenceContext: () -> ShipEditorPersistenceContext?,
    private val renderTitle: (CustomPanelAPI, Float, String) -> Unit,
    private val closeModal: () -> Unit,
    private val addCustomTag: (FleetMemberAPI, ShipEditorPersistenceContext, String, Boolean) -> Unit,
    private val replaceCustomTag: (FleetMemberAPI, ShipEditorPersistenceContext, String, String) -> Unit,
    private val removeCustomTags: (FleetMemberAPI, ShipEditorPersistenceContext, Set<String>) -> Unit,
    private val addCustomShipMode: (ShipEditorPersistenceContext, String) -> Unit,
    private val replaceCustomShipMode: (ShipEditorPersistenceContext, String, String) -> Unit,
    private val removeCustomShipModes: (ShipEditorPersistenceContext, Set<String>) -> Unit,
) {
    fun renderParameterEditor(
        dialog: CustomPanelAPI,
        ship: FleetMemberAPI,
        context: ShipEditorPersistenceContext,
        dialogWidth: Float,
        dialogHeight: Float,
    ) {
        val renderState = currentRenderState(context)
        if (renderState == null) {
            state.returnToManagerFromEdit()
            return
        }

        CustomTagEditModalRenderer.render(
            dialog = dialog,
            dialogWidth = dialogWidth,
            dialogHeight = dialogHeight,
            state = renderState,
            isShipModeEdit = state.isShipModeEdit(),
            buttons = buttons,
            callbacks = callbacks(ship, context),
        )
    }

    fun currentParameterCount(): Int = currentDraftDefinition()?.parameters?.size ?: 0

    fun startManagerEdit(definition: EditableWeaponTagDefinition) {
        state.beginManagerEdit(definition)
    }

    fun startManagerShipModeEdit(definition: EditableWeaponTagDefinition) {
        state.beginManagerEditShipMode(definition)
    }

    fun returnToManagerFromEdit() {
        state.returnToManagerFromEdit()
    }

    private fun callbacks(
        ship: FleetMemberAPI,
        context: ShipEditorPersistenceContext,
    ): CustomTagEditModalCallbacks {
        return CustomTagEditModalCallbacks(
            renderTitle = { titleDialog, width, title ->
                renderTitle(titleDialog, width, title)
            },
            updateDraftValue = ::updateDraftValue,
            cycleDraftChoice = ::cycleDraftChoice,
            resetDraftValuesToDefaults = state::resetDraftValuesToDefaults,
            confirm = { renderState -> confirm(ship, context, renderState) },
            copyWeaponTag = { tag -> addCustomTag(ship, context, tag, false) },
            deleteWeaponTag = { sourceTag -> removeCustomTags(ship, context, setOf(sourceTag)) },
            copyShipMode = { mode -> addCustomShipMode(context, mode) },
            deleteShipMode = { mode -> removeCustomShipModes(context, setOf(mode)) },
            cancelToManager = ::returnToManagerFromEdit,
            closeModal = closeModal,
        )
    }

    private fun confirm(
        ship: FleetMemberAPI,
        context: ShipEditorPersistenceContext,
        renderState: CustomTagEditRenderState,
    ) {
        val tag = renderState.canonicalTag ?: return
        if (renderState.isManagerEdit && state.isShipModeEdit()) {
            when {
                renderState.managerEditSource == null -> state.stageShipModeAddition(
                    tag,
                    contextController.currentShipModesForActiveContext(activePersistenceContext()),
                )
                renderState.managerEditSourceIsPendingAddition -> state.replaceShipModeAddition(
                    renderState.managerEditSource,
                    tag,
                )
                else -> state.stageShipModeEdit(renderState.managerEditSource, tag)
            }
            returnToManagerFromEdit()
        } else if (state.isShipModeEdit()) {
            if (renderState.editSourceTag == null) {
                addCustomShipMode(context, tag)
            } else {
                replaceCustomShipMode(context, renderState.editSourceTag, tag)
            }
        } else if (renderState.isManagerEdit) {
            when {
                renderState.managerEditSource == null -> state.stageAddition(
                    tag,
                    contextController.currentTagsForActiveContext(activePersistenceContext()),
                )
                renderState.managerEditSourceIsPendingAddition -> state.replaceAddition(renderState.managerEditSource, tag)
                else -> state.stageEdit(renderState.managerEditSource, tag)
            }
            returnToManagerFromEdit()
        } else if (renderState.editSource == null) {
            addCustomTag(ship, context, tag, true)
        } else {
            replaceCustomTag(ship, context, renderState.editSource.second, tag)
        }
    }

    private fun currentRenderState(context: ShipEditorPersistenceContext): CustomTagEditRenderState? {
        val definition = currentDraftDefinition()
        val fixedTag = state.fixedEditTag()
        val draftValues = definition?.let(::currentDraftValues).orEmpty()
        if (state.isShipModeEdit()) {
            return currentShipModeRenderState(context, definition, draftValues)
        }
        return CustomTagEditRenderStateBuilder.build(
            context = context,
            definition = definition,
            fixedTag = fixedTag,
            draftValues = draftValues,
            editSource = currentEditSource(),
            editSourceGroupIndex = bindings.editSourceGroupProvider?.invoke(),
            editSourceTagProviderValue = bindings.editSourceTagProvider?.invoke(),
            modalState = state,
        )
    }

    private fun currentShipModeRenderState(
        context: ShipEditorPersistenceContext,
        definition: EditableWeaponTagDefinition?,
        draftValues: Map<String, String>,
    ): CustomTagEditRenderState? {
        return CustomShipModeEditRenderStateBuilder.build(
            definition = definition,
            draftValues = draftValues,
            currentModes = contextController.currentShipModesFor(context),
            loadModes = context::loadModes,
            editSourceGroupIndex = bindings.editSourceGroupProvider?.invoke(),
            editSourceValue = bindings.editSourceTagProvider?.invoke(),
            modalState = state,
        )
    }

    private fun currentDraftDefinition(): EditableWeaponTagDefinition? {
        val id = bindings.draftDefinitionIdProvider?.invoke() ?: return null
        return EditableWeaponTagDefinitions.definitions.firstOrNull { it.id == id }
            ?: EditableShipModeDefinitions.definitionById(id)
    }

    private fun currentDraftValues(definition: EditableWeaponTagDefinition): MutableMap<String, String> {
        return state.draftValuesFor(definition)
    }

    private fun updateDraftValue(parameterId: String, value: String) {
        val definition = currentDraftDefinition() ?: return
        state.updateDraftValue(definition, parameterId, value)
    }

    private fun cycleDraftChoice(definition: EditableWeaponTagDefinition, parameter: ChoiceParameter, delta: Int = 1) {
        state.cycleDraftChoice(definition, parameter, delta)
    }

    private fun currentEditSource(): Pair<Int, String>? {
        val groupIndex = bindings.editSourceGroupProvider?.invoke() ?: return null
        val tag = bindings.editSourceTagProvider?.invoke()?.takeIf { it.isNotBlank() } ?: return null
        return groupIndex to tag
    }
}
