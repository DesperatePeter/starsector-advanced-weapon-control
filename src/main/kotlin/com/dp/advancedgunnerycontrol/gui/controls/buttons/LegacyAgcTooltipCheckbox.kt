package com.dp.advancedgunnerycontrol.gui.controls.buttons

import com.dp.advancedgunnerycontrol.gui.controls.suppression.CampaignButtonSuppression

import com.dp.advancedgunnerycontrol.gui.entrypoints.AGCGUI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipLocation
import com.fs.starfarer.api.util.Misc

// Legacy checkbox helpers
internal object LegacyAgcTooltipCheckbox {
    fun add(
        tooltip: TooltipMakerAPI,
        label: String?,
        data: Any?,
        tooltipText: String? = null,
        width: Float = 160f,
        height: Float = 18f,
        pad: Float = 3f,
    ): ButtonAPI {
        val button = tooltip.addAreaCheckbox(
            label,
            data,
            Misc.getBasePlayerColor(),
            Misc.getDarkPlayerColor(),
            Misc.getBrightPlayerColor(),
            width,
            height,
            pad
        )
        CampaignButtonSuppression.registerCampaignButton(button)
        if (!tooltipText.isNullOrBlank()) {
            tooltip.addTooltipToPrevious(
                AGCGUI.makeTooltip(tooltipText),
                TooltipLocation.BELOW
            )
        }
        return button
    }
}
