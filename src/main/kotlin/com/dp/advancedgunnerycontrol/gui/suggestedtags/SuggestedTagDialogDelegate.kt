package com.dp.advancedgunnerycontrol.gui.suggestedtags

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate
import com.fs.starfarer.api.ui.CustomPanelAPI

internal class SuggestedTagDialogDelegate(
    private val panelPlugin: SuggestedTagPanelPlugin,
) : CustomVisualDialogDelegate {
    override fun init(panel: CustomPanelAPI, callbacks: CustomVisualDialogDelegate.DialogCallbacks) {
        panelPlugin.init(panel, callbacks)
    }

    override fun getCustomPanelPlugin(): CustomUIPanelPlugin = panelPlugin
    override fun getNoiseAlpha(): Float = 0f
    override fun advance(amount: Float) {}
    override fun reportDismissed(option: Int) {}
}
