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
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext

internal object CustomTagEditRenderStateBuilder {
    fun build(
        context: ShipEditorPersistenceContext,
        definition: EditableWeaponTagDefinition?,
        fixedTag: String?,
        draftValues: Map<String, String>,
        editSource: Pair<Int, String>?,
        editSourceGroupIndex: Int?,
        editSourceTagProviderValue: String?,
        modalState: CustomListModalStateController,
    ): CustomTagEditRenderState? {
        if (definition == null && fixedTag == null) return null
        val buildResult = definition?.buildCanonicalTag(draftValues)
        val preview = buildResult?.canonicalTag?.let(EditableWeaponTagDefinitions::displayName)
            ?: buildResult?.errors?.firstOrNull()?.message
            ?: fixedTag?.let(EditableWeaponTagDefinitions::displayName)
            ?: definition?.templateTag
            ?: ""
        val canonicalTag = buildResult?.canonicalTag ?: fixedTag
        val isValid = buildResult?.isValid ?: (fixedTag != null)
        val isManagerEdit = modalState.isManagerReturnEdit()
        val managerEditSource = modalState.managerEditSourceTag(editSourceGroupIndex, editSourceTagProviderValue)
        val managerEditSourceIsPendingAddition = modalState.managerEditIsPendingAddition()
        val editSourceTag = editSource?.second ?: managerEditSource
        val editSourceState = editSourceState(context, editSourceTag)
        val existingTags = existingTags(
            context = context,
            editSourceTag = editSourceTag,
            modalState = modalState,
        )
        val fixedTagEdit = definition == null
        return CustomTagEditRenderState(
            definition = definition,
            fixedTagEdit = fixedTagEdit,
            draftValues = draftValues,
            preview = preview,
            canonicalTag = canonicalTag,
            isValid = isValid,
            editSource = editSource,
            isManagerEdit = isManagerEdit,
            managerEditSource = managerEditSource,
            managerEditSourceIsPendingAddition = managerEditSourceIsPendingAddition,
            editSourceTag = editSourceTag,
            existingTags = existingTags,
            confirmAvailability = availability(
                preview = preview,
                canonicalTag = canonicalTag,
                isValid = isValid,
                editSourceState = editSourceState,
                existingTags = existingTags,
                action = CustomTagEditAction.CONFIRM,
                fixedTagEdit = fixedTagEdit,
            ),
            copyAvailability = availability(
                preview = preview,
                canonicalTag = canonicalTag,
                isValid = isValid,
                editSourceState = editSourceState,
                existingTags = existingTags,
                action = CustomTagEditAction.COPY,
                fixedTagEdit = fixedTagEdit,
            ),
            deleteAvailability = availability(
                preview = preview,
                canonicalTag = canonicalTag,
                isValid = isValid,
                editSourceState = editSourceState,
                existingTags = existingTags,
                action = CustomTagEditAction.DELETE,
                fixedTagEdit = fixedTagEdit,
            ),
        )
    }

    private fun availability(
        preview: String,
        canonicalTag: String?,
        isValid: Boolean,
        editSourceState: CustomTagEditSourceState,
        existingTags: Set<String>,
        action: CustomTagEditAction,
        fixedTagEdit: Boolean,
    ): CustomTagEditAvailability {
        return CustomTagEditLogic.availability(
            preview = preview,
            canonicalTag = canonicalTag,
            isValid = isValid,
            editSourceState = editSourceState,
            existingTags = existingTags,
            action = action,
            fixedTagEdit = fixedTagEdit,
        )
    }

    private fun editSourceState(
        context: ShipEditorPersistenceContext,
        editSourceTag: String?,
    ): CustomTagEditSourceState {
        val canonicalSource = editSourceTag
            ?.takeIf { it.isNotBlank() }
            ?.let(::canonicalTag)
        if (canonicalSource == null) {
            return CustomTagEditSourceState(
                canonicalTag = null,
                inCustomList = false,
                activeOnShip = false,
            )
        }
        val shipId = context.shipId.takeIf { it.isNotBlank() }
        val inCustomList = shipId
            ?.let { canonicalSource in canonicalizeWeaponTagNames(CustomWeaponTagListStore.getSupportedCustomTags(it)) }
            ?: false
        val activeOnShip = isActiveOnShip(context, canonicalSource)
        return CustomTagEditSourceState(
            canonicalTag = canonicalSource,
            inCustomList = inCustomList,
            activeOnShip = activeOnShip,
        )
    }

    private fun isActiveOnShip(context: ShipEditorPersistenceContext, canonicalTag: String): Boolean {
        for (loadoutIndex in 0 until Settings.maxLoadouts()) {
            for (groupIndex in 0 until Values.MAX_WEAPON_GROUPS) {
                if (canonicalTag in canonicalizeWeaponTagNames(context.loadWeaponTags(groupIndex, loadoutIndex))) {
                    return true
                }
            }
        }
        return false
    }

    private fun existingTags(
        context: ShipEditorPersistenceContext,
        editSourceTag: String?,
        modalState: CustomListModalStateController,
    ): Set<String> {
        val shipId = context.shipId.takeIf { it.isNotBlank() } ?: return emptySet()
        val managerEdits = modalState.edits()
        val managerEditSources = managerEdits.keys
        val currentStagedTags = modalState.stagedTags(
            currentTags = CustomWeaponTagListStore.getSupportedCustomTags(shipId),
            pendingAdditions = modalState.additions(),
            pendingRemovals = modalState.currentMarkedForRemoval(),
            pendingEdits = managerEdits,
        )
        return canonicalizeWeaponTagNames(
            currentStagedTags.filterNot { tag ->
                tag == editSourceTag ||
                    tag in managerEditSources
            }
        ).toSet()
    }

    private fun canonicalTag(tag: String): String = canonicalizeWeaponTagName(tag)
}
