package com.dp.advancedgunnerycontrol.combatgui

import com.dp.advancedgunnerycontrol.combatgui.buttons.ActionButton
import com.dp.advancedgunnerycontrol.combatgui.buttons.ButtonAction
import com.dp.advancedgunnerycontrol.combatgui.buttons.ButtonInfo
import com.dp.advancedgunnerycontrol.combatgui.buttons.HoverTooltip
import com.dp.advancedgunnerycontrol.gui.groupAsString
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.*
import com.dp.advancedgunnerycontrol.utils.*
import com.fs.starfarer.api.combat.ShipAPI
import org.lazywizard.lazylib.ui.LazyFont
import java.awt.Color

class GuiLayout(private val ship: ShipAPI, private val font: LazyFont) {
    companion object{
        private const val xSpacing = 105f
        private const val ySpacing = 50f
        private const val xTooltip = 300f
        private const val yTooltip = 500f
        private var storageIndex = Values.storageIndex
        private val color = Color.GREEN
    }

    private fun createButtonInfo(xIndex: Int, txt: String, tooltipTxt: String) : ButtonInfo{
        return ButtonInfo(Settings.uiPositionX().toFloat() + xIndex * xSpacing, Settings.uiPositionY().toFloat() + ySpacing,
            100f, 20f, 0.5f, txt, font, color, HoverTooltip(
                xTooltip, yTooltip, tooltipTxt))
    }

    @Suppress("UNUSED_CHANGED_VALUE")
    private fun createActionButtons() : List<ActionButton>{
        var xIndex = 0
        val resetButtonInfo = createButtonInfo(xIndex++, "Reset", "Reset all tags for current ship and loadout")
        val resetButtonAction = object : ButtonAction() {
            override fun execute() {
                val noTags = listOf<String>()
                for (i in 0 until ship.weaponGroupsCopy.size){
                    applyTagsToWeaponGroup(ship, i, noTags)
                    saveTags(ship, i, storageIndex, noTags)
                    refreshButtons()
                }
            }
        }
        val cycleLoadoutButtonInfo = createButtonInfo(xIndex++, "Cycle LO",
            "Cycle loadout for all ships (${storageIndex + 1} / ${Settings.maxLoadouts()} " +
                "<${Settings.loadoutNames().getOrNull(storageIndex) ?: "NoName"}>)")
        val cycleLoadoutAction = object : ButtonAction() {
            override fun execute() {
                storageIndex = if (storageIndex < Settings.maxLoadouts() - 1) storageIndex + 1 else 0
                cycleLoadoutButtonInfo.tooltip.txt = "Cycle loadout for all ships (${storageIndex + 1} / ${Settings.maxLoadouts()} " +
                        "<${Settings.loadoutNames().getOrNull(storageIndex) ?: "NoName"}>)"
                refreshButtons()
                reloadAllShips(storageIndex)
            }

        }

        val helpTooltip = createButtonInfo(xIndex++, "Help", Values.HELP_TEXT)
        val reloadTooltip = createButtonInfo(xIndex++, "Reload",
        "Reload all modes and apply them to deployed ships. Normally, you shouldn't have to use this button manually.")
        val reloadAction = object : ButtonAction(){
            override fun execute() {
                reloadAllShips(storageIndex)
            }
        }
        val resetButton = ActionButton(resetButtonAction, resetButtonInfo)
        val cycleLoadoutButton = ActionButton(cycleLoadoutAction, cycleLoadoutButtonInfo)
        val helpButton = ActionButton(null, helpTooltip)
        val reloadButton = ActionButton(reloadAction, reloadTooltip)
        return listOf(resetButton, cycleLoadoutButton, helpButton, reloadButton)
    }

    private fun createWeaponGroupAction(ship: ShipAPI, index: Int): ButtonGroupAction {
        return object : ButtonGroupAction() {
            override fun execute(data: List<Any>) {
                val tagStrings = data.filterIsInstance<String>()
                applyTagsToWeaponGroup(ship, index, tagStrings)
                saveTags(ship, index, storageIndex, tagStrings)
            }

        }
    }

    private fun createShipAiBtnGroupAction(ship: ShipAPI) : ButtonGroupAction{
        return object  : ButtonGroupAction(){
            override fun execute(data: List<Any>) {
                val tags = data.filterIsInstance<String>()
                assignShipMode(tags, ship)
                saveShipModes(ship, storageIndex, tags)
            }
        }
    }


    private fun createDescriptionText(index: Int): String {
        return "Group ${index + 1}: ${groupAsString(ship.fleetMember.variant.weaponGroups[index], ship.fleetMember)}"
    }

    private fun fetchCurrentWeaponTags(index: Int) : List<String>{
        return loadTags(ship, index, storageIndex)
    }


    private val actionButtons = createActionButtons()
    private val weaponButtonGroups = List(ship.variant.weaponGroups.size) { index ->
        DataButtonGroup(
            Settings.uiPositionX().toFloat(), Settings.uiPositionY().toFloat() - index * ySpacing, 100f,
            20f, 0.5f, font, color, 5f, createWeaponGroupAction(ship, index), xTooltip, yTooltip, createDescriptionText(index)
        )
    }
    private val shipButtonGroup = DataButtonGroup(Settings.uiPositionX().toFloat(),
        Settings.uiPositionY().toFloat() - weaponButtonGroups.size * ySpacing,
    100f, 20f, 0.5f, font, color, 5f, createShipAiBtnGroupAction(ship), xTooltip, yTooltip, "Ship AI Modes")

    init {
        weaponButtonGroups.forEachIndexed { index, buttonGroup ->
            val currentTags = fetchCurrentWeaponTags(index)
            tags.forEach {
                buttonGroup.addButton(it, it, tagTooltips[it] ?: "", currentTags.contains(it))
            }
        }
        ShipModes.values().mapNotNull { shipModeToString[it] }.forEach {
            shipButtonGroup.addButton(it, it, detailedShipModeDescriptions[shipModeFromString[it]] ?: "")
        }
        refreshButtons()
    }

    private fun refreshButtons(){
        weaponButtonGroups.forEachIndexed { index, buttonGroup ->
            val currentTags = fetchCurrentWeaponTags(index)
            buttonGroup.refreshAllButtons(currentTags)
            buttonGroup.enableAllButtons()
            buttonGroup.getData().filterIsInstance<String>().forEach { tag ->
                tagIncompatibility[tag]?.let { it.forEach { innerTag ->
                    buttonGroup.disableButton(innerTag)
                } }
            }
            buttonGroup.buttons.forEach{ btn ->
                val str = btn.data as? String ?: ""
                if(true != ship.weaponGroupsCopy[index]?.weaponsCopy?.all { w -> createTag(str, w )?.isValid() == true }){
                    btn.isDisabled = true
                }
            }
        }
        val currentShipModeTags = loadShipModes(ship, storageIndex)
        shipButtonGroup.refreshAllButtons(currentShipModeTags)
    }

    fun advance(){
        weaponButtonGroups.forEach { it.advance() }
        shipButtonGroup.advance()
        refreshButtons()
        actionButtons.forEach { it.advance() }
    }
    fun render(){
        weaponButtonGroups.forEach { it.render() }
        actionButtons.forEach { it.render() }
        shipButtonGroup.render()
    }
}