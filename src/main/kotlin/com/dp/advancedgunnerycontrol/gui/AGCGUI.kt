package com.dp.advancedgunnerycontrol.gui

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.FMValues
import com.dp.advancedgunnerycontrol.typesandvalues.FireMode
import com.dp.advancedgunnerycontrol.utils.FireModeStorage
import com.dp.advancedgunnerycontrol.utils.WeaponModeSelector
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.EngagementResultAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.loading.WeaponGroupSpec
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.UIComponentAPI


class AGCGUI : InteractionDialogPlugin {
    private var level = Level.TOP
    private var ship : FleetMemberAPI? = null
    private var weaponGroup = 0
    private var dialog: InteractionDialogAPI? = null
    private var text : TextPanelAPI? = null
    private var options : OptionPanelAPI? = null
    private var visualPanel : VisualPanelAPI? = null
    private val modeStorage = FireModeStorage()
    private var persist : Boolean = false
    private var showHover = false
    private var customPanel : CustomPanelAPI? = null

    override fun init(dialog: InteractionDialogAPI?) {
        dialog?.let {
            this.dialog = it
            text = it.textPanel
            options = it.optionPanel
            visualPanel = it.visualPanel
            visualPanel?.saveCurrentVisual()
        }
        displayOptions()
    }

    override fun optionSelected(str: String?, data : Any?) {
        str?.let {
            when(it){
                "Close" -> dialog?.dismiss()
                "Back" -> {
                    level = when(level){
                        Level.SHIP -> Level.TOP
                        Level.WEAPON -> Level.SHIP
                        Level.TOP -> Level.TOP
                    }
                }
                // compiler complains without else branch...
                else -> kotlin.run {  }
            }
        }
        data?.let { dt ->
            (dt as? FleetMemberAPI)?.let { ship = it; level = Level.SHIP; }
            (dt as? Int)?.let { weaponGroup = it; level = Level.WEAPON; }
            (dt as? FireMode)?.let { mode ->
                ship?.let { shp ->
                    if (modeStorage.modesByShip[shp.id] == null){
                        modeStorage.modesByShip[shp.id] = mutableMapOf()
                    }
                    modeStorage.modesByShip[shp.id]?.let { it[weaponGroup] = WeaponModeSelector(mode) }
                }

                level = Level.SHIP
                text?.addPara("Set weapon mode to ${FMValues.FIRE_MODE_DESCRIPTIONS[mode]}")
                persist = true
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
                options?.addOption("Close", false)
                return
            }
            Level.SHIP -> displayShipOptions()
            Level.WEAPON -> displayWeaponOptions()
        }
        options?.addOption("Back", false)
    }

    private fun showModeGUI(){
        val shipView = ShipView() // essentially an empty CustomUIPanelPlugin
        // visualPanel?.restoreSavedVisual()
        customPanel = visualPanel?.showCustomPanel(1000f, 600f, shipView)
        customPanel?.position?.inTMid(20f)
        val shipDisplay = customPanel?.createUIElement(1000f, 200f)
        shipDisplay?.position?.inTMid(5f)
        ship?.let { sh ->
            val elements = mutableListOf<UIComponentAPI>()
            for(i in 0 until sh.variant.weaponGroups.size){
                val element = customPanel?.createUIElement(125f, 400f, true)

                element?.let {
                    it.addTitle("Group ${i+1}")
                    shipView.addGroupButtons(i, sh, it)
                    it.addPara(groupAsString(sh.variant.weaponGroups[i], sh), 5.0f)
                    // without this call, we get a "can only anchor to siblings" exception
                    customPanel?.addComponent(it)
                    val pos = customPanel?.addUIElement(it)
                    pos?.let { p ->
                        if (elements.isNotEmpty()){
                            p.rightOfTop(elements.last(), 10f)
                        }else{
                            p.inBL(10f, 10f)
                        }
                    }
                    elements.add(it)
                }
            }
        }

    }

    private fun displayFleetOptions(){
        clear()
        visualPanel?.showFleetInfo("Select a ship to adjust fire modes", Global.getSector().playerFleet, "-", null)
        text?.addPara("Welcome to the AdvancedGunneryControl text interface (experimental)")
        text?.addPara("If this interface interferes with your flow, you can disable it in Settings.editme")
        text?.addPara("Please select a ship to modify weapon groups for.")

        Global.getSector().playerFleet?.membersWithFightersCopy?.forEach {
            it?.let { fleetMember ->
                if(!fleetMember.isFighterWing){
                    options?.let { opts ->
                        opts.addOption("${fleetMember.shipName} (${fleetMember.variant.fullDesignationWithHullNameForShip})", fleetMember)
                    }
                }
            }
        }
    }

    private fun displayShipOptions(){
        clear()
//        dialog?.visualPanel?.showFleetMemberInfo(ship)
//        text?.addPara("Selected ${ship?.variant?.fullDesignationWithHullNameForShip}")
//        text?.addPara("Please select weapon group to modify.")
//        ship?.let { sh ->
//            val groups = sh.stats.variant.weaponGroups.mapIndexedNotNull { i, wg ->  groupAsString(i, wg, sh) }
//            options?.let { opts ->
//                groups.forEachIndexed { index, s ->
//                    opts.addOption("Group ${index+1}: $s", index)
//                }
//            }
//        }
        showModeGUI()
    }

    private fun displayWeaponOptions(){
        clear()
        text?.addPara("Selected ${ship?.variant?.fullDesignationWithHullNameForShip}")
        val description = ship?.let{ shp -> shp.variant?.weaponGroups?.get(weaponGroup)?.let { groupWithMode(weaponGroup, it, shp) }}
        text?.addPara("Selected Group ${weaponGroup+1}: $description")
        text?.addPara("Please select mode.")
        Settings.cycleOrder().forEach { mode ->
            options?.addOption(FMValues.FIRE_MODE_DESCRIPTIONS[mode], mode)
        }
    }

    private fun groupWithMode(i: Int, group : WeaponGroupSpec, sh: FleetMemberAPI) : String{
        val groupDescription = groupAsString(group, sh)
        var currentMode = (modeStorage.modesByShip[sh.id]?.get(i)?.currentModeAsString() ?: "--")
        currentMode = if (currentMode.length > 4){
            currentMode.substring(0, currentMode.length-4)
        }else{
            currentMode
        }
        return  "$groupDescription $currentMode"
    }

    private fun clear(){
        if (persist){
            persist = false
            return
        }
        text?.clear()
        visualPanel?.fadeVisualOut()
    }

    override fun optionMousedOver(optionString: String?, optionData: Any?) {
        (optionData as? FleetMemberAPI)?.let {
            visualPanel?.showFleetMemberInfo(it)
            showHover = true
            return
        }
        if (showHover){
            showHover = false
            displayOptions()
        }


    }

    override fun advance(p0: Float) { }

    override fun backFromEngagement(p0: EngagementResultAPI?) { }

    override fun getContext(): Any? = null

    override fun getMemoryMap(): MutableMap<String, MemoryAPI>? = null
}