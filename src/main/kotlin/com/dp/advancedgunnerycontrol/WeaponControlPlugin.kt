@file:Suppress("UnusedImport")

package com.dp.advancedgunnerycontrol

import com.dp.advancedgunnerycontrol.combatgui.GuiBase
import com.dp.advancedgunnerycontrol.combatgui.agccombatgui.AGCCombatGui
import com.dp.advancedgunnerycontrol.keyboardinput.ControlEventType
import com.dp.advancedgunnerycontrol.keyboardinput.KeyStatusManager
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.*
import com.dp.advancedgunnerycontrol.utils.*
import com.dp.advancedgunnerycontrol.weaponais.TagBasedAI
import com.dp.advancedgunnerycontrol.weaponais.tags.MergeTag
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.loading.WeaponGroupType
import org.lazywizard.lazylib.ui.FontException
import org.lazywizard.lazylib.ui.LazyFont
import java.awt.Color


class WeaponControlPlugin : BaseEveryFrameCombatPlugin() {
    private lateinit var engine: CombatEngineAPI
    private lateinit var deployChecker: DeploymentChecker
    private var font: LazyFont? = null

    private var drawable: LazyFont.DrawableString? = null
    private val keyManager = KeyStatusManager()

    private var textFrameTimer: Int = 0
    private var periodicActionsFrameTimer: Int = 1
    private val optionApplicationFrameInterval = 50
    private var isInitialized = false
    private var initialShipInitRequired = Settings.enableAutoSaveLoad()
    private var mergeWeaponGroupsIndex: Int? = null
    private var mergedWeaponRestoration = mutableMapOf<WeaponAPI, Int>()
    private var restoreAlternatingGroups = mutableSetOf<WeaponGroupAPI>()
    private var combatGui: GuiBase? = null

    companion object {
        fun determineSelectedShip(engine: CombatEngineAPI, displayHudWarning: Boolean = true): ShipAPI? {
            var ship: ShipAPI? = null
            if(engine.combatUI.isShowingCommandUI){
                ship = getSelectedShipFromHud(engine.combatUI, displayHudWarning)
            }
            if (engine.playerShip?.shipTarget?.owner == 0) {
                ship = engine.playerShip?.shipTarget
            }
            if(ship == null || ship.owner != 0){
                ship = engine.playerShip
            }
            if(ship?.fleetMember == null) return null
            return ship
        }

        private fun getSelectedShipFromHud(ui: CombatUIAPI, displayHudWarning: Boolean): ShipAPI?{
            try {
                val warRoom = getFieldsByName("warroom", ui).firstOrNull() ?: return null
                val selectionManager = invokeMethodByName("getSelectionManager", warRoom, declared = true) ?: return null
                val combatManager = invokeMethodThatReturnsType(selectionManager, "CombatFleetManager") ?: return null
                val ship = invokeMethodByName("getShipIfShip", combatManager) ?: return null
                if(displayHudWarning && !Settings.suppressHudWarning()){
                    ui.addMessage(0, "Selecting ships for AGC GUI from command HUD is experimental! " +
                            "(warning suppressible in settings)")
                }
                return (ship as? ShipAPI)
            }catch (e: Exception){
                return null
            }
        }
    }

    private fun executeWeaponMergeCommand(index: Int) {

        val ship = determineSelectedShip(engine) ?: return

        if (ship.weaponGroupsCopy?.getOrNull(index) == ship.selectedGroupAPI) {
            printMessage("Please don't select weapon groups immediately after merging. Aborting...")
        }

        if (ship.customData.containsKey(Values.CUSTOM_SHIP_DATA_ARE_WEAPONS_MERGED_KEY)) {
            unmergeWeapons(ship)
        } else {
            mergeWeapons(ship, index)
        }
        ship.giveCommand(ShipCommand.SELECT_GROUP, null, index)

    }

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        super.advance(amount, events)
        if (!isInitialized) return
        combatGui?.advance()

        if (initialShipInitRequired) {
            initialShipInitRequired = !initAllShips()
        }

        mergeWeaponGroupsIndex?.let {
            executeWeaponMergeCommand(it)
            mergeWeaponGroupsIndex = null
        }

        if (periodicActionsFrameTimer++ >= optionApplicationFrameInterval) {
            periodicActionsFrameTimer = 0
            if (Settings.allowEnemyShipModeApplication()) applyOptionsToEnemies()
            if (Settings.automaticallyReapplyPlayerShipModes()) reapplyShipModesAsNecessary()
        }

