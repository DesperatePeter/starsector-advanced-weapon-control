package com.dp.advancedgunnerycontrol.gui.customlists.manager

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.fs.starfarer.api.ui.CustomPanelAPI

/**
 * Bridges manager-list rows to concrete row renderers.
 * State mutation and row-button visuals live in focused helpers.
 */
internal object CustomListManagerRowRenderer {
    fun renderManagerRow(
        dialog: CustomPanelAPI,
        row: CustomTagManagerRow,
        y: Float,
        width: Float,
        buttons: MutableList<ButtonBase<*>>,
        actions: CustomListManagerRowActions,
    ) {
        CustomTagManagerRowRenderer.renderManagerRow(
            dialog = dialog,
            row = row,
            y = y,
            width = width,
            buttons = buttons,
            editSourceForTag = actions.editSourceForTag,
            editSourceForShipMode = actions.editSourceForShipMode,
            onToggleListSection = actions.onToggleListSection,
            onToggleCategory = actions.onToggleCategory,
            onToggleArchetype = actions.onToggleArchetype,
            onToggleTag = actions.onToggleTag,
            onToggleShipMode = actions.onToggleShipMode,
            onEditShipMode = actions.onEditShipMode,
            onEditTag = actions.onEditTag,
            onAddDefinition = actions.onAddDefinition,
            onAddDirectTag = actions.onAddDirectTag,
            onAddShipModeDefinition = actions.onAddShipModeDefinition,
            onAddShipMode = actions.onAddShipMode,
        )
    }
}
