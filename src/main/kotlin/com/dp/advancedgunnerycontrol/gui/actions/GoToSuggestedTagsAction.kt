package com.dp.advancedgunnerycontrol.gui.actions

import com.dp.advancedgunnerycontrol.gui.GUIAttributes
import com.dp.advancedgunnerycontrol.gui.GUIShower
import com.dp.advancedgunnerycontrol.gui.suggesttaggui.SuggestedTagGui
import com.fs.starfarer.api.Global

class GoToSuggestedTagsAction(attributes: GUIAttributes) : GUIAction(attributes) {
    override fun execute() {
        attributes.dialog?.dismiss()
        GUIShower.shouldOpenSuggestedTagGui = true
    }

    override fun getTooltip(): String = "Open up a new GUI that let's you customize suggested tags for weapons."

    override fun getName(): String = "Customize suggested tags"

}