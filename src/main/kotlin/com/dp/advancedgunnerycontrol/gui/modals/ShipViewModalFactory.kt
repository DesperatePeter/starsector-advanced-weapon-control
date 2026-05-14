package com.dp.advancedgunnerycontrol.gui.modals

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.foundation.*

import com.dp.advancedgunnerycontrol.gui.*


import com.fs.starfarer.api.ui.CustomPanelAPI
import java.awt.Color
import kotlin.math.max

internal object ShipViewModalFactory {
    fun createShell(
        panel: CustomPanelAPI,
        dialogWidth: Float,
        dialogHeight: Float,
        borderColor: Color,
        dialogFillColor: Color = CampaignGuiStyle.MODAL_DIALOG_FILL_COLOR,
    ): ShipViewModalShell {
        val bounds = centeredBounds(panel, dialogWidth, dialogHeight)
        val backdrop = createBackdrop(panel)
        val dialog = createDialog(
            parent = panel,
            bounds = bounds,
            borderColor = borderColor,
            fillColor = dialogFillColor,
        )
        return ShipViewModalShell(backdrop, dialog, bounds)
    }

    fun centeredBounds(
        panel: CustomPanelAPI,
        dialogWidth: Float,
        dialogHeight: Float,
    ): ShipViewModalBounds {
        return ShipViewModalBounds(
            width = dialogWidth,
            height = dialogHeight,
            x = max(CampaignGuiStyle.PANEL_PADDING, (panel.position.width - dialogWidth) / 2f),
            y = max(CampaignGuiStyle.PANEL_PADDING, (panel.position.height - dialogHeight) / 2f),
        )
    }

    fun createDialog(
        parent: CustomPanelAPI,
        bounds: ShipViewModalBounds,
        borderColor: Color,
        fillColor: Color,
    ): CustomPanelAPI {
        val dialog = parent.createCustomPanel(
            bounds.width,
            bounds.height,
            CampaignPanelPlugin(
                CampaignPanelType.CONTROL_PANEL,
                lineWidth = 2f,
                fillColor = fillColor,
                borderColor = borderColor,
            )
        )
        parent.addComponent(dialog)
        dialog.position.inTL(bounds.x, bounds.y)
        return dialog
    }

    private fun createBackdrop(panel: CustomPanelAPI): CustomPanelAPI {
        val backdrop = panel.createCustomPanel(
            panel.position.width,
            panel.position.height,
            CampaignPanelPlugin(CampaignPanelType.WEAPON_GROUPS_PANEL, fillColor = CampaignGuiStyle.MODAL_BACKDROP_FILL_COLOR)
        )
        panel.addComponent(backdrop)
        backdrop.position.inTL(0f, 0f)
        return backdrop
    }
}
