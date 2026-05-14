package com.dp.advancedgunnerycontrol.gui.customlists.manager

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

import com.fs.starfarer.api.ui.CustomPanelAPI

/**
 * Dispatch facade for Manage Tags and Ship Modes list rows.
 * Concrete row families live in focused renderers to keep callback wiring small.
 */
internal object CustomTagManagerRowRenderer {
    fun renderManagerRow(
        dialog: CustomPanelAPI,
        row: CustomTagManagerRow,
        y: Float,
        width: Float,
        buttons: MutableList<ButtonBase<*>>,
        editSourceForTag: (String) -> String?,
        editSourceForShipMode: (String) -> String?,
        onToggleCategory: (String) -> Unit,
        onToggleListSection: (String) -> Unit,
        onToggleArchetype: (String) -> Unit,
        onToggleTag: (CustomTagManagerRow.TagEntry, String?) -> Unit,
        onToggleShipMode: (CustomTagManagerRow.ShipModeEntry) -> Unit,
        onEditShipMode: (mode: String, pendingAddition: Boolean, sourceMode: String) -> Unit,
        onEditTag: (tag: String, pendingAddition: Boolean, sourceTag: String) -> Unit,
        onAddDefinition: (EditableWeaponTagDefinition) -> Unit,
        onAddDirectTag: (String) -> Unit,
        onAddShipModeDefinition: (EditableWeaponTagDefinition) -> Unit,
        onAddShipMode: (String) -> Unit,
    ) {
        when (row) {
            is CustomTagManagerRow.ListSectionHeading -> CustomTagManagerHeadingRowRenderer.renderListSectionHeading(
                dialog = dialog,
                row = row,
                y = y,
                width = width,
                buttons = buttons,
                onToggleListSection = onToggleListSection,
            )
            is CustomTagManagerRow.CategoryHeading -> CustomTagManagerHeadingRowRenderer.renderCategoryHeading(
                dialog = dialog,
                row = row,
                y = y,
                width = width,
                buttons = buttons,
                onToggleCategory = onToggleCategory,
            )
            is CustomTagManagerRow.ArchetypeHeading -> CustomTagManagerHeadingRowRenderer.renderArchetypeHeading(
                dialog = dialog,
                row = row,
                y = y,
                width = width,
                buttons = buttons,
                onToggleArchetype = onToggleArchetype,
            )
            is CustomTagManagerRow.TagEntry -> CustomTagManagerTagRowRenderer.renderTagEntry(
                dialog = dialog,
                row = row,
                y = y,
                width = width,
                buttons = buttons,
                editSourceForTag = editSourceForTag,
                onToggleTag = onToggleTag,
                onEditTag = onEditTag,
            )
            is CustomTagManagerRow.AddTagEntry -> CustomTagManagerTagRowRenderer.renderAddTagEntry(
                dialog = dialog,
                row = row,
                y = y,
                width = width,
                buttons = buttons,
                onAddDefinition = onAddDefinition,
                onAddDirectTag = onAddDirectTag,
            )
            is CustomTagManagerRow.ShipModeEntry -> CustomTagManagerShipModeRowRenderer.renderShipModeEntry(
                dialog = dialog,
                row = row,
                y = y,
                width = width,
                buttons = buttons,
                editSourceForShipMode = editSourceForShipMode,
                onToggleShipMode = onToggleShipMode,
                onEditShipMode = onEditShipMode,
            )
            is CustomTagManagerRow.AddShipModeEntry -> CustomTagManagerShipModeRowRenderer.renderAddShipModeEntry(
                dialog = dialog,
                row = row,
                y = y,
                width = width,
                buttons = buttons,
                onAddShipModeDefinition = onAddShipModeDefinition,
                onAddShipMode = onAddShipMode,
            )
        }
    }

    fun renderChangeReviewRow(
        dialog: CustomPanelAPI,
        row: CustomTagChangeReviewRow,
        y: Float,
        width: Float,
        buttons: MutableList<ButtonBase<*>>,
        onToggleSection: (String) -> Unit,
    ) {
        CustomTagChangeReviewRowRenderer.render(
            dialog = dialog,
            row = row,
            y = y,
            width = width,
            buttons = buttons,
            onToggleSection = onToggleSection,
        )
    }
}
