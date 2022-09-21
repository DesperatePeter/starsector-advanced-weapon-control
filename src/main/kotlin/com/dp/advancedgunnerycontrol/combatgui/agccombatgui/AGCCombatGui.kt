package com.dp.advancedgunnerycontrol.combatgui.agccombatgui

import com.dp.advancedgunnerycontrol.combatgui.GuiBase
import com.dp.advancedgunnerycontrol.combatgui.buttons.ButtonAction
import com.dp.advancedgunnerycontrol.gui.groupAsString
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.TagListView
import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.dp.advancedgunnerycontrol.typesandvalues.assignShipMode
import com.dp.advancedgunnerycontrol.typesandvalues.saveShipModes
import com.dp.advancedgunnerycontrol.utils.*
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import org.lazywizard.lazylib.ui.LazyFont

class AGCCombatGui(private val ship: ShipAPI) : GuiBase(AGCGridLayout) {
    override fun getTitleString(): String {
        return "${ship.name}, ${ship.fleetMember?.variant?.fullDesignationWithHullNameForShip ?: "Unknown ship type"}" +
                " | Tag Scrollbar: " + tagListView.asciiScrollBar()
    }

    private val tagListView = TagListView()

    init {
        initializeUi()
        refreshButtons()
    }

    private fun createActionButtons(){
        val resetButtonAction = object : ButtonAction {
            override fun execute() {
                val noTags = listOf<String>()
                for (i in 0 until ship.weaponGroupsCopy.size){
                    applyTagsToWeaponGroup(ship, i, noTags)
                    saveTags(ship, i, Values.storageIndex, noTags)
                }
                saveShipModes(ship, Values.storageIndex, noTags)
                assignShipMode(noTags, ship)
                refreshButtons()
            }
        }
        addButton(resetButtonAction, "Reset", "Reset all tags for current ship and loadout")

        var cycleLoadoutTooltipText = "Cycle loadout for all ships (${Values.storageIndex + 1} / ${Settings.maxLoadouts()} " +
                "<${Settings.loadoutNames().getOrNull(Values.storageIndex) ?: "NoName"}>)"
        val cycleLoadoutAction = object : ButtonAction {
            override fun execute() {
                Values.storageIndex = if (Values.storageIndex < Settings.maxLoadouts() - 1) Values.storageIndex + 1 else 0
                cycleLoadoutTooltipText = "Cycle loadout for all ships (${Values.storageIndex + 1} / ${Settings.maxLoadouts()} " +
                        "<${Settings.loadoutNames().getOrNull(Values.storageIndex) ?: "NoName"}>)"
                refreshButtons()
                reloadAllShips(Values.storageIndex)
            }
        }
        addButton(cycleLoadoutAction, "Cycle LO", cycleLoadoutTooltipText)

        val reloadAction = object : ButtonAction{
            override fun execute() {
                reloadAllShips(Values.storageIndex)
            }
        }
        addButton(reloadAction, "Reload", "Reload all modes and apply them to deployed ships. Normally, you shouldn't have to use this button manually.")

        val saveAction = object  : ButtonAction{
            override fun execute() {
                persistTemporaryShipData(Values.storageIndex, Global.getCombatEngine().ships ?: listOf())
            }
        }
        addButton(saveAction, "Save", "Make all temporary changes to current loadout done in combat permanent." +
                "\nNote: Only relevant if in-combat persistence has been disabled in the settings.", Settings.enableCombatChangePersistance())

        addButton(null, "Help", Values.HELP_TEXT, true)
    }

    override fun advance() {
        super.advance()
        tagListView.advance()
        if(tagListView.hasViewChanged()){
            super.reRenderButtonGroups()
        }
    }

    private fun createWeaponGroupDescription(index: Int): String {
        return "Group ${index + 1}: ${groupAsString(ship.fleetMember.variant.weaponGroups[index], ship.fleetMember)}"
    }

    private fun initializeUi(){
        ship.fleetMember?.let { Settings.hotAddTags(loadAllTags(it)) }

        for (i in 0 until  ship.variant.weaponGroups.size){
            addButtonGroup(WeaponGroupAction(ship, i), CreateWeaponButtons(tagListView), RefreshWeaponButtons(ship, i), createWeaponGroupDescription(i))
        }
        addButtonGroup(ShipAiAction(ship), CreateShipAiButtons(), RefreshShipAiButtons(ship), "Ship AI Modes")
        createActionButtons()
    }
}