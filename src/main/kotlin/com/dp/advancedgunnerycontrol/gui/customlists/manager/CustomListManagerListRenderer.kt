package com.dp.advancedgunnerycontrol.gui.customlists.manager

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI

internal object CustomListManagerListRenderer {
    fun render(
        dialog: CustomPanelAPI,
        ship: FleetMemberAPI,
        context: ShipEditorPersistenceContext,
        dialogWidth: Float,
        dialogHeight: Float,
        rows: List<CustomTagManagerRow>,
        headerHeight: Float,
        state: CustomListManagerState,
        buttons: MutableList<ButtonBase<*>>,
        callbacks: CustomListManagerModalCallbacks,
    ): CustomListModalScrollRegion {
        val rowTop = CampaignGuiStyle.MODAL_PADDING + headerHeight + CampaignGuiStyle.MODAL_TITLE_BODY_GAP
        val buttonY = dialogHeight - CampaignGuiStyle.MODAL_PADDING - CampaignGuiStyle.MODAL_ROW_HEIGHT
        val actions = CustomListManagerRowActionBinder.bind(
            ship = ship,
            context = context,
            state = state,
            callbacks = callbacks,
        )
        return CustomListModalListRenderer.render(
            dialog = dialog,
            dialogWidth = dialogWidth,
            dialogHeight = dialogHeight,
            rowTop = rowTop,
            buttonY = buttonY,
            rows = rows,
            target = CustomListModalScrollTarget.MANAGE_TAGS,
            scrollUpData = "custom_tag_manager_scroll_up",
            scrollDownData = "custom_tag_manager_scroll_down",
            currentOffset = callbacks.scrollOffset(CustomListModalScrollTarget.MANAGE_TAGS),
            buttons = buttons,
            onScrollOffsetChanged = { callbacks.setScrollOffset(CustomListModalScrollTarget.MANAGE_TAGS, it) },
            onDirty = callbacks.refresh,
        ) { listPanel, row, y, width ->
            CustomListManagerRowRenderer.renderManagerRow(
                dialog = listPanel,
                row = row,
                y = y,
                width = width,
                buttons = buttons,
                actions = actions,
            )
        }
    }
}
