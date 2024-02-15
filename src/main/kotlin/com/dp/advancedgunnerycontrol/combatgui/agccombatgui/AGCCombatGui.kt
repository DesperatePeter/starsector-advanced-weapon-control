package com.dp.advancedgunnerycontrol.combatgui.agccombatgui


import org.magiclib.combatgui.MagicCombatGuiBase
import org.magiclib.combatgui.MagicCombatRenderShapes.Highlight
import org.magiclib.combatgui.MagicCombatRenderShapes.renderHighlights
import org.magiclib.combatgui.buttons.MagicCombatButtonAction
import org.magiclib.combatgui.buttons.MagicCombatActionButton
import com.dp.advancedgunnerycontrol.gui.groupAsString
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.*
import com.dp.advancedgunnerycontrol.utils.*
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import kotlin.math.max
import kotlin.reflect.KProperty

class AGCCombatGui(private val ship: ShipAPI, private val campaignMode: Boolean = false) : MagicCombatGuiBase(AGCGridLayout) {
    override fun getTitleString(): String {
        return "${ship.name}, ${ship.fleetMember?.variant?.fullDesignationWithHullNameForShip ?: "Unknown ship type"}" +
                " | Tag Scrollbar: " + tagListView.asciiScrollBar()
    }

    override fun getMessageString(): String? {
        ship.variant?.weaponGroups?.let { groups ->
            val firstEmpty = groups.indexOf(groups.find { it.slots.size == 0 })
            val lastFilled = groups.indexOf(groups.findLast { it.slots.size != 0 })
            if((firstEmpty != -1) && (firstEmpty - 1 != lastFilled)){
                return "Warning!\nPlease make sure your filled weapon groups are contiguous!" +
                        "\nYour filled groups should look like [X][X][X][X][ ][ ][ ], not [X][X][ ][X][ ][X][ ]" +
                        "\nOtherwise, configured tags might behave weirdly (since Starsector will collapse your weapon groups later)." +
                        "\nIf in refit-screen, simply briefly select another ship and then this ship again." +
                        "\nWarning!"
            }
//            if(ship.weaponGroupsCopy.filter { it.weaponsCopy.size != 0 }.size != ship.variant.weaponGroups.filter { it.slots.size != 0 }.size){
//                return "Ship variant weapon groups size doesn't match ship. Try refreshing by selecting a different ship."
//            }
        }
        return null
    }

    private val viewScaleMult = Global.getSector()?.viewport?.viewMult ?: 1.0f
    private var highlights = mutableListOf<Highlight>()

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
        val resetButtonAction = object : MagicCombatButtonAction {
            override fun execute() {
                val noTags = listOf<String>()
                for (i in 0 until ship.weaponGroupsCopy.size) {
                    applyTagsToWeaponGroup(ship, i, noTags)
                    saveTags(ship, i, Values.storageIndex, noTags)
                }
                saveShipModes(ship, Values.storageIndex, noTags)
                assignShipModes(noTags, ship)
                refreshButtons()
            }
        }
        addButton(resetButtonAction, "Reset", "Reset all tags for current ship and loadout")

        // CYCLE LOADOUT BUTTON
        val cycleLoadoutButtonInfo = createButtonInfo(standaloneButtons.size, "", "")
        var cycleActionButton: MagicCombatActionButton? = null
        fun updateCycleLoadoutInfo(){
            cycleLoadoutButtonInfo.txt = "Cycle LO ${Values.storageIndex + 1} / ${Settings.maxLoadouts()}"
            cycleLoadoutButtonInfo.tooltip.txt = "Cycle loadout for all ships (${Values.storageIndex + 1} / ${Settings.maxLoadouts()} " +
                    "<${Settings.loadoutNames().getOrNull(Values.storageIndex) ?: "NoName"}>)" +
                    "\nOnly enabled in advanced mode."
            cycleActionButton?.isDisabled = !Settings.isAdvancedMode
        }
        val cycleLoadoutAction = object : MagicCombatButtonAction {
            override fun execute() {
                Values.storageIndex =
                    if (Values.storageIndex < Settings.maxLoadouts() - 1) Values.storageIndex + 1 else 0
                updateCycleLoadoutInfo()
                refreshButtons()
                reloadAllShips(Values.storageIndex)
                refreshButtons()
            }
        }
        cycleActionButton = MagicCombatActionButton(cycleLoadoutAction, cycleLoadoutButtonInfo)
        updateCycleLoadoutInfo()
        addCustomButton(cycleActionButton)

        // RELOAD BUTTON
        val reloadAction = object : MagicCombatButtonAction {
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
        val saveAction = object : MagicCombatButtonAction {
            override fun execute() {
                persistTemporaryShipData(Values.storageIndex, Global.getCombatEngine().ships ?: listOf())
            }
        }
        addButton(
            saveAction,
            "Save",
            "Make all temporary changes to current loadout done in combat permanent." +
                    "\nOnly enabled if automatic in-combat persistence has been disabled in the settings.",
            Settings.enableCombatChangePersistence()
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
        val simpleAdvancedAction = object : MagicCombatButtonAction{
            override fun execute() {
                Settings.isAdvancedMode = !Settings.isAdvancedMode
                updateSimpleAdvancedTexts()
                updateCycleLoadoutInfo()
                tagListView.reset()
                // this isn't pretty and will break if I ever add another button group...
                // TODO: Maybe add IDs to button groups?
                buttonGroups.last().descriptionText = shipAiModesText
                ship.fleetMember?.let { Settings.hotAddTags(loadAllTags(it, generateUniversalFleetMemberId(ship))) }
                reRenderButtonGroups()
            }
        }
        addCustomButton(MagicCombatActionButton(simpleAdvancedAction, simpleAdvancedButtonInfo))

        // Suggested modes Button
        val suggestedModeAction = object : MagicCombatButtonAction{
            override fun execute() {
                if(ship.fleetMember == null) return
                applySuggestedModes(ship.fleetMember, Values.storageIndex, true, generateUniversalFleetMemberId(ship))
                reloadAllShips(Values.storageIndex)
                ship.fleetMember.let { Settings.hotAddTags(loadAllTags(it, generateUniversalFleetMemberId(ship))) }
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
        if (tagListView.hasChanged()) {
            super.reRenderButtonGroups()
        }
    }

    private fun createWeaponGroupDescription(index: Int): String {
        val group = ship.weaponGroupsCopy?.getOrNull(index)
        return "Group ${index + 1}: ${group?.let { groupAsString(it, ship.fleetMember) } ?: "N/A"}"
    }

    private fun initializeUi() {
        ship.fleetMember?.let { Settings.hotAddTags(loadAllTags(it, generateUniversalFleetMemberId(ship))) }

        for (i in 0 until (ship.variant.weaponGroups?.filter { it.slots?.size != 0 }?.size ?: 0)) {
            addButtonGroup(
                WeaponGroupAction(ship, i, highlights, viewScaleMult, campaignMode),
                CreateWeaponButtons(tagListView),
                RefreshWeaponButtons(ship, i),
                createWeaponGroupDescription(i)
            )
        }

        addButtonGroup(ShipAiAction(ship), CreateShipAiButtons(), RefreshShipAiButtons(ship), shipAiModesText)
        createActionButtons()
    }
}