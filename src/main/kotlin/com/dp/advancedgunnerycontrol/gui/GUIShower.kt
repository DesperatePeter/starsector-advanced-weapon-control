package com.dp.advancedgunnerycontrol.gui

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import org.lwjgl.input.Keyboard

class GUIShower : EveryFrameScript {
    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = true

    override fun advance(p0: Float) {

        if (Global.getSector().isInNewGameAdvance || Global.getSector().campaignUI.isShowingDialog
            || Global.getCurrentState() == GameState.TITLE
        ) return

        if (Keyboard.isKeyDown(Keyboard.KEY_MINUS)){
            Global.getSector().campaignUI.showInteractionDialog(AGCGUI(), Global.getSector().playerFleet)
        }
    }
}