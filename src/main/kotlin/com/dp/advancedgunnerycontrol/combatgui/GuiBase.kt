package com.dp.advancedgunnerycontrol.combatgui

import com.dp.advancedgunnerycontrol.combatgui.agccombatgui.*
import com.dp.advancedgunnerycontrol.combatgui.buttongroups.*
import com.dp.advancedgunnerycontrol.combatgui.buttons.ActionButton
import com.dp.advancedgunnerycontrol.combatgui.buttons.ButtonAction
import com.dp.advancedgunnerycontrol.combatgui.buttons.ButtonInfo
import com.dp.advancedgunnerycontrol.combatgui.buttons.HoverTooltip
import com.dp.advancedgunnerycontrol.gui.groupAsString
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.*
import com.dp.advancedgunnerycontrol.typesandvalues.Values.storageIndex
import com.dp.advancedgunnerycontrol.utils.*
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import org.lazywizard.lazylib.ui.FontException
import org.lazywizard.lazylib.ui.LazyFont

abstract class GuiBase(private val ship: ShipAPI, private val guiLayout: GuiLayout) {

    private val xSpacing = guiLayout.buttonWidthPx + guiLayout.paddingPx
    private val ySpacing = guiLayout.buttonHeightPx + guiLayout.paddingPx + guiLayout.textSpacingBufferPx
    private val xTooltip = guiLayout.xTooltipRel * Global.getSettings().screenWidthPixels / Global.getSettings().screenScaleMult
    private val yTooltip = guiLayout.yTooltipRel * Global.getSettings().screenHeightPixels / Global.getSettings().screenScaleMult
    private val xAnchor = guiLayout.xAnchorRel * Global.getSettings().screenWidthPixels / Global.getSettings().screenScaleMult
    private val yAnchor = guiLayout.yAnchorRel * Global.getSettings().screenHeightPixels / Global.getSettings().screenScaleMult
    val color = guiLayout.color

    private var font: LazyFont? = null

    private fun createActionButtons(){
        val resetButtonAction = object : ButtonAction() {
            override fun execute() {
                val noTags = listOf<String>()
                for (i in 0 until ship.weaponGroupsCopy.size){
                    applyTagsToWeaponGroup(ship, i, noTags)
                    saveTags(ship, i, storageIndex, noTags)
                }
                saveShipModes(ship, storageIndex, noTags)
                assignShipMode(noTags, ship)
                refreshButtons()
            }
        }
        addButton(resetButtonAction, "Reset", "Reset all tags for current ship and loadout")

        var cycleLoadoutTooltipText = "Cycle loadout for all ships (${storageIndex + 1} / ${Settings.maxLoadouts()} " +
                "<${Settings.loadoutNames().getOrNull(storageIndex) ?: "NoName"}>)"
        val cycleLoadoutAction = object : ButtonAction() {
            override fun execute() {
                storageIndex = if (storageIndex < Settings.maxLoadouts() - 1) storageIndex + 1 else 0
                cycleLoadoutTooltipText = "Cycle loadout for all ships (${storageIndex + 1} / ${Settings.maxLoadouts()} " +
                        "<${Settings.loadoutNames().getOrNull(storageIndex) ?: "NoName"}>)"
                refreshButtons()
                reloadAllShips(storageIndex)
            }
        }
        addButton(cycleLoadoutAction, "Cycle LO", cycleLoadoutTooltipText)

        val reloadAction = object : ButtonAction(){
            override fun execute() {
                reloadAllShips(storageIndex)
            }
        }
        addButton(reloadAction, "Reload", "Reload all modes and apply them to deployed ships. Normally, you shouldn't have to use this button manually.")

        val saveAction = object  : ButtonAction(){
            override fun execute() {
                persistTemporaryShipData(storageIndex, Global.getCombatEngine().ships ?: listOf())
            }
        }
        addButton(saveAction, "Save", "Make all temporary changes to current loadout done in combat permanent." +
                "\nNote: Only relevant if in-combat persistence has been disabled in the settings.", Settings.enableCombatChangePersistance())

        addButton(null, "Help", Values.HELP_TEXT, true)
    }

    private fun createWeaponGroupDescription(index: Int): String {
        return "Group ${index + 1}: ${groupAsString(ship.fleetMember.variant.weaponGroups[index], ship.fleetMember)}"
    }

    abstract val title : LazyFont.DrawableString?
    private val standaloneButtons = mutableListOf<ActionButton>()
    private val buttonGroups = mutableListOf<DataButtonGroup>()