        undoMergeIfTransferring()

        if (Settings.enableAutoSaveLoad()) initNewlyDeployedShips(deployChecker.checkDeployment())

        TagBasedAI.getTagsRegisteredForEveryFrameAdvancement().forEach { it.advance() }

        if (keyManager.parseInputEvents(events)) {
            processControlEvents()
        }
    }

    private fun initAllShips(): Boolean {
        if (engine.ships.none { it.owner == 0 }) {
            return false
        }
        reloadAllShips(Values.storageIndex)
        return true
    }

    private fun reapplyShipModesAsNecessary() {
        engine.ships.filterNotNull().filter { it.owner == 0 }.forEach { ship ->
            if (loadShipModes(ship, Values.storageIndex).let { it.isNotEmpty() && !it.contains(defaultShipMode) }
                && ship.shipAI != null && !hasCustomAI(ship)) {
                assignShipModes(loadShipModes(ship, Values.storageIndex), ship)
            }
        }
    }

    private fun undoMergeIfTransferring() {
        engine.shipPlayerIsTransferringCommandFrom?.let {
            if(mergedWeaponRestoration.isNotEmpty()) {
                unmergeWeapons(it, false)
                printMessage(
                    "Restoring merged weapons of original ship due to ship transfer...",
                    Settings.uiDisplayFrames() * 2
                )
            }
        }
    }

    private fun applyOptionsToEnemies() {
        engine.ships.filterNotNull().filter { it.owner == 1 }.forEach {
            if (it.customData.containsKey(Values.CUSTOM_SHIP_DATA_OPTIONS_TO_APPLY_KEY)) {
                val toApply = determineTagsByWeaponFromCustomData(it)
                toApply.forEach { (k, v) ->
                    applyTagsToWeapon(k, v)
                }
                it.removeCustomData(Values.CUSTOM_SHIP_DATA_OPTIONS_TO_APPLY_KEY)
                it.setCustomData(Values.CUSTOM_SHIP_DATA_OPTIONS_HAVE_BEEN_APPLIED_KEY, "DONE")
            }
            if (it.customData.containsKey(Values.CUSTOM_SHIP_DATA_SHIP_MODES_TO_APPLY_KEY)){
                val modes = determineShipModesFromCustomData(it)
                assignShipModes(modes, it)
                it.removeCustomData(Values.CUSTOM_SHIP_DATA_SHIP_MODES_TO_APPLY_KEY)
                it.setCustomData(Values.CUSTOM_SHIP_DATA_OPTIONS_HAVE_BEEN_APPLIED_KEY, "DONE")
            }
        }
    }

    private fun initNewlyDeployedShips(deployedShips: List<String>?) {
        deployedShips?.let { fleetShips ->
            if (fleetShips.isEmpty()) return
            // at least when deploying multiple ships, this should be faster than searching each time
            val ships = engine.ships.associateBy { it.fleetMemberId }
                .filter { it.value?.owner == 0 }.filter { fleetShips.contains(it.key) }
            if (ships.isEmpty()) return
            reloadShips(Values.storageIndex, ships.values.toList())
        }
    }

    private fun processControlEvents() {
        when (keyManager.mkeyStatus.mcontrolEvent) {
            ControlEventType.INFO -> {
                if (combatGui == null) {
                    combatGui = determineSelectedShip(engine)?.let { AGCCombatGui(it) } ?: kotlin.run {
                        printMessage("No valid ship selected/deployed. Cannot open Gunnery GUI." +
                                "\nThis usually happens when you don't have a flagship deployed or ally selected." +
                                "\nDeploy a flagship and/or target an ally with the R-Key " +
                                "or open the command HUD (TAB) and select an ally (experimental).",
                            Settings.uiDisplayFrames() * 2) //
                        null
                    }
                    if(combatGui != null){
                        engine.isPaused = true
                        engine.viewport?.isExternalControl = true
                    }
                } else {
                    combatGui = null
                    engine.viewport?.isExternalControl = false
                }
            }

            ControlEventType.MERGE -> {
                determineSelectedShip(engine)?.let { ship ->
                    if (engine.playerShip != ship) {
                        printMessage("Merging weapons only available for player ship!" +
                                "\nMake sure you have no allies selected via R-Key.")
                        return
                    }
                    // NOTE: When trying to merge weapons into the active weapon group, the game throws a NPE
                    // for that reason, when the command to merge is given, I instruct the ship to select a void
                    // group and then instruct the advance-loop of the next frame to actually perform the merging.
                    mergeWeaponGroupsIndex = ship.selectedGroupAPI?.let { ship.weaponGroupsCopy?.indexOf(it) }
                    ship.giveCommand(ShipCommand.SELECT_GROUP, null, ship.weaponGroupsCopy?.size ?: 0)
                }
            }

            else -> printMessage("Unrecognized Command")
        }
    }

    private fun mergeWeapons(ship: ShipAPI, index: Int) {
        printMessage("Merging weapons with tag Merge into current weapon group." +
                "\nPress [${Settings.mergeHotkey().uppercaseChar()}] again to undo. ")
        val groups = ship.weaponGroupsCopy ?: return
        val currentGroup = groups.getOrNull(index) ?: return
        var wasSuccessful = false
        mergedWeaponRestoration = mutableMapOf()
        restoreAlternatingGroups = mutableSetOf()
        groups.forEachIndexed { i, group ->
            if (group != currentGroup) {
                for (weapon in group.weaponsCopy.toList()) {
                    if ((weapon.getAutofirePlugin() as? TagBasedAI)?.tags?.any { it is MergeTag } == true) {
                        wasSuccessful = true
                        val removedWeapon = group.removeWeapon(group.weaponsCopy.indexOf(weapon))
                        // Note: For some reason, switching to an empty alternating weapon group crashes the game...
                        if(group.type == WeaponGroupType.ALTERNATING){
                            restoreAlternatingGroups.add(group)
                            group.type = WeaponGroupType.LINKED
                        }
                        removedWeapon?.let {
                            currentGroup.addWeaponAPI(it)
                            mergedWeaponRestoration.put(it, i)
                        }
                    }
                }
            }
        }
        if(wasSuccessful){
            ship.setCustomData(Values.CUSTOM_SHIP_DATA_ARE_WEAPONS_MERGED_KEY, null)
            reloadShips(Values.storageIndex, listOf(ship))
        }else{
            printMessage("Unable to merge weapons into current group because no other weapon groups" +
                                 "\nhave the Merge tag. Assign the merge tag to some groups and press [${Settings.mergeHotkey().uppercaseChar()}] again." +
                    "\nNote: The Merge tag is available in advanced mode.",
                Settings.uiDisplayFrames() * 2)
        }
    }

    private fun unmergeWeapons(ship: ShipAPI, displayMessage: Boolean = true) {
        if(displayMessage) printMessage("Restoring weapon groups")
        val groups = ship.weaponGroupsCopy ?: return
        mergedWeaponRestoration.forEach { m ->
            ship.getWeaponGroupFor(m.key)?.let { wg ->
                val i = wg.weaponsCopy.indexOf(m.key)
                groups.getOrNull(m.value)?.addWeaponAPI(wg.removeWeapon(i))
            }

        }
        restoreAlternatingGroups.forEach {
            it.type = WeaponGroupType.ALTERNATING
        }
        mergedWeaponRestoration = mutableMapOf()
        restoreAlternatingGroups = mutableSetOf()
        ship.removeCustomData(Values.CUSTOM_SHIP_DATA_ARE_WEAPONS_MERGED_KEY)
        reloadShips(Values.storageIndex, listOf(ship))
    }

    private fun printMessage(message: String, frames: Int = Settings.uiDisplayFrames()) {
        drawable = font?.createText(message, baseColor = Color.GREEN)
        textFrameTimer = frames
    }

    override fun init(engine: CombatEngineAPI?) {
        super.init(engine)
        if(engine == null) return

        engine.customData[Values.CUSTOM_ENGINE_AGC_PRESENT_KEY] = "true"

        try {
            font = LazyFont.loadFont("graphics/fonts/insignia15LTaa.fnt")
        } catch (e: FontException) {
            Global.getLogger(this.javaClass).error("Failed to load font, won't de displaying messages", e)
        }

        // don't init during title screen unless it's a simulation launched from mission screen
        if(Global.getCurrentState() == GameState.TITLE && !engine.isSimulation) return

        this.engine = engine
        deployChecker = DeploymentChecker(engine)
        isInitialized = true

    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        super.renderInUICoords(viewport)
        combatGui?.render()
        drawable?.apply {
            draw(
                Settings.uiMessagePositionX() * Global.getSettings().screenHeightPixels,
                Settings.uiMessagePositionY() * Global.getSettings().screenHeightPixels
            )
            textFrameTimer--
        }
        if ((textFrameTimer <= 0) && (Settings.uiDisplayFrames() >= 0)) {
            drawable = null
        }
    }
}