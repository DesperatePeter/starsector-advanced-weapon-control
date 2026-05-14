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

import com.dp.advancedgunnerycontrol.gui.session.CustomListModalMode

internal class CustomListEditLifecycleController(
    private val draftStore: CustomListDraftStore,
    private val onDraftDefinitionIdUpdate: ((String?) -> Unit)?,
    private val onDraftValuesUpdate: ((Map<String, String>) -> Unit)?,
    private val onEditSourceGroupUpdate: ((Int?) -> Unit)?,
    private val onEditSourceTagUpdate: ((String?) -> Unit)?,
    private val onModalModeUpdate: ((CustomListModalMode?) -> Unit)?,
    private val onChanged: () -> Unit,
) {
    fun isManagerReturnEdit(): Boolean {
        return draftStore.value(CustomListDraftKeys.Edit.RETURN) == CustomListDraftKeys.Edit.RETURN_MANAGER
    }

    fun managerEditSourceTag(sourceGroupIndex: Int?, sourceTag: String?): String? {
        if (!isManagerReturnEdit()) return null
        if (sourceGroupIndex != null) return null
        return sourceTag
            ?.takeIf { it.isNotBlank() }
            ?.let { if (isShipModeEdit()) canonicalShipMode(it) else canonicalTag(it) }
    }

    fun isShipModeEdit(): Boolean =
        draftStore.value(CustomListDraftKeys.Edit.KIND) == CustomListDraftKeys.Edit.KIND_SHIP_MODE

    fun managerEditIsPendingAddition(): Boolean {
        return draftStore.value(CustomListDraftKeys.Edit.PENDING_ADDITION)
            ?.toBooleanStrictOrNull()
            ?: false
    }

    fun fixedEditTag(): String? {
        return draftStore.value(CustomListDraftKeys.Edit.FIXED_TAG)
            ?.takeIf { it.isNotBlank() }
            ?.let(::canonicalTag)
    }

    fun draftValuesFor(definition: EditableWeaponTagDefinition): MutableMap<String, String> {
        val current = draftStore.values()
        EditableWeaponTagDefinitions.defaultValuesFor(definition).forEach { (key, value) ->
            current.putIfAbsent(key, value)
        }
        return current
    }

    fun updateDraftValue(definition: EditableWeaponTagDefinition, parameterId: String, value: String) {
        val values = draftValuesFor(definition)
        values[parameterId] = value
        draftStore.writeValues(values)
    }

    fun resetDraftValuesToDefaults(definition: EditableWeaponTagDefinition) {
        val values = draftStore.values()
        for (parameter in definition.parameters) {
            values.remove(parameter.id)
        }
        val defaults = EditableWeaponTagDefinitions.defaultValuesFor(definition)
        for (parameter in definition.parameters) {
            defaults[parameter.id]?.let { value -> values[parameter.id] = value }
        }
        draftStore.writeValues(values)
    }

    fun cycleDraftChoice(definition: EditableWeaponTagDefinition, parameter: ChoiceParameter, delta: Int = 1) {
        val values = draftValuesFor(definition)
        val current = values[parameter.id] ?: parameter.defaultOptionId
        val currentIndex = parameter.options.indexOfFirst { it.id == current }.coerceAtLeast(0)
        val next = parameter.options[(currentIndex + delta + parameter.options.size) % parameter.options.size]
        updateDraftValue(definition, parameter.id, next.id)
    }

    fun beginStandaloneEdit(definition: EditableWeaponTagDefinition) {
        val values = EditableWeaponTagDefinitions.defaultValuesFor(definition).toMutableMap()
        values.remove(CustomListDraftKeys.Edit.FIXED_TAG)
        openEditModal(definition.id, values, sourceGroupIndex = null, sourceTag = null)
    }

    fun beginManagerEdit(definition: EditableWeaponTagDefinition) {
        val values = managerEditDraftValues(definition, CustomListDraftKeys.Edit.KIND_TAG)
        openEditModal(definition.id, values, sourceGroupIndex = null, sourceTag = null)
    }

    fun beginManagerEditFromTag(
        definition: EditableWeaponTagDefinition,
        sourceTag: String,
        pendingAddition: Boolean,
        parameterValues: Map<String, String> = emptyMap(),
    ) {
        val values = managerEditDraftValues(
            definition = definition,
            editKind = CustomListDraftKeys.Edit.KIND_TAG,
            parameterValues = parameterValues,
            pendingAddition = pendingAddition,
        )
        openEditModal(definition.id, values, sourceGroupIndex = null, sourceTag = canonicalTag(sourceTag))
    }

    fun beginManagerEditShipMode(definition: EditableWeaponTagDefinition) {
        val values = managerEditDraftValues(definition, CustomListDraftKeys.Edit.KIND_SHIP_MODE)
        openEditModal(definition.id, values, sourceGroupIndex = null, sourceTag = null)
    }

    fun beginManagerEditFromShipMode(
        definition: EditableWeaponTagDefinition,
        sourceMode: String,
        pendingAddition: Boolean,
        parameterValues: Map<String, String> = emptyMap(),
    ) {
        val values = managerEditDraftValues(
            definition = definition,
            editKind = CustomListDraftKeys.Edit.KIND_SHIP_MODE,
            parameterValues = parameterValues,
            pendingAddition = pendingAddition,
        )
        openEditModal(definition.id, values, sourceGroupIndex = null, sourceTag = canonicalShipMode(sourceMode))
    }

    fun beginRightClickEdit(
        definition: EditableWeaponTagDefinition?,
        tag: String,
        sourceGroupIndex: Int,
        parameterValues: Map<String, String> = emptyMap(),
    ) {
        val canonicalTag = canonicalTag(tag)
        val values = if (definition == null) {
            mutableMapOf(CustomListDraftKeys.Edit.FIXED_TAG to canonicalTag)
        } else {
            (if (parameterValues.isEmpty()) EditableWeaponTagDefinitions.defaultValuesFor(definition) else parameterValues)
                .toMutableMap()
                .also { it.remove(CustomListDraftKeys.Edit.FIXED_TAG) }
        }
        openEditModal(definition?.id, values, sourceGroupIndex = sourceGroupIndex, sourceTag = canonicalTag)
    }

    fun beginRightClickEditShipMode(
        definition: EditableWeaponTagDefinition,
        mode: String,
        parameterValues: Map<String, String> = emptyMap(),
    ) {
        val canonicalMode = canonicalShipMode(mode)
        val values = (if (parameterValues.isEmpty()) EditableWeaponTagDefinitions.defaultValuesFor(definition) else parameterValues)
            .toMutableMap()
        values[CustomListDraftKeys.Edit.KIND] = CustomListDraftKeys.Edit.KIND_SHIP_MODE
        values.remove(CustomListDraftKeys.Edit.RETURN)
        values.remove(CustomListDraftKeys.Edit.PENDING_ADDITION)
        values.remove(CustomListDraftKeys.Edit.FIXED_TAG)
        openEditModal(definition.id, values, sourceGroupIndex = -1, sourceTag = canonicalMode)
    }

    fun returnToManagerFromEdit() {
        val values = draftStore.values()
        values.remove(CustomListDraftKeys.Edit.RETURN)
        values.remove(CustomListDraftKeys.Edit.KIND)
        values.remove(CustomListDraftKeys.Edit.PENDING_ADDITION)
        values.remove(CustomListDraftKeys.Edit.FIXED_TAG)
        onDraftValuesUpdate?.invoke(values)
        clearEditSource()
        onModalModeUpdate?.invoke(CustomListModalMode.MANAGE_TAGS)
        onChanged()
    }

    fun clearEditSource() {
        onDraftDefinitionIdUpdate?.invoke(null)
        onEditSourceGroupUpdate?.invoke(null)
        onEditSourceTagUpdate?.invoke(null)
    }

    private fun managerEditDraftValues(
        definition: EditableWeaponTagDefinition,
        editKind: String,
        parameterValues: Map<String, String> = emptyMap(),
        pendingAddition: Boolean? = null,
    ): MutableMap<String, String> {
        val values = draftStore.values()
        EditableWeaponTagDefinitions.defaultValuesFor(definition).forEach { (key, value) -> values[key] = value }
        parameterValues.forEach { (key, value) -> values[key] = value }
        values[CustomListDraftKeys.Edit.RETURN] = CustomListDraftKeys.Edit.RETURN_MANAGER
        values[CustomListDraftKeys.Edit.KIND] = editKind
        if (pendingAddition == null) {
            values.remove(CustomListDraftKeys.Edit.PENDING_ADDITION)
        } else {
            values[CustomListDraftKeys.Edit.PENDING_ADDITION] = pendingAddition.toString()
        }
        values.remove(CustomListDraftKeys.Edit.FIXED_TAG)
        return values
    }

    private fun openEditModal(
        definitionId: String?,
        values: Map<String, String>,
        sourceGroupIndex: Int?,
        sourceTag: String?,
    ) {
        onDraftDefinitionIdUpdate?.invoke(definitionId)
        onDraftValuesUpdate?.invoke(values)
        onEditSourceGroupUpdate?.invoke(sourceGroupIndex)
        onEditSourceTagUpdate?.invoke(sourceTag)
        onModalModeUpdate?.invoke(CustomListModalMode.EDIT_TAG)
        onChanged()
    }

    private fun canonicalTag(tag: String): String = canonicalizeWeaponTagName(tag)

    private fun canonicalShipMode(mode: String): String = canonicalizeShipModeName(mode)
}
