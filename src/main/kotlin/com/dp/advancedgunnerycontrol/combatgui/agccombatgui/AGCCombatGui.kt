package com.dp.advancedgunnerycontrol.combatgui.agccombatgui

import com.dp.advancedgunnerycontrol.combatgui.GuiBase
import com.dp.advancedgunnerycontrol.combatgui.Highlight
import com.dp.advancedgunnerycontrol.combatgui.buttons.ActionButton
import com.dp.advancedgunnerycontrol.combatgui.buttons.ButtonAction
import com.dp.advancedgunnerycontrol.combatgui.renderHighlights
import com.dp.advancedgunnerycontrol.gui.groupAsString
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.*
import com.dp.advancedgunnerycontrol.utils.*
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import kotlin.math.max
import kotlin.reflect.KProperty

class AGCCombatGui(private val ship: ShipAPI) : GuiBase(AGCGridLayout) {
    override fun getTitleString(): String {
        return "${ship.name}, ${ship.fleetMember?.variant?.fullDesignationWithHullNameForShip ?: "Unknown ship type"}" +
                " | Tag Scrollbar: " + tagListView.asciiScrollBar()
    }

    private val viewScaleMult = Global.getSector()?.viewport?.viewMult ?: 1.0f
    private val highlights = mutableListOf<Highlight>()

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

    override fun render() {
        super.render()
        if(Settings.enableWeaponHighlighting()){
            renderHighlights(highlights, Global.getCombatEngine()?.viewport?.viewMult ?: 1.0f)
            highlights.forEach { it.a = max(0.0f, it.a - 0.008f) }
        }
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
                    "<${Settings.loadoutNames().getOrNull(Values.storageIndex) ?: "NoName"}>)" +
                    "\nOnly enabled in advanced mode."
            cycleActionButton?.isDisabled = !Settings.isAdvancedMode
        }
        val cycleLoadoutAction = object : ButtonAction {
            override fun execute() {
                Values.storageIndex =
                    if (Values.storageIndex < Settings.maxLoadouts() - 1) Values.storageIndex + 1 else 0
                updateCycleLoadoutInfo()
                refreshButtons()
                reloadAllShips(Values.storageIndex)
                refreshButtons()
            }
        }
        cycleActionButton = ActionButton(cycleLoadoutAction, cycleLoadoutButtonInfo)
        updateCycleLoadoutInfo()
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
                    "\nOnly enabled if automatic in-combat persistence has been disabled in the settings.",
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

        // Suggested modes Button
        val suggestedModeAction = object : ButtonAction{
            override fun execute() {
                applySuggestedModes(ship.fleetMember, Values.storageIndex)
                reloadAllShips(Values.storageIndex)
                Settings.hotAddTags(loadAllTags(ship.fleetMember, generateUniversalFleetMemberId(ship)))
                refreshButtons()
                reRenderButtonGroups()
            }

        }
        addButton(suggestedModeAction, "Suggested",
            "Apply suggested modes to all weapon groups." +
                    "\nOnly works for vanilla weapons and mods that provide suggested modes." +
                    "\nNote: In simple mode, some added tags might be invisible. " +
                    "Switch to advanced mode to see all tags")
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
        ship.fleetMember?.let { Settings.hotAddTags(loadAllTags(it, generateUniversalFleetMemberId(ship))) }

        for (i in 0 until ship.variant.weaponGroups.size) {
            addButtonGroup(
                WeaponGroupAction(ship, i, highlights, viewScaleMult),
                CreateWeaponButtons(tagListView),
                RefreshWeaponButtons(ship, i),
                createWeaponGroupDescription(i)
            )
        }

        addButtonGroup(ShipAiAction(ship), CreateShipAiButtons(), RefreshShipAiButtons(ship), shipAiModesText)
        createActionButtons()
    }
}