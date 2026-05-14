package com.dp.advancedgunnerycontrol.gui.customlists.manager

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI

/**
 * Owns Manage Tags and Ship Modes modal composition.
 * ShipView still owns persistence/mutation execution and modal lifecycle.
 */
internal object CustomListManagerModalRenderer {
    fun renderManager(
        dialog: CustomPanelAPI,
        ship: FleetMemberAPI,
        context: ShipEditorPersistenceContext,
        dialogWidth: Float,
        dialogHeight: Float,
        rows: List<CustomTagManagerRow>,
        state: CustomListModalStateController,
        buttons: MutableList<ButtonBase<*>>,
        callbacks: CustomListManagerModalCallbacks,
    ): CustomListModalScrollRegion? {
        if (context.shipId.isBlank()) return null
        val headerHeight = CustomListManagerHeaderRenderer.render(dialog, dialogWidth)
        val scrollRegion = CustomListManagerListRenderer.render(
            dialog = dialog,
            ship = ship,
            context = context,
            dialogWidth = dialogWidth,
            dialogHeight = dialogHeight,
            rows = rows,
            headerHeight = headerHeight,
            state = state,
            buttons = buttons,
            callbacks = callbacks,
        )

        CustomListManagerFooterRenderer.render(
            dialog = dialog,
            dialogWidth = dialogWidth,
            dialogHeight = dialogHeight,
            currentTags = callbacks.currentTags(context),
            currentShipModes = callbacks.currentShipModes(context),
            state = state,
            buttons = buttons,
            callbacks = callbacks,
        )
        return scrollRegion
    }

    fun renderNestedManagerEdit(
        dialog: CustomPanelAPI,
        ship: FleetMemberAPI,
        context: ShipEditorPersistenceContext,
        dialogWidth: Float,
        dialogHeight: Float,
        rows: List<CustomTagManagerRow>,
        editParameterCount: Int,
        state: CustomListModalStateController,
        buttons: MutableList<ButtonBase<*>>,
        callbacks: CustomListManagerModalCallbacks,
    ): CustomListModalScrollRegion? {
        val managerButtonStart = buttons.size
        val scrollRegion = renderManager(
            dialog = dialog,
            ship = ship,
            context = context,
            dialogWidth = dialogWidth,
            dialogHeight = dialogHeight,
            rows = rows,
            state = state,
            buttons = buttons,
            callbacks = callbacks,
        )
        CustomListNestedEditOverlayRenderer.render(
            dialog = dialog,
            ship = ship,
            context = context,
            dialogWidth = dialogWidth,
            dialogHeight = dialogHeight,
            editParameterCount = editParameterCount,
            managerButtonStart = managerButtonStart,
            buttons = buttons,
            callbacks = callbacks,
        )
        return scrollRegion
    }
}
