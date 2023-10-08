package com.dp.advancedgunnerycontrol.gui.refitscreen

import com.dp.advancedgunnerycontrol.combatgui.agccombatgui.AGCCombatGui
import com.dp.advancedgunnerycontrol.combatgui.buttons.ButtonAction
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.utils.getChildren
import com.dp.advancedgunnerycontrol.utils.hasMethodNamed
import com.dp.advancedgunnerycontrol.utils.invokeMethodByName
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.campaign.CampaignState
import com.fs.state.AppDriver
import org.lwjgl.input.Keyboard

class RefitScreenHandler {

    companion object{
        var refitPanelAnchorX = 0f
        var refitPanelAnchorY = 0f
    }

    private var lastEventTime = 0L

    private val isRefit
        get() = Global.getSector().campaignUI.currentCoreTab == CoreUITabId.REFIT
    private val isRelevantEvent
        get() = Keyboard.getEventNanoseconds() > lastEventTime
    private val isAgcHotkey
        get() = Keyboard.getEventCharacter().lowercaseChar() == Settings.guiHotkey() && isRelevantEvent
    private val isEsc
        get() = Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) && isRelevantEvent
    private val shouldOpen
        get() = isAgcHotkey && gui == null
    private val shouldClose
        get() = gui != null && (isAgcHotkey || isEsc)

    private var gui: RefitScreenPanel? = null
    private var buttonHolder: ButtonHolderPanel? = null
    fun advance(amount: Float){
        if(!isRefit){
            buttonHolder?.close()
            buttonHolder = null
            return
        }

        if(buttonHolder == null){
            buttonHolder = createButtonHolder()
        }

        buttonHolder?.advance(amount)

        if(shouldOpen){
            openGUI()
        }else if(shouldClose){
            closeGUI()
        }
        lastEventTime = Keyboard.getEventNanoseconds()
    }

    private fun openGUI(){
        gui = createGUI()
    }
    private fun closeGUI(){
        gui?.close()
        gui = null
    }
    private fun toggleOpenGUI(){
        if(gui == null){
            openGUI()
        }else{
            closeGUI()
        }
    }

    private fun getRefitPanel(core: UIPanelAPI): UIPanelAPI?{
        val panel1 = core.getChildren().find { hasMethodNamed(it, "setBorderInsetLeft") } as? UIPanelAPI ?: return null
        val panel2 = panel1.getChildren().find { hasMethodNamed(it, "goBackToParentIfNeeded") } as? UIPanelAPI ?: return null
        val refitPanel = panel2.getChildren().find { hasMethodNamed(it, "syncWithCurrentVariant") } as? UIPanelAPI
        refitPanelAnchorX = refitPanel?.position?.centerX ?: 0f
        refitPanelAnchorY = refitPanel?.position?.centerY ?: 0f
        return refitPanel
    }

    private fun getShip(refitPanel: UIPanelAPI): ShipAPI?{
        val shipDisplay = invokeMethodByName("getShipDisplay", refitPanel) as? UIPanelAPI ?: return null
        invokeMethodByName("syncWithCurrentVariant", refitPanel)
        return invokeMethodByName("getShip", shipDisplay) as? ShipAPI
    }

    private fun createGUI(): RefitScreenPanel?{
        getCore()?.let { core ->
            val refitPanel = getRefitPanel(core) ?: return null
            val combatGui = getShip(refitPanel)?.let { ship ->
                AGCCombatGui(ship, true)
            } ?: return null
            val refitScreenPanel = RefitScreenPanel(combatGui, core)
            val panel = Global.getSettings().createCustom(1f, 1f, refitScreenPanel) ?: return null
            refitScreenPanel.panel = panel
            core.addComponent(panel)?.inTL(10f, 10f)
            return refitScreenPanel
        }
        return null
    }

    private fun createButtonHolder(): ButtonHolderPanel?{
        getCore()?.let { core ->
            val refitPanel = getRefitPanel(core) ?: return null
            val buttonHolderPanel = ButtonHolderPanel(object: ButtonAction{
                override fun execute() {
                    toggleOpenGUI()
                }
            }, refitPanel)
            val panel = Global.getSettings().createCustom(1f, 1f, buttonHolderPanel) ?: return null
            buttonHolderPanel.panel = panel
            val posX = refitPanelAnchorX / Global.getSettings().screenScaleMult + (refitPanel.position?.width ?: 0f) * 0.37f * Global.getSettings().screenScaleMult
            val posY = refitPanelAnchorY / Global.getSettings().screenScaleMult - (refitPanel.position?.height ?: 0f) * 0.5f * Global.getSettings().screenScaleMult
            refitPanel.addComponent(panel)?.inBR(110f, 10f)
            return  buttonHolderPanel

        }
        return null
    }

    private fun getCore(): UIPanelAPI?{
        val appState = AppDriver.getInstance()?.currentState as? CampaignState ?: return null
        val dialog = invokeMethodByName("getEncounterDialog", appState)
        return (dialog?.run { invokeMethodByName("getCoreUI", this) as? UIPanelAPI }
            ?: invokeMethodByName("getCore", appState) as? UIPanelAPI)
    }


}