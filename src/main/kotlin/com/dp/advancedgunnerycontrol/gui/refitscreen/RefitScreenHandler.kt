package com.dp.advancedgunnerycontrol.gui.refitscreen

import com.dp.advancedgunnerycontrol.combatgui.agccombatgui.AGCCombatGui
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
import kotlin.math.max

class RefitScreenHandler {

    companion object{
        var refitPanelAnchorX = 0f
        var refitPanelAnchorY = 0f
    }

    private var lastEventTime = 0L

    private val isRelevantEvent
        get() = Global.getSector().campaignUI.currentCoreTab == CoreUITabId.REFIT && Keyboard.getEventNanoseconds() > lastEventTime
    private val isAgcHotkey
        get() = Keyboard.getEventCharacter().lowercaseChar() == Settings.guiHotkey()
    private val isEsc
        get() = Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)
    private val shouldOpen
        get() = isAgcHotkey && gui == null
    private val shouldClose
        get() = gui != null && (isAgcHotkey || isEsc)

    private var gui: RefitScreenPanel? = null
    fun advance(amount: Float){
        if(!isRelevantEvent) return
        lastEventTime = Keyboard.getEventNanoseconds()
        if(shouldOpen){
            gui = createGUI()

        }else if(shouldClose){
            gui?.close()
            gui = null
        }
    }

    private fun getRefitPanel(core: UIPanelAPI): UIPanelAPI?{
        val panel1 = core.getChildren().find { hasMethodNamed(it, "setBorderInsetLeft") } as? UIPanelAPI ?: return null
        val panel2 = panel1.getChildren().find { hasMethodNamed(it, "goBackToParentIfNeeded") } as? UIPanelAPI ?: return null
        return panel2.getChildren().find { hasMethodNamed(it, "syncWithCurrentVariant") } as? UIPanelAPI
    }

    private fun getShip(refitPanel: UIPanelAPI): ShipAPI?{
        val shipDisplay = invokeMethodByName("getShipDisplay", refitPanel) as? UIPanelAPI ?: return null
        invokeMethodByName("syncWithCurrentVariant", refitPanel)
        return invokeMethodByName("getShip", shipDisplay) as? ShipAPI
    }

    private fun createGUI(): RefitScreenPanel?{
        val appState = AppDriver.getInstance()?.currentState as? CampaignState ?: return null
        val dialog = invokeMethodByName("getEncounterDialog", appState)
        (dialog?.run { invokeMethodByName("getCoreUI", this) as? UIPanelAPI }
            ?: invokeMethodByName("getCore", appState) as? UIPanelAPI)?.let { core ->
            val refitPanel = getRefitPanel(core) ?: return null
            refitPanelAnchorX = refitPanel.position.centerX
            refitPanelAnchorY = refitPanel.position.centerY
            val combatGui = getShip(refitPanel)?.let { ship ->
                AGCCombatGui(ship, true)
            } ?: return null
            val gui = RefitScreenPanel(combatGui, core)
            // values like height/width/x/y are irrelevant
            val panel = Global.getSettings().createCustom(1210f, 800f, gui) ?: return null
            gui.panel = panel
//            val x = max(Settings.uiAnchorX() - 0.05f, 0f) * Global.getSettings().screenWidth
//            val y = max(1f - Settings.uiAnchorY() - 0.1f, 0f) * Global.getSettings().screenHeight
            core.addComponent(panel)?.inTL(10f, 10f)
            return gui
        }
        return null
    }


}