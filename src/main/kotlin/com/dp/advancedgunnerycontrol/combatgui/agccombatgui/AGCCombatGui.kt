package com.dp.advancedgunnerycontrol.combatgui.agccombatgui

import com.dp.advancedgunnerycontrol.combatgui.GuiBase
import com.dp.advancedgunnerycontrol.combatgui.buttons.ActionButton
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
import kotlin.reflect.KProperty

class AGCCombatGui(private val ship: ShipAPI) : GuiBase(AGCGridLayout) {
    override fun getTitleString(): String {
        return "${ship.name}, ${ship.fleetMember?.variant?.fullDesignationWithHullNameForShip ?: "Unknown ship type"}" +
                " | Tag Scrollbar: " + tagListView.asciiScrollBar()
    }

    private val tagListView = TagListView()
    private val shipAiModesText : String by object {
        operator fun getValue(thisRef: Any?, property: KProperty<*>) : String{
            return if(Settings.isAdvancedMode) "Ship AI Modes" else ""
        }
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String){
        }
    }

    init {
        initializeUi()
        refreshButtons()
    }

    private fun createActionButtons() {
        // RESET BUTTON
        val resetButtonAction = object : ButtonAction {
            override fun execute() {
                val noTags = listOf<String>()
                for (i in 0 until ship.weaponGroupsCopy.size) {
                    applyTagsToWeaponGroup(ship, i, noTags)
                    saveTags(ship, i, Values.storageIndex, noTags)
                }
                saveShipModes(ship, Values.storageIndex, noTags)
                assignShipMode(noTags, ship)
                refreshButtons()
            }
        }
        addButton(resetButtonAction, "Reset", "Reset all tags for current ship and loadout")

        // CYCLE LOADOUT BUTTON
        val cycleLoadoutButtonInfo = createButtonInfo(standaloneButtons.size, "", "")
        var cycleActionButton: ActionButton? = null
        fun updateCycleLoadoutInfo(){
            cycleLoadoutButtonInfo.txt = "Cycle LO ${Values.storageIndex + 1} / ${Settings.maxLoadouts()}"
            cycleLoadoutButtonInfo.tooltip.txt = "Cycle loadout for all ships (${Values.storageIndex + 1} / ${Settings.maxLoadouts()} " +
                    "<${Settings.loadoutNames().getOrNull(Values.storageIndex) ?: "NoName"}>)"
            cycleActionButton?.isDisabled = !Settings.isAdvancedMode
        }
        updateCycleLoadoutInfo()
        val cycleLoadoutAction = object : ButtonAction {
            override fun execute() {
                Values.storageIndex =
                    if (Values.storageIndex < Settings.maxLoadouts() - 1) Values.storageIndex + 1 else 0
                updateCycleLoadoutInfo()
                refreshButtons()
                reloadAllShips(Values.storageIndex)
            }
        }
        cycleActionButton = ActionButton(cycleLoadoutAction, cycleLoadoutButtonInfo)
        addCustomButton(cycleActionButton)

        // RELOAD BUTTON
        val reloadAction = object : ButtonAction {
            override fun execute() {
                reloadAllShips(Values.storageIndex)
            }
        }
        addButton(
            reloadAction,
            "Reload",
            "Reload all modes and apply them to deployed ships. Normally, you shouldn't have to use this button manually."
        )

        // SAVE BUTTON
        val saveAction = object : ButtonAction {
            override fun execute() {
                persistTemporaryShipData(Values.storageIndex, Global.getCombatEngine().ships ?: listOf())
            }
        }
        addButton(
            saveAction,
            "Save",
            "Make all temporary changes to current loadout done in combat permanent." +
                    "\nNote: Only relevant if in-combat persistence has been disabled in the settings.",
            Settings.enableCombatChangePersistance()
        )
        addButton(null, "Help", Values.HELP_TEXT, true)

        // SIMPLE/ADVANCED MODE BUTTON
        val simpleAdvancedButtonInfo = createButtonInfo(standaloneButtons.size, "", "")
        fun updateSimpleAdvancedTexts() {
            simpleAdvancedButtonInfo.txt = if (Settings.isAdvancedMode) "To Simple" else "To Advanced"
            simpleAdvancedButtonInfo.tooltip.txt = if (Settings.isAdvancedMode) {
                "Switch to simple mode, displaying less tags"
            } else {
                "Switch to advanced mode, showing more tags"
            }
        }
        updateSimpleAdvancedTexts()
        val simpleAdvancedAction = object : ButtonAction{
            override fun execute() {
                Settings.isAdvancedMode = !Settings.isAdvancedMode
                updateSimpleAdvancedTexts()
                updateCycleLoadoutInfo()
                // this isn't pretty and will break if I ever add another button group...
                // FIXME: Maybe add IDs to button groups?
                buttonGroups.last().descriptionText = shipAiModesText
                reRenderButtonGroups()
            }
        }
        addCustomButton(ActionButton(simpleAdvancedAction, simpleAdvancedButtonInfo))
    }

    override fun advance() {
        super.advance()
        tagListView.advance()
        if (tagListView.hasViewChanged()) {
            super.reRenderButtonGroups()
        }
    }

    private fun createWeaponGroupDescription(index: Int): String {
        return "Group ${index + 1}: ${groupAsString(ship.fleetMember.variant.weaponGroups[index], ship.fleetMember)}"
    }

    private fun initializeUi() {
        Settings.hotAddTags(loadAllTags(ship.fleetMember, generateUniversalFleetMemberId(ship)))

        for (i in 0 until ship.variant.weaponGroups.size) {
            addButtonGroup(
                WeaponGroupAction(ship, i),
                CreateWeaponButtons(tagListView),
                RefreshWeaponButtons(ship, i),
                createWeaponGroupDescription(i)
            )
        }

        addButtonGroup(ShipAiAction(ship), CreateShipAiButtons(), RefreshShipAiButtons(ship), shipAiModesText)
        createActionButtons()
    }
}