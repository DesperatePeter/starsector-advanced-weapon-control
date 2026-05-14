package com.dp.advancedgunnerycontrol.gui.entrypoints
import com.dp.advancedgunnerycontrol.gui.refitscreen.RefitScreenHandler
import com.dp.advancedgunnerycontrol.gui.suggestedtags.SuggestedTagGui
import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import org.lwjgl.input.Keyboard

class GUIShower : EveryFrameScript {
    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = true

    private val refitHandler = RefitScreenHandler()
    private var guiHotkeyWasDown = false

    companion object{
        var shouldOpenSuggestedTagGui = false
        var shouldOpenAgcGui = false
        var pendingCampaignShipEditorShipId: String? = null
    }

    override fun advance(amount: Float) {
        val sector = Global.getSector() ?: return

        refitHandler.advance(amount)

        if (sector.campaignUI.currentCoreTab == CoreUITabId.REFIT) {
            guiHotkeyWasDown = Keyboard.isKeyDown(Settings.guiHotkey())
            // The refit integration owns the GUI hotkey while the refit tab is
            // active. Letting the campaign-level poller also react to the same
            // key can race the direct editor open path and produce intermittent
            // "nothing opened" behavior.
            return
        }

        val guiHotkeyDown = Keyboard.isKeyDown(Settings.guiHotkey())
        val guiHotkeyPressed = guiHotkeyDown && !guiHotkeyWasDown
        guiHotkeyWasDown = guiHotkeyDown

        if (sector.isInNewGameAdvance || sector.campaignUI.isShowingDialog
            || Global.getCurrentState() == GameState.TITLE
        ) return

        if (guiHotkeyPressed || shouldOpenAgcGui) {
            sector.campaignUI?.showInteractionDialog(AGCGUI(), sector.playerFleet)
            shouldOpenAgcGui = false
        }
        if (shouldOpenSuggestedTagGui) {
            sector.campaignUI?.showInteractionDialog(SuggestedTagGui(), null)
            shouldOpenSuggestedTagGui = false
        }
    }
}
