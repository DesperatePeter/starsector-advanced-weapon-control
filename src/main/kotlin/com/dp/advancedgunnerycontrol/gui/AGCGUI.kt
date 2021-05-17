package com.dp.advancedgunnerycontrol.gui

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.utils.FireModeStorage
import com.dp.advancedgunnerycontrol.utils.ShipModeStorage
import com.dp.advancedgunnerycontrol.utils.SuffixStorage
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.EngagementResultAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.UIComponentAPI

class AGCGUI : InteractionDialogPlugin {
    companion object{
        var storageIndex = 0
        fun incrementIndex(){
            if (storageIndex < Settings.maxLoadouts() - 1) storageIndex += 1 else storageIndex = 0
        }
        fun lastIndex() : Int{
            if (storageIndex == 0) return Settings.maxLoadouts() -1
            return storageIndex -1
        }
    }
    private var level = Level.TOP
    private var ship : FleetMemberAPI? = null
    private var dialog: InteractionDialogAPI? = null
    private var text : TextPanelAPI? = null
    private var options : OptionPanelAPI? = null
    private var visualPanel : VisualPanelAPI? = null
    private var customPanel : CustomPanelAPI? = null

    override fun init(dialog: InteractionDialogAPI?) {
        storageIndex = 0
        dialog?.let {
            this.dialog = it
            text = it.textPanel
            options = it.optionPanel
            visualPanel = it.visualPanel
            visualPanel?.saveCurrentVisual()
        }
        if(!Settings.enablePersistentModes()){
            text?.addPara("Persistent Storage has been disabled in the settings.")
            text?.addPara("Enable it to use this GUI")
            options?.addOption("Exit", false)
            return
        }
        displayOptions()
    }

    override fun optionSelected(str: String?, data : Any?) {
        str?.let {
            if("Back" == it) level=Level.TOP
            if("Exit" == it) dialog?.dismiss()
        }
        (data as? String)?.let {
            if("cycle" == it){
                incrementIndex()
            }
            if("copy" == it){
                ship?.id?.let { shipId->
                    ShipModeStorage[storageIndex].modesByShip[shipId] =
                        (ShipModeStorage[lastIndex()].modesByShip[shipId]?.toMutableMap() ?: mutableMapOf())
                    FireModeStorage[storageIndex].modesByShip[shipId] =
                        (FireModeStorage[lastIndex()].modesByShip[shipId]?.toMutableMap() ?: mutableMapOf())
                    SuffixStorage[storageIndex].modesByShip[shipId] =
                        (SuffixStorage[lastIndex()].modesByShip[shipId]?.toMutableMap() ?: mutableMapOf())
                }
            }
        }
        displayOptions()
        return
    }

    private fun displayOptions(){
        options?.clearOptions()
        when(level){
            Level.TOP -> {
                displayFleetOptions()
                return
            }
            Level.SHIP -> displayShipOptions()
        }
        options?.addOption("Back", false)
        options?.addOption("Cycle loadout [Current ${storageIndex + 1} / ${Settings.maxLoadouts()}] <${Settings.loadoutNames().getOrNull(storageIndex) ?: "NoName"}>", "cycle")
        options?.addOption("Copy last loadout", "copy")
    }

    private fun showModeGUI(){
        val shipView = ShipView() // essentially an empty CustomUIPanelPlugin
        customPanel = visualPanel?.showCustomPanel(1200f, 600f, shipView)
        customPanel?.position?.inTMid(20f)
        ship?.let { sh ->
            val elements = mutableListOf<UIComponentAPI>()
            val shipModeElement = customPanel?.createUIElement(1190f, 40f, false)
            shipModeElement?.let {
                it.addTitle("Ship AI Modes")
                shipView.addShipModeButtonGroup(sh, it)
                customPanel?.addComponent(it)
                customPanel?.addUIElement(it)?.inTL(5f, 5f)
            }
            for(i in 0 until sh.variant.weaponGroups.size){
                val element = customPanel?.createUIElement(162f, 500f, false)
                element?.let {
                    it.addTitle("Group ${i+1}")
                    shipView.addModeButtonGroup(i, sh, it)
                    it.addPara("Suffixes:", 5.0f)
                    shipView.addSuffixButtons(i, sh, it)
                    it.addPara(groupAsString(sh.variant.weaponGroups[i], sh), 5.0f)
                    // without this call, we get a "can only anchor to siblings" exception
                    customPanel?.addComponent(it)
                    val pos = customPanel?.addUIElement(it)
                    pos?.let { p ->
                        if (elements.isNotEmpty()){
                            p.rightOfTop(elements.last(), 10f)
                        }else{
                            p.belowLeft(shipModeElement, 5f)
                        }
                    }
                    elements.add(it)
                }
            }
        }

    }

    private fun displayFleetOptions(){

        clear()
        dialog?.showFleetMemberPickerDialog("Pick a ship to adjust weapon modes & suffixes for",
            "Confirm", "Exit", 5, 6, 100f, true, false,
            Global.getSector().playerFleet.membersWithFightersCopy.filter{ !it.isFighterWing }, object : FleetMemberPickerListener{
                override fun pickedFleetMembers(selected: MutableList<FleetMemberAPI>?) {
                    selected?.firstOrNull()?.let {
                        ship = it
                        level = Level.SHIP
                        displayOptions()
                        return
                    }
                    dialog?.dismiss()
                }

                override fun cancelledFleetMemberPicking() {
                    dialog?.dismiss()
                }

            })
    }

    private fun displayShipOptions(){
        clear()
        showModeGUI()
    }

    private fun clear(){
        text?.clear()
        visualPanel?.fadeVisualOut()
    }

    override fun optionMousedOver(optionString: String?, optionData: Any?) { }

    override fun advance(p0: Float) { }

    override fun backFromEngagement(p0: EngagementResultAPI?) { }

    override fun getContext(): Any? = null

    override fun getMemoryMap(): MutableMap<String, MemoryAPI>? = null
}