    /**
     * adds a new button group to the GUI. This library will take care of positioning based on grid layout.
     * all actions will be automatically executed when appropriate
     * @param action will be performed when one of the buttons gets clicked, can't pass null
     * @param create will be performed when the button group gets added, create individual buttons in this action, can't pass null
     * @param refresh will be called every frame, feel free to pass null
     * @note ButtonGroups represent a set of data and the data of all active buttons will be passed to the action
     */
    protected fun addButtonGroup(action: ButtonGroupAction, create: CreateButtonsAction, refresh: RefreshButtonsAction?, descriptionText: String){
        val group = object : DataButtonGroup(font, descriptionText, createButtonGroupLayout(buttonGroups.size)){
            override fun createButtons() {
                create.createButtons(this)
            }

            override fun refresh() {
                refresh?.refreshButtons(this)
            }

            override fun executeAction(data: List<Any>, triggeringButtonData: Any?) {
                action.execute(data, triggeringButtonData)
           }
        }
        group.createButtons()
        buttonGroups.add(group)
    }

    /**
     * add a custom button group where you have to take care of positioning
     * actions will be automatically executed when appropriate
     */
    protected fun addCustomButtonGroup(buttonGroup: DataButtonGroup){
        buttonGroup.createButtons()
        buttonGroups.add(buttonGroup)
    }

    /**
     * add a new button to the GUI and let this library handle positioning
     * @param action will be executed when the button is click, feel free to pass null
     * @param txt display text
     * @param tooltipTxt will be displayed when user hovers over button
     */
    protected fun addButton(action: ButtonAction?, txt: String, tooltipTxt: String, isDisabled: Boolean = false){
        val btnInfo = createButtonInfo(standaloneButtons.size, txt, tooltipTxt)
        val btn = ActionButton(action, btnInfo)
        btn.isDisabled = isDisabled
        standaloneButtons.add(btn)
    }

    /**
     * add a custom button where you have to take care of positioning
     */
    protected fun addCustomButton(button: ActionButton){
        standaloneButtons.add(button)
    }

    /**
     * @return layout that would be assigned to button group when using addButtonGroup
     * @note Only relevant if you plan on using addCustomButtonGroup
     */
    protected fun createButtonGroupLayout(index: Int) : ButtonGroupLayout{
        return ButtonGroupLayout(xAnchor, yAnchor - index * ySpacing, guiLayout.buttonWidthPx, guiLayout.buttonHeightPx,
        guiLayout.a, guiLayout.color, guiLayout.paddingPx, xTooltip, yTooltip)
    }

    /**
     * @return button info that would be assigned to button when using addButton
     * @note Only relevant if you plan on using addCustomButton
     */
    protected fun createButtonInfo(xIndex: Int, txt: String, tooltipTxt: String) : ButtonInfo{
        return ButtonInfo(
            xAnchor + xIndex * xSpacing, yAnchor + ySpacing,
            guiLayout.buttonWidthPx, guiLayout.buttonHeightPx, guiLayout.a, txt, font, color, HoverTooltip(
                xTooltip, yTooltip, tooltipTxt))
    }

    private fun initializeUi(){
        Settings.hotAddTags(loadAllTags(ship.fleetMember))
        for (i in 0 until  ship.variant.weaponGroups.size){
            addButtonGroup(WeaponGroupAction(ship, i), CreateWeaponButtons(), RefreshWeaponButtons(ship, i), createWeaponGroupDescription(i))
        }
        addButtonGroup(ShipAiAction(ship), CreateShipAiButtons(), RefreshShipAiButtons(ship), "Ship AI Modes")

        createActionButtons()
    }

    init {
        try {
            font = LazyFont.loadFont("graphics/fonts/insignia15LTaa.fnt")
        } catch (e: FontException) {
            Global.getLogger(this.javaClass).error("Failed to load font, won't de displaying messages", e)
        }
        initializeUi()
        refreshButtons()
    }

    private fun refreshButtons(){
        buttonGroups.forEach{
            it.refresh()
        }
    }

    fun advance(){
        buttonGroups.forEach { it.advance() }
        standaloneButtons.forEach { it.advance() }
        refreshButtons()
    }
    fun render(){
        buttonGroups.forEach { it.render() }
        standaloneButtons.forEach { it.render() }
        title?.draw(xAnchor, yAnchor + (2 * ySpacing))
    }
}