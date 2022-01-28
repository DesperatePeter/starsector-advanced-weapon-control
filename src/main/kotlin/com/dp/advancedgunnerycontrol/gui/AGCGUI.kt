package com.dp.advancedgunnerycontrol.gui

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.utils.FireModeStorage
import com.dp.advancedgunnerycontrol.utils.ShipModeStorage
import com.dp.advancedgunnerycontrol.utils.SuffixStorage
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.EngagementResultAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.min

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

        fun makeTooltip(description: String) : TooltipMakerAPI.TooltipCreator {
            return object : TooltipMakerAPI.TooltipCreator {
                override fun isTooltipExpandable(p0: Any?): Boolean = false
                override fun getTooltipWidth(p0: Any?): Float = min(description.length.toFloat()*7f, 850f)

                override fun createTooltip(tooltip: TooltipMakerAPI?, p1: Boolean, p2: Any?) {
                    tooltip?.addPara(description, Color.GREEN, 5f)
                }
            }
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
            options?.addOption("Exit", "exit")
            return
        }
        displayOptions()
    }

    override fun optionSelected(str: String?, data : Any?) {
        (data as? String)?.let {
            when(it){
                "cycle" -> incrementIndex()
                "copy" -> copyLastLoadout()
                "next" -> selectNextShip()
                "reset" -> resetCurrentLoadout()
                "resetShip" -> resetCurrentShip()
                "exit" -> dialog?.dismiss()
                "back" -> level=Level.TOP
                "applySuggested" -> applySuggestedModes(ship)
                "applySuggestedFleet" -> applySuggestedModes()
                "applyVariant" -> ship?.let { it1 -> applyModesToSameVariantShips(it1, storageIndex) }
                else -> kotlin.run {  } // do nothing
            }
        }
        displayOptions()
        return
    }

    private fun applySuggestedModes(ship: FleetMemberAPI?){
        ship?.let { sh -> applySuggestedModes( sh, storageIndex) }
    }

    private fun applySuggestedModes(){
        Global.getSector().playerFleet.membersWithFightersCopy.filterNot { m -> m.isFighterWing }.forEach {
            applySuggestedModes(it)
        }
    }

    private fun copyLastLoadout(){
        ship?.id?.let { shipId->
            ShipModeStorage[storageIndex].modesByShip[shipId] =
                (ShipModeStorage[lastIndex()].modesByShip[shipId]?.toMutableMap() ?: mutableMapOf())
            FireModeStorage[storageIndex].modesByShip[shipId] =
                (FireModeStorage[lastIndex()].modesByShip[shipId]?.toMutableMap() ?: mutableMapOf())
            SuffixStorage[storageIndex].modesByShip[shipId] =
                (SuffixStorage[lastIndex()].modesByShip[shipId]?.toMutableMap() ?: mutableMapOf())
        }
    }

    private fun selectNextShip(){
        val shipList = Global.getSector().playerFleet.membersWithFightersCopy.filterNot { m -> m.isFighterWing }
        val index = shipList.indexOf(ship)
        ship = if(index >= shipList.size - 1){
            shipList.first()
        }else{
            shipList[index + 1]
        }
    }

    private fun resetCurrentLoadout(){
        ShipModeStorage[storageIndex].purge()
        FireModeStorage[storageIndex].purge()
        SuffixStorage[storageIndex].purge()
    }

    private fun resetCurrentShip(){
        ship?.let {
            ShipModeStorage[storageIndex].modesByShip[it.id]?.clear()
            SuffixStorage[storageIndex].modesByShip[it.id]?.clear()
            FireModeStorage[storageIndex].modesByShip[it.id]?.clear()
        }
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
        options?.addOption("Back", "back")
        options?.setShortcut("back", Keyboard.KEY_ESCAPE, false, false, false, false)
        options?.addOption("Cycle loadout [Current ${storageIndex + 1} / ${Settings.maxLoadouts()}] <${Settings.loadoutNames().getOrNull(storageIndex) ?: "NoName"}>", "cycle")
        options?.addOption("Copy last loadout", "copy")
        options?.setTooltip("copy", "Copy all modes from loadout ${lastIndex() + 1 } to current loadout for current ship.")
        options?.addOption("Next Ship", "next")
        options?.addOption("-----------------------", "")
        options?.addOption("Reset current ship (current loadout)", "resetShip")
        options?.addOption("Reset all ships (current loadout)", "reset")
        options?.addOption("Apply suggested modes (ship)", "applySuggested")
        options?.setTooltip("applySuggested", "This will apply suggested weapon modes and suffixes to all " +
                "weapon groups. The suggested modes are defined in data/config/modSettings.json. Other mods can also " +
                "define suggested modes in their modSettings.json.\n" +
                "Please double check that all modes look good after applying them. Groups with mixed " +
                "weapons will arbitrarily select one of the weapons to select a mode for the group.\n" +
                "Only affects current loadout and ship.\n" +
                "Note that weapons from mods will usually only have suggested modes if the author" +
                "of that mod included suggested modes in their mod.")
        options?.addOption("Apply suggested modes (Fleet)", "applySuggestedFleet")
        options?.setTooltip("applySuggestedFleet", "Same as above, but affects the entire fleet.")
        options?.addOption("Copy to other ships of same variant", "applyVariant")
        options?.setTooltip("applyVariant", "This will override the modes for all other ships with the same " +
                "hull and variant name with the modes set for the current ship.\n" +
                "This is useful if you used autofit to set up multiple identical ships.")

    }

    private fun showModeGUI(){
        val shipView = ShipView() // essentially an empty CustomUIPanelPlugin
        customPanel = visualPanel?.showCustomPanel(1210f, 650f, shipView)
        customPanel?.position?.inTMid(20f)
        ship?.let { sh ->
            val imgView = customPanel?.createUIElement(100f, 100f, false)
            imgView?.addImage(sh.hullSpec.spriteName, 80f, 80f, 5.0f)
            customPanel?.addUIElement(imgView)?.inTL(1f, 1f)
            val shipModeHeader = customPanel?.createUIElement(1200f, 50f, false)
            shipModeHeader?.addTitle("Ship AI Modes (${sh.shipName}, ${sh.variant?.fullDesignationWithHullNameForShip}):")
            customPanel?.addUIElement(shipModeHeader)?.rightOfBottom(imgView, 1f)
            customPanel?.let {
                if (imgView != null) {
                    shipView.addShipModeButtonGroup(sh, it, imgView)
                }
            }
            val elements = mutableListOf<UIComponentAPI>()
            for(i in 0 until sh.variant.weaponGroups.size){
                val element = customPanel?.createUIElement(162f, 500f, false)
                element?.let {
                    it.addTitle("Group ${i+1}")
                    shipView.addModeButtonGroup(i, sh, it)
                    it.addPara("Suffixes:", 5.0f)
                    shipView.addSuffixButtons(i, sh, it)
                    it.addImages(162f, 35f, 1f, 1f, *groupWeaponSpriteNames(sh.variant.weaponGroups[i], sh).toTypedArray())
                    it.addPara(groupAsString(sh.variant.weaponGroups[i], sh), 5.0f)
                    it.addPara("${groupFluxCost(sh.variant.weaponGroups[i], sh)} flux/s", 5.0f)
                    //it.addImages()
                    // without this call, we get a "can only anchor to siblings" exception
                    customPanel?.addComponent(it)
                    val pos = customPanel?.addUIElement(it)
                    pos?.let { p ->
                        if (elements.isNotEmpty()){
                            p.rightOfTop(elements.last(), 10f)
                        }else{
                            p.belowLeft(imgView, 35f)
                            //p.belowLeft(shipModeElement, 5f)
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