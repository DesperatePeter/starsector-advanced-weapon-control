package com.dp.advancedgunnerycontrol.gui.customlists.state

import com.dp.advancedgunnerycontrol.gui.session.CustomListModalBindings
import com.dp.advancedgunnerycontrol.weapontags.ChoiceParameter
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinition
import java.awt.Color

internal class CustomListModalStateController(
    bindings: CustomListModalBindings,
    changeHandler: CustomListModalChangeHandler,
) : CustomListManagerState, CustomListChangeReviewState {
    private val components = CustomListModalStateComponents.create(bindings, changeHandler)
    private val tagStaging = components.tagStaging
    private val shipModeStaging = components.shipModeStaging
    private val expansionState = components.expansionState
    private val editLifecycle = components.editLifecycle
    private val debugColorDrafts = components.debugColorDrafts
    private val loadoutRenameDrafts = components.loadoutRenameDrafts
    private val session = components.session

    fun isManagerReturnEdit(): Boolean = editLifecycle.isManagerReturnEdit()

    fun managerEditSourceTag(sourceGroupIndex: Int?, sourceTag: String?): String? =
        editLifecycle.managerEditSourceTag(sourceGroupIndex, sourceTag)

    fun isShipModeEdit(): Boolean = editLifecycle.isShipModeEdit()

    fun managerEditIsPendingAddition(): Boolean = editLifecycle.managerEditIsPendingAddition()

    fun additions(): MutableList<String> = tagStaging.additions()

    fun setAdditions(tags: List<String>) = tagStaging.setAdditions(tags)

    fun edits(): Map<String, String> = tagStaging.edits()

    fun setEdits(edits: Map<String, String>) = tagStaging.setEdits(edits)

    fun currentMarkedForRemoval(): MutableSet<String> = tagStaging.currentMarkedForRemoval()

    override fun toggleMarkedForRemoval(tag: String) = tagStaging.toggleMarkedForRemoval(tag)

    override fun normalizeStagedState(currentTags: List<String>): CustomTagManagerStagedState =
        tagStaging.normalizeStagedState(currentTags)

    fun stagedTags(
        currentTags: List<String>,
        pendingAdditions: List<String>,
        pendingRemovals: Set<String>,
        pendingEdits: Map<String, String>,
    ): List<String> =
        tagStaging.stagedTags(currentTags, pendingAdditions, pendingRemovals, pendingEdits)

    override fun stageAddition(tag: String, currentTags: List<String>) = tagStaging.stageAddition(tag, currentTags)

    override fun removeAddition(tag: String) = tagStaging.removeAddition(tag)

    fun replaceAddition(sourceTag: String, editedTag: String) = tagStaging.replaceAddition(sourceTag, editedTag)

    fun stageEdit(sourceTag: String, editedTag: String) = tagStaging.stageEdit(sourceTag, editedTag)

    override fun sourceForEdit(editedTag: String): String? = tagStaging.sourceForEdit(editedTag)

    override fun expandedCategories(): MutableSet<String> = expansionState.expandedCategories()

    override fun expandedArchetypes(): MutableSet<String> = expansionState.expandedArchetypes()

    override fun changeReviewExpandedSections(defaultSections: Collection<String>): MutableSet<String> =
        expansionState.changeReviewExpandedSections(defaultSections)

    override fun toggleManagerCategory(categoryTitle: String) = expansionState.toggleManagerCategory(categoryTitle)

    override fun toggleManagerArchetype(archetypeId: String) = expansionState.toggleManagerArchetype(archetypeId)

    override fun expandedListSections(): MutableSet<String> = expansionState.expandedListSections()

    override fun toggleListSection(sectionId: String) = expansionState.toggleListSection(sectionId)

    override fun shipModeAdditions(): MutableList<String> = shipModeStaging.additions()

    fun setShipModeAdditions(modes: List<String>) = shipModeStaging.setAdditions(modes)

    override fun shipModeRemovals(): MutableSet<String> = shipModeStaging.removals()

    fun setShipModeRemovals(modes: Set<String>) = shipModeStaging.setRemovals(modes)

    override fun shipModeEdits(): Map<String, String> = shipModeStaging.edits()

    fun setShipModeEdits(edits: Map<String, String>) = shipModeStaging.setEdits(edits)

    override fun toggleShipModeMarkedForRemoval(mode: String) = shipModeStaging.toggleMarkedForRemoval(mode)

    override fun stageShipModeAddition(mode: String, currentModes: List<String>) =
        shipModeStaging.stageAddition(mode, currentModes)

    override fun removeShipModeAddition(mode: String) = shipModeStaging.removeAddition(mode)

    override fun normalizeShipModeStagedState(currentModes: List<String>): CustomShipModeManagerStagedState =
        shipModeStaging.normalizeStagedState(currentModes)

    fun stagedShipModes(
        currentModes: List<String>,
        pendingAdditions: List<String>,
        pendingRemovals: Set<String>,
        pendingEdits: Map<String, String>,
    ): List<String> =
        shipModeStaging.stagedShipModes(currentModes, pendingAdditions, pendingRemovals, pendingEdits)

    fun stageShipModeEdit(sourceMode: String, editedMode: String) =
        shipModeStaging.stageEdit(sourceMode, editedMode)

    fun replaceShipModeAddition(sourceMode: String, editedMode: String) =
        shipModeStaging.replaceAddition(sourceMode, editedMode)

    override fun sourceForShipModeEdit(editedMode: String): String? = shipModeStaging.sourceForEdit(editedMode)

    override fun toggleChangeReviewSection(sectionId: String, defaultSections: Collection<String>) =
        expansionState.toggleChangeReviewSection(sectionId, defaultSections)

    fun fixedEditTag(): String? = editLifecycle.fixedEditTag()

    fun draftValuesFor(definition: EditableWeaponTagDefinition): MutableMap<String, String> =
        editLifecycle.draftValuesFor(definition)

    fun updateDraftValue(definition: EditableWeaponTagDefinition, parameterId: String, value: String) =
        editLifecycle.updateDraftValue(definition, parameterId, value)

    fun resetDraftValuesToDefaults(definition: EditableWeaponTagDefinition) =
        editLifecycle.resetDraftValuesToDefaults(definition)

    fun cycleDraftChoice(definition: EditableWeaponTagDefinition, parameter: ChoiceParameter, delta: Int = 1) =
        editLifecycle.cycleDraftChoice(definition, parameter, delta)

    fun beginStandaloneEdit(definition: EditableWeaponTagDefinition) = editLifecycle.beginStandaloneEdit(definition)

    fun beginManagerEdit(definition: EditableWeaponTagDefinition) = editLifecycle.beginManagerEdit(definition)

    fun beginManagerEditFromTag(
        definition: EditableWeaponTagDefinition,
        sourceTag: String,
        pendingAddition: Boolean,
        parameterValues: Map<String, String> = emptyMap(),
    ) = editLifecycle.beginManagerEditFromTag(definition, sourceTag, pendingAddition, parameterValues)

    fun beginManagerEditShipMode(definition: EditableWeaponTagDefinition) =
        editLifecycle.beginManagerEditShipMode(definition)

    fun beginManagerEditFromShipMode(
        definition: EditableWeaponTagDefinition,
        sourceMode: String,
        pendingAddition: Boolean,
        parameterValues: Map<String, String> = emptyMap(),
    ) = editLifecycle.beginManagerEditFromShipMode(definition, sourceMode, pendingAddition, parameterValues)

    fun beginRightClickEdit(
        definition: EditableWeaponTagDefinition?,
        tag: String,
        sourceGroupIndex: Int,
        parameterValues: Map<String, String> = emptyMap(),
    ) = editLifecycle.beginRightClickEdit(definition, tag, sourceGroupIndex, parameterValues)

    fun beginRightClickEditShipMode(
        definition: EditableWeaponTagDefinition,
        mode: String,
        parameterValues: Map<String, String> = emptyMap(),
    ) = editLifecycle.beginRightClickEditShipMode(definition, mode, parameterValues)

    fun returnToManagerFromEdit() = editLifecycle.returnToManagerFromEdit()

    fun closeModal() = session.closeModal()

    fun debugColorIndex(maxIndex: Int): Int = debugColorDrafts.debugColorIndex(maxIndex)

    fun setDebugColorIndex(index: Int) = debugColorDrafts.setDebugColorIndex(index)

    fun debugColorDraft(fallback: Color): Color = debugColorDrafts.debugColorDraft(fallback)

    fun hasDebugColorDraftRgb(): Boolean = debugColorDrafts.hasDebugColorDraftRgb()

    fun setDebugColorDraft(color: Color) = debugColorDrafts.setDebugColorDraft(color)

    fun debugColorPersistent(): Boolean = debugColorDrafts.debugColorPersistent()

    fun setDebugColorPersistent(persistent: Boolean) = debugColorDrafts.setDebugColorPersistent(persistent)

    fun loadoutRenameIndex(defaultIndex: Int, maxLoadouts: Int): Int =
        loadoutRenameDrafts.loadoutRenameIndex(defaultIndex, maxLoadouts)

    fun loadoutRenameDraft(defaultIndex: Int, maxLoadouts: Int, displayName: (Int) -> String): String =
        loadoutRenameDrafts.loadoutRenameDraft(defaultIndex, maxLoadouts, displayName)

    fun setLoadoutRenameDraft(index: Int, name: String) =
        loadoutRenameDrafts.setLoadoutRenameDraft(index, name)
}
