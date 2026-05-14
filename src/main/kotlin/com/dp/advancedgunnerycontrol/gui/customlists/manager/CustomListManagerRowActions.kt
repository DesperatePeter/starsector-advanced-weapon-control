package com.dp.advancedgunnerycontrol.gui.customlists.manager

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

import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.fs.starfarer.api.fleet.FleetMemberAPI

internal data class CustomListManagerRowActions(
    val editSourceForTag: (String) -> String?,
    val editSourceForShipMode: (String) -> String?,
    val onToggleCategory: (String) -> Unit,
    val onToggleListSection: (String) -> Unit,
    val onToggleArchetype: (String) -> Unit,
    val onToggleTag: (CustomTagManagerRow.TagEntry, String?) -> Unit,
    val onToggleShipMode: (CustomTagManagerRow.ShipModeEntry) -> Unit,
    val onEditShipMode: (mode: String, pendingAddition: Boolean, sourceMode: String) -> Unit,
    val onEditTag: (tag: String, pendingAddition: Boolean, sourceTag: String) -> Unit,
    val onAddDefinition: (EditableWeaponTagDefinition) -> Unit,
    val onAddDirectTag: (String) -> Unit,
    val onAddShipModeDefinition: (EditableWeaponTagDefinition) -> Unit,
    val onAddShipMode: (String) -> Unit,
)

internal object CustomListManagerRowActionBinder {
    fun bind(
        ship: FleetMemberAPI,
        context: ShipEditorPersistenceContext,
        state: CustomListManagerState,
        callbacks: CustomListManagerModalCallbacks,
    ): CustomListManagerRowActions {
        return CustomListManagerRowActions(
            editSourceForTag = state::sourceForEdit,
            editSourceForShipMode = state::sourceForShipModeEdit,
            onToggleListSection = state::toggleListSection,
            onToggleCategory = state::toggleManagerCategory,
            onToggleArchetype = state::toggleManagerArchetype,
            onToggleTag = { tagRow, editSource ->
                when {
                    tagRow.pendingAddition -> state.removeAddition(tagRow.tag)
                    tagRow.pendingEdit -> state.toggleMarkedForRemoval(editSource ?: tagRow.tag)
                    else -> state.toggleMarkedForRemoval(tagRow.tag)
                }
            },
            onToggleShipMode = { modeRow ->
                when {
                    modeRow.pendingAddition -> state.removeShipModeAddition(modeRow.mode)
                    modeRow.pendingEdit -> state.toggleShipModeMarkedForRemoval(
                        state.sourceForShipModeEdit(modeRow.mode) ?: modeRow.mode,
                    )
                    else -> state.toggleShipModeMarkedForRemoval(modeRow.mode)
                }
            },
            onEditShipMode = callbacks.startShipModeEdit,
            onEditTag = { tag, pendingAddition, sourceTag ->
                callbacks.startTagEdit(ship, context, tag, pendingAddition, sourceTag)
            },
            onAddDefinition = callbacks.startDefinitionEdit,
            onAddDirectTag = { tag -> state.stageAddition(tag, callbacks.currentTags(context)) },
            onAddShipModeDefinition = callbacks.startShipModeDefinitionEdit,
            onAddShipMode = { mode -> state.stageShipModeAddition(mode, callbacks.currentShipModes(context)) },
        )
    }
}
