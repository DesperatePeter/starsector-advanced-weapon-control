package com.dp.advancedgunnerycontrol.gui.actions


import com.dp.advancedgunnerycontrol.gui.session.GUIAttributes
import com.dp.advancedgunnerycontrol.gui.entrypoints.GUIShower
import com.dp.advancedgunnerycontrol.gui.suggestedtags.SuggestedTagGui
import com.fs.starfarer.api.Global

class GoToSuggestedTagsAction(attributes: GUIAttributes) : GUIAction(attributes) {
    override fun execute() {
        GUIShower.pendingCampaignShipEditorShipId = attributes.ship?.id
        attributes.dialog?.dismiss()
        GUIShower.shouldOpenSuggestedTagGui = true
    }

    override fun getTooltip(): String = "Customize the suggested tags used for weapons."

    override fun getName(): String = "Customize suggested tags"

}
