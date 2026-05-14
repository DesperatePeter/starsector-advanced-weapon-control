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

import com.dp.advancedgunnerycontrol.gui.style.CampaignActionButtonKind
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.fs.starfarer.api.ui.CustomPanelAPI

internal object CustomTagManagerShipModeRowRenderer {
    fun renderShipModeEntry(
        dialog: CustomPanelAPI,
        row: CustomTagManagerRow.ShipModeEntry,
        y: Float,
        width: Float,
        buttons: MutableList<ButtonBase<*>>,
        editSourceForShipMode: (String) -> String?,
        onToggleShipMode: (CustomTagManagerRow.ShipModeEntry) -> Unit,
        onEditShipMode: (mode: String, pendingAddition: Boolean, sourceMode: String) -> Unit,
    ) {
        val status = CustomTagManagerRows.shipModeStatus(row)
        val label = CustomTagManagerRows.shipModeLabel(row.mode, status)
        val template = CampaignGuiStyle.actionButtonTemplate(status.kind)
        val managerButton = CustomTagManagerRowButton.add(
            parent = dialog,
            data = "custom_ship_mode_manager_mode:${row.mode}",
            y = y,
            rowWidth = width,
            indentLevel = 1,
            template = template,
            labelText = label,
            highlightTokens = CustomTagManagerRows.highlightTokens(label, status),
            tooltip = CustomTagManagerRows.shipModeTooltip(row, status),
            centerConfirmCancelText = false,
        ) {
            onToggleShipMode(row)
        }
        if (!row.markedForRemoval) {
            managerButton.onRightClick {
                val sourceMode = if (row.pendingEdit) {
                    editSourceForShipMode(row.mode) ?: row.mode
                } else {
                    row.mode
                }
                onEditShipMode(row.mode, row.pendingAddition, sourceMode)
            }
        }
        buttons.add(managerButton)
    }

    fun renderAddShipModeEntry(
        dialog: CustomPanelAPI,
        row: CustomTagManagerRow.AddShipModeEntry,
        y: Float,
        width: Float,
        buttons: MutableList<ButtonBase<*>>,
        onAddShipModeDefinition: (EditableWeaponTagDefinition) -> Unit,
        onAddShipMode: (String) -> Unit,
    ) {
        val template = CampaignGuiStyle.actionButtonTemplate(CampaignActionButtonKind.ADD_TAG)
        val label = CustomTagManagerRows.shipModeLabel(row.mode, CustomTagManagerTagStatus.NORMAL)
        buttons.add(CustomTagManagerRowButton.add(
            parent = dialog,
            data = "custom_ship_mode_manager_add:${row.definition?.id ?: row.mode}",
            y = y,
            rowWidth = width,
            indentLevel = 1,
            template = template,
            labelText = "Add $label",
            tooltip = "Add $label to this Custom ship-mode list.",
        ) {
            row.definition?.let(onAddShipModeDefinition) ?: onAddShipMode(row.mode)
        })
    }
}
