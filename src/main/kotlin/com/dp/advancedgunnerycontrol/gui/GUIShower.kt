package com.dp.advancedgunnerycontrol.gui

import com.dp.advancedgunnerycontrol.gui.refitscreen.RefitScreenHandler
import com.dp.advancedgunnerycontrol.gui.suggesttaggui.SuggestedTagGui
import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.state.AppDriver
import org.lwjgl.input.Keyboard

class GUIShower : EveryFrameScript {
    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = true

    private val refitHandler = RefitScreenHandler()

    companion object{
        var shouldOpenSuggestedTagGui = false
        var shouldOpenAgcGui = false
    }

    override fun advance(amount: Float) {

        refitHandler.advance(amount)

        if (Global.getSector().isInNewGameAdvance || Global.getSector().campaignUI.isShowingDialog
            || Global.getCurrentState() == GameState.TITLE
        ) return

        if (Keyboard.getEventCharacter().lowercaseChar() == Settings.guiHotkey() || shouldOpenAgcGui) {
            Global.getSector()?.campaignUI?.showInteractionDialog(AGCGUI(), Global.getSector().playerFleet)
            shouldOpenAgcGui = false
        }
        if (shouldOpenSuggestedTagGui) {
            Global.getSector()?.campaignUI?.showInteractionDialog(SuggestedTagGui(), null)
            shouldOpenSuggestedTagGui = false
        }
    }
}