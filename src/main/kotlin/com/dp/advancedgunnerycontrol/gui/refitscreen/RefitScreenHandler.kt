package com.dp.advancedgunnerycontrol.gui.refitscreen


import com.dp.advancedgunnerycontrol.gui.entrypoints.DirectShipEditorPanel
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.reflection.getChildren
import com.dp.advancedgunnerycontrol.reflection.hasMethodNamed
import com.dp.advancedgunnerycontrol.reflection.invokeMethodByName
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.campaign.CampaignState
import com.fs.state.AppDriver
import org.magiclib.combatgui.buttons.MagicCombatButtonAction

class RefitScreenHandler {

    companion object{
        var refitPanelAnchorX = 0f
        var refitPanelAnchorY = 0f
        val isRefit
            get() = Global.getSector()?.campaignUI?.currentCoreTab == CoreUITabId.REFIT
    }




    private var gui: DirectShipEditorPanel? = null
    private var buttonHolder: ButtonHolderPanel? = null
    private var pendingOpenAttempts = 0
    private val moduleManager = ModuleIdManager()
    fun advance(amount: Float){
        if(!Settings.enableRefitScreenIntegration()) return

        if(!isRefit){
            buttonHolder?.close()
            buttonHolder = null
            pendingOpenAttempts = 0
            return
        }

        if(buttonHolder == null){
            buttonHolder = createButtonHolder()
        }

        getShip()?.let {
            moduleManager.update(it)
        }

        if (pendingOpenAttempts > 0 && gui == null) {
            pendingOpenAttempts--
            openGUI(scheduleRetry = false)
        }

        // the button holder takes care of opening/closing the GUI via button or hotkey
        buttonHolder?.advance(amount)
    }

    private fun openGUI(scheduleRetry: Boolean = true){
        gui = createGUI()
        if (scheduleRetry && gui == null && pendingOpenAttempts == 0) {
            pendingOpenAttempts = 5
        }
    }
    private fun closeGUI(){
        gui?.close()
        gui = null
        pendingOpenAttempts = 0
    }
    private fun toggleOpenGUI(){
        if(gui == null){
            openGUI()
        }else{
            closeGUI()
        }
    }

    private fun getRefitPanel(core: UIPanelAPI): UIPanelAPI?{
        val panel1 = core.getChildren().find { hasMethodNamed(it, "setBorderInsetLeft", "getRefitPanel, panel1") } as? UIPanelAPI ?: return null
        val panel2 = panel1.getChildren().find { hasMethodNamed(it, "goBackToParentIfNeeded", "getRefitPanel, panel2") } as? UIPanelAPI ?: return null
        val refitPanel = panel2.getChildren().find { hasMethodNamed(it, "syncWithCurrentVariant", "getRefitPanel, refitPanel") } as? UIPanelAPI
        refitPanelAnchorX = refitPanel?.position?.centerX ?: 0f
        refitPanelAnchorY = refitPanel?.position?.centerY ?: 0f
        return refitPanel
    }

    private fun getShip(refitPanel: UIPanelAPI, sync: Boolean = true): ShipAPI?{
        if(sync) {
            // syncWithCurrentVariant can rebuild the refit ship display. Fetch
            // the display after syncing or AGC may keep editing a stale ShipAPI
            // with pre-refit weapon groups.
            invokeMethodByName("syncWithCurrentVariant", refitPanel, narrativeContext = "GetShip, syncing current refit variant.")
        }
        val shipDisplay = invokeMethodByName("getShipDisplay", refitPanel, narrativeContext = "GetShip, getting ship display") as? UIPanelAPI ?: return null
        val ship =  invokeMethodByName("getShip", shipDisplay, narrativeContext = "GetShip, getting ship from ShipDisplay") as? ShipAPI
        return ship
    }

    private fun getShip(): ShipAPI?{
        val core = getCore() ?: return null
        val refitPanel = getRefitPanel(core) ?: return null
        return getShip(refitPanel, false)
    }

    private fun createGUI(): DirectShipEditorPanel?{
        getCore()?.let { core ->
            val refitPanel = getRefitPanel(core) ?: return null
            val ship = getShip(refitPanel) ?: return null
            return DirectShipEditorPanel.open(core, ship, Settings.guiHotkey()) {
                gui = null
            }
        }
        return null
    }

    private fun createButtonHolder(): ButtonHolderPanel?{
        getCore()?.let { core ->
            val refitPanel = getRefitPanel(core) ?: return null
            val buttonHolderPanel = ButtonHolderPanel(object: MagicCombatButtonAction {
                override fun execute() {
                    toggleOpenGUI()
                }
            },
                refitPanel
            ) { gui != null }
            val panel = Global.getSettings().createCustom(1f, 1f, buttonHolderPanel) ?: return null
            val panelPosition = refitPanel.addComponent(panel) ?: return null
            panelPosition.inBR(110f, 120f)
            buttonHolderPanel.panel = panel
            return  buttonHolderPanel

        }
        return null
    }

    private fun getCore(): UIPanelAPI?{
        val appState = AppDriver.getInstance()?.currentState as? CampaignState ?: return null
        val dialog = invokeMethodByName("getEncounterDialog", appState, narrativeContext = "getCore, getting main dialog")
        return (dialog?.run { invokeMethodByName("getCoreUI", this, narrativeContext = "getCore, trying to get from main dialog") as? UIPanelAPI }
            ?: invokeMethodByName("getCore", appState, narrativeContext = "getCore, trying to get from app state") as? UIPanelAPI)
    }


}
