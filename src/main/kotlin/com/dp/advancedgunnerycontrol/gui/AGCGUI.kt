package com.dp.advancedgunnerycontrol.gui

import com.dp.advancedgunnerycontrol.gui.actions.*
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.TagListView
import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.EngagementResultAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import java.awt.Color
import kotlin.math.min

class AGCGUI : InteractionDialogPlugin {
    companion object{
        var storageIndex = Values.storageIndex //

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

    private var attributes = GUIAttributes()
    private var shipView : ShipView? = null

    private fun addAction(action: GUIAction){
        attributes.options?.addOption(action.getName(), action, action.getTooltip())
        action.getShortcut()?.let {
            attributes.options?.setShortcut(action, it, false, false, false, false)
        }
    }

    override fun init(dialog: InteractionDialogAPI?) {
        storageIndex = 0
        attributes.init(dialog)
        if(!Settings.enablePersistentModes()){
            attributes.text?.addPara("Persistent Storage has been disabled in the settings.")
            attributes.text?.addPara("Enable it to use this GUI")
            addAction(ExitAction(attributes))
            return
        }
        displayOptions()
    }

    override fun optionSelected(str: String?, data : Any?) {
        (data as? GUIAction)?.execute()
        displayOptions()
        return
    }

    private fun displayOptions(){
        attributes.options?.clearOptions()
        when(attributes.level){
            Level.TOP -> displayFleetOptions()
            Level.SHIP -> displayShipOptions()
        }
    }

    private fun showModeGUI(){
        shipView = ShipView(attributes.tagView)
        shipView?.showShipModes(attributes)
    }

    private fun displayFleetOptions(){
        clear()
        attributes.dialog?.showFleetMemberPickerDialog("Pick a ship to adjust weapon modes & suffixes for",
            "Confirm", "Exit", 5, 6, 100f, true, false,
            Global.getSector().playerFleet.membersWithFightersCopy.filter{ !it.isFighterWing }, object : FleetMemberPickerListener{
                override fun pickedFleetMembers(selected: MutableList<FleetMemberAPI>?) {
                    selected?.firstOrNull()?.let {
                        attributes.ship = it
                        attributes.level = Level.SHIP
                        displayOptions()
                        return
                    } ?: attributes.dialog?.dismiss()
                }
                override fun cancelledFleetMemberPicking() {
                    attributes.dialog?.dismiss()
                }
            })
    }

    private fun displayShipOptions(){
        clear()
        showModeGUI()
        generateShipActions(attributes).forEach {
            addAction(it)
        }
    }

    private fun clear(){
        attributes.text?.clear()
        attributes.visualPanel?.fadeVisualOut()
    }

    override fun optionMousedOver(optionString: String?, optionData: Any?) { }
    override fun advance(p0: Float) {
        if (shipView?.shouldRegenerate() == true){
            showModeGUI()
        }
    }
    override fun backFromEngagement(p0: EngagementResultAPI?) { }
    override fun getContext(): Any? = null
    override fun getMemoryMap(): MutableMap<String, MemoryAPI>? = null
}