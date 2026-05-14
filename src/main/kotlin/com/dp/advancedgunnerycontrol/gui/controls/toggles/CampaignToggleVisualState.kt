package com.dp.advancedgunnerycontrol.gui.controls.toggles

import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.fs.starfarer.api.ui.ButtonAPI

// Toggle visual state
class CampaignToggleVisualState {
    private var visualStateChecked: Boolean? = null

    fun apply(button: ButtonAPI, force: Boolean = false) {
        if (!force && visualStateChecked == button.isChecked) return
        CampaignGuiStyle.applyToggleableCheckboxVisualState(button)
        visualStateChecked = button.isChecked
    }

    fun applyUnavailable(button: ButtonAPI) {
        CampaignGuiStyle.applyUnavailableCheckboxVisualState(button)
        visualStateChecked = null
    }
}
