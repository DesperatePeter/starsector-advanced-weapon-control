package com.dp.advancedgunnerycontrol.gui

import com.dp.advancedgunnerycontrol.typesandvalues.TagListView
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.OptionPanelAPI
import com.fs.starfarer.api.campaign.TextPanelAPI
import com.fs.starfarer.api.campaign.VisualPanelAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI

class GUIAttributes {
    var level = Level.TOP
    var ship : FleetMemberAPI? = null
    var dialog: InteractionDialogAPI? = null
    var text : TextPanelAPI? = null
    var options : OptionPanelAPI? = null
    var visualPanel : VisualPanelAPI? = null
    var customPanel : CustomPanelAPI? = null
    var tagView = TagListView()

    fun init(input: InteractionDialogAPI?) {
        input?.let {
            dialog = it
            text = it.textPanel
            options = it.optionPanel
            visualPanel = it.visualPanel
            visualPanel?.saveCurrentVisual()
        }
    }
}