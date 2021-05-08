package com.dp.advancedgunnerycontrol.gui

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.FMValues
import com.dp.advancedgunnerycontrol.typesandvalues.FireMode
import com.dp.advancedgunnerycontrol.utils.FireModeStorage
import com.dp.advancedgunnerycontrol.utils.WeaponModeSelector
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.OptionPanelAPI
import com.fs.starfarer.api.campaign.TextPanelAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.EngagementResultAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.loading.WeaponGroupSpec


class AGCGUI : InteractionDialogPlugin {
    private var level = Level.TOP
    private var ship : FleetMemberAPI? = null
    private var weaponGroup = 0
    private var dialog: InteractionDialogAPI? = null
    private var text : TextPanelAPI? = null
    private var options : OptionPanelAPI? = null
    private val modeStorage = FireModeStorage()
    private var persistText : Boolean = false

    override fun init(dialog: InteractionDialogAPI?) {
        dialog?.let {
            this.dialog = it
            text = it.textPanel
            options = it.optionPanel
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
                persistText = true
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

    private fun displayFleetOptions(){
        clearText()
        text?.addPara("Welcome to the AGC text interface (experimental)")
        text?.addPara("Please select a ship to modify weapon groups for.")

        Global.getSector().playerFleet?.membersWithFightersCopy?.forEach {
            it?.let { fleetMember ->
                if(!fleetMember.isFighterWing){
                    options?.let { opts ->
                        opts.addOption(fleetMember.variant.fullDesignationWithHullNameForShip, fleetMember)
                    }
                }
            }
        }
    }

    private fun displayShipOptions(){
        clearText()
        text?.addPara("Selected ${ship?.variant?.fullDesignationWithHullNameForShip}")
        text?.addPara("Please select weapon group to modify.")
        ship?.let { sh ->
            val groups = sh.stats.variant.weaponGroups.mapIndexedNotNull { i, wg ->  groupAsString(i, wg, sh) }
            options?.let { opts ->
                groups.forEachIndexed { index, s ->
                    opts.addOption("Group ${index+1}: $s", index)
                }
            }
        }
    }

    private fun displayWeaponOptions(){
        clearText()
        text?.addPara("Selected ${ship?.variant?.fullDesignationWithHullNameForShip}")
        val description = ship?.let{ shp -> shp.variant?.weaponGroups?.get(weaponGroup)?.let { groupAsString(weaponGroup, it, shp) }}
        text?.addPara("Selected Group ${weaponGroup+1}: $description")
        text?.addPara("Please select mode.")
        Settings.cycleOrder().forEach { mode ->
            options?.addOption(FMValues.FIRE_MODE_DESCRIPTIONS[mode], mode)
        }
    }

    private fun groupAsString(i: Int, group : WeaponGroupSpec, sh: FleetMemberAPI) : String{
        val strings = group.slots.mapNotNull { sh.variant.getWeaponId(it) }.map {
            Global.getSettings().getWeaponSpec(it).weaponName
        }
        val set = strings.toSet()
        val occ = mutableMapOf<String, Int>()
        strings.forEach { occ[it] = occ[it]?.plus(1) ?: 1 }
        var currentMode = (modeStorage.modesByShip[sh.id]?.get(i)?.currentModeAsString() ?: "--")
        currentMode = if (currentMode.length > 2){
            currentMode.substring(0, currentMode.length-4)
        }else{
            currentMode
        }
        return set.map { ("${occ[it] ?: "0"} x $it") }.toString() + " $currentMode"
    }

    private fun clearText(){
        if (persistText){
            persistText = false
            return
        }
        text?.clear()
    }

    override fun optionMousedOver(p0: String?, p1: Any?) { }

    override fun advance(p0: Float) { }

    override fun backFromEngagement(p0: EngagementResultAPI?) { }

    override fun getContext(): Any? = null

    override fun getMemoryMap(): MutableMap<String, MemoryAPI>? = null
}