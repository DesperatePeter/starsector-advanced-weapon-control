@file:Suppress("UnusedImport")

package com.dp.advancedgunnerycontrol

import com.dp.advancedgunnerycontrol.combatgui.GuiLayout
import com.dp.advancedgunnerycontrol.keyboardinput.ControlEventType
import com.dp.advancedgunnerycontrol.keyboardinput.KeyStatusManager
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.*
import com.dp.advancedgunnerycontrol.utils.*
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.input.InputEventAPI
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
    var storageIndex = 0
    private var isInitialized = false
    private var initialShipInitRequired = Settings.enableAutoSaveLoad()

    private var combatGui: GuiLayout? = null

    companion object {
        fun determineSelectedShip(engine: CombatEngineAPI): ShipAPI? {
            return if (engine.playerShip?.shipTarget?.owner == 0) {
                (engine.playerShip?.shipTarget) ?: engine.playerShip
            } else {
                engine.playerShip
            }
        }
    }

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        super.advance(amount, events)
        if (!isInitialized) return
        combatGui?.advance()

        if(initialShipInitRequired){
            initialShipInitRequired = !initAllShips()
        }

         if (Settings.enableAutoSaveLoad()) initNewlyDeployedShips(deployChecker.checkDeployment())

        if (keyManager.parseInputEvents(events)) {
            processControlEvents()
        }
    }

    private fun initAllShips() : Boolean{
        if(engine.ships.none { it.owner == 0 }){
            return false
        }
        val remainingShips = reloadAllShips(Values.storageIndex)
        if(Settings.enableLegacyCommands()){
            remainingShips.let {
                it.forEach { ship ->
                    initOrGetAIManager(ship)
                }
            }
        }
        return true
    }

    private fun initNewlyDeployedShips(deployedShips: List<String>?){
        deployedShips?.let { fleetShips ->
            if (fleetShips.isEmpty()) return
            // at least when deploying multiple ships, this should be faster than searching each time
            val ships = engine.ships.associateBy { it.fleetMemberId }
                .filter { it.value?.owner == 0 }.filter { fleetShips.contains(it.key) }
            if(ships.isEmpty()) return
            val remainingShips = reloadShips(Values.storageIndex, ships.values.toList())
            if(Settings.enableLegacyCommands()){
                remainingShips.let {
                    it.forEach { ship ->
                        initOrGetAIManager(ship)
                    }
                }
            }
        }
    }

    private fun processControlEvents() {
        when (keyManager.mkeyStatus.mcontrolEvent) {
            ControlEventType.COMBINE -> combineWeaponGroup()
            ControlEventType.CYCLE -> {
                if(Settings.enableLegacyCommands()){
                    cycleWeaponGroupMode()
                    saveCurrentShipIfApplicable()
                }else{
                    printMessage("Command unavailable, enable legacy commands in Settings.editme")
                }
            }
            ControlEventType.INFO -> {
                // printShipInfo()
                if (combatGui == null){
                    engine.isPaused = true
                    engine.viewport?.isExternalControl = true
                    combatGui = determineSelectedShip(engine)?.let { font?.let { f -> GuiLayout(it, f) } }
                }else{
                    combatGui = null
                    engine.viewport?.isExternalControl = false
                }
            }
            ControlEventType.RESET -> {
                if(Settings.enableLegacyCommands()) {
                    resetAiManager()
                    saveCurrentShipIfApplicable()
                    printShipInfo()
                }else{
                    printMessage("Command unavailable, enable legacy commands in Settings.editme")
                }
            }
            ControlEventType.LOAD -> {
                if(Settings.enableLegacyCommands()) {
                    initAllShips()
                }else{
                    printMessage("Command unavailable, enable legacy commands in Settings.editme")
                }
            }
            ControlEventType.CYCLE_LOADOUT -> {
                if(Settings.enableLegacyCommands()) {
                    cycleLoadouts()
                    printMessage("Switched to loadout ${storageIndex + 1} / ${Settings.maxLoadouts()} <${Settings.loadoutNames()[storageIndex]}>")
                }else{
                    printMessage("Command unavailable, enable legacy commands in Settings.editme")
                }
            }
            ControlEventType.SUFFIX -> {
                if(Settings.enableLegacyCommands()){
                    cycleSuffix()
                    saveCurrentShipIfApplicable()
                }else{
                    printMessage("Command unavailable, enable legacy commands in Settings.editme")
                }
            }
            ControlEventType.HELP -> {
                printMessage(Settings.getKeybindingInfoText(), Settings.uiDisplayFrames() * 5)
            }
            ControlEventType.SAVE -> {
                if(Settings.enableLegacyCommands()) {
                    saveCurrentShipState()
                    printMessage("Saved modes for current ship.")
                }else{
                    printMessage("Command unavailable, enable legacy commands in Settings.editme")
                }
            }
            else -> printMessage("Unrecognized Command (please send bug report)")
        }
    }

    private fun cycleSuffix(){
        if(tryCycleSuffix()){
            printShipInfo()
        }else{
            printMessage("Invalid Weapon Group\nCycle fire mode and try again.")
        }
    }

    private fun tryCycleSuffix() :  Boolean{
        val ship = determineSelectedShip(engine) ?: return false
        val aiManager = initOrGetAIManager(ship) ?: return false
        val index = keyManager.mkeyStatus.lastPressedWeaponGroup - 1
        if (index == -1 || (ship.weaponGroupsCopy?.size ?: 0) <= index) {
            return false
        }
        return aiManager.cycleSuffix(index)
    }

    private fun cycleWeaponGroupMode() {
        val ship = determineSelectedShip(engine)
        val aiManager = initOrGetAIManager(ship) ?: return
        val index = keyManager.mkeyStatus.mpressedWeaponGroup - 1
        if ((ship?.weaponGroupsCopy?.size ?: 0) <= index) {
            printMessage("Invalid Weapon Group")
            return
        }
        aiManager.cycleWeaponGroupMode(index)
        if (Settings.uiForceFullInfo()) {
            printShipInfo()
        } else {
            printMessage(aiManager.getFireModeDescription(index))
        }
    }

    private fun resetAiManager(){
        initOrGetAIManager(determineSelectedShip(engine))?.reset()
    }

    private fun initOrGetAIManager(ship: ShipAPI?): WeaponAIManager? {
        ship?.let { ship_ ->
            ShipModeStorage[storageIndex].modesByShip[ship_.fleetMemberId]?.values?.let {
                assignShipMode(it.toList(), ship_)
            }
            if (ship_.customData?.containsKey(Values.WEAPON_AI_MANAGER_KEY) == true) {
                ship_.customData?.get(Values.WEAPON_AI_MANAGER_KEY)?.let { unsafeManager ->
                    (unsafeManager as? WeaponAIManager)?.let { return it }
                }
            }
            val aiManager = WeaponAIManager(engine, ship_)
            ship_.fleetMemberId?.let { id ->
                val suffixMap = SuffixStorage[storageIndex].modesByShip[id]?.mapValues { SuffixSelector(suffixFromString[it.value]) }
                    ?.toMutableMap()
                val modeMap =
                    FireModeStorage[storageIndex].modesByShip[id]?.mapValues { WeaponModeSelector(FMValues.FIRE_MODE_TRANSLATIONS[it.value]) }
                        ?.toMutableMap()
                aiManager.refresh(modeMap, suffixMap)

                ship_.setCustomData(Values.WEAPON_AI_MANAGER_KEY, aiManager)

            }
            return aiManager
        }
        return null
    }

    private fun printShipInfo() {
        determineSelectedShip(engine)?.let { ship ->
            val wpAiManager = initOrGetAIManager(ship)
            var shipMode = "No Ship AI"
            if(ship.shipAI != null){
                val numShipModes = ShipModeStorage[storageIndex].modesByShip[ship.fleetMemberId ?: ""]?.values?.size ?: 0
                shipMode = (ShipModeStorage[storageIndex].modesByShip[ship.fleetMemberId ?: ""]?.values?.firstOrNull() ?: "Default")
                if (numShipModes > 1){
                    shipMode += " +${numShipModes-1}"
                }
            }
            val shipInfo = "${ship.variant.fullDesignationWithHullNameForShip} ($shipMode)"
            var i = 1 // current weapon group display number
            val weaponGroupInfo = ship.weaponGroupsCopy.map { weaponGroup ->
                "Group ${i}: " + weaponGroup.weaponsCopy.map { weapon -> weapon.displayName }.toSet() +
                        (wpAiManager?.getFireModeSuffix(i++ - 1) ?: " --") +
                        "\n"
            }
            val weaponGroupDisplayText =
                weaponGroupInfo.toString().trimMargin("[").trimMargin("]").trimMargin(",").trimIndent()
            printMessage("$shipInfo:\n $weaponGroupDisplayText")
        }
    }

    private fun cycleLoadouts() {
        if (storageIndex < Settings.maxLoadouts() - 1) storageIndex+=1 else storageIndex = 0
        engine.ships.filterNotNull().filter { it.owner == 0 }.forEach {
            it.customData.remove(Values.WEAPON_AI_MANAGER_KEY)
            initOrGetAIManager(it)
        }
    }

    private fun saveCurrentShipIfApplicable() {
        if(Settings.enableCombatChangePersistance()) saveCurrentShipState()
    }

    private fun saveCurrentShipState() {
        if(!Settings.enablePersistentModes()) return
        val ship = determineSelectedShip(engine) ?: return
        initOrGetAIManager(ship)?.let { aiManager ->
            ship.fleetMemberId?.let { id ->
                Settings.fireModeStorage[storageIndex].modesByShip[id] = aiManager.weaponGroupModes.mapValues {
                    FMValues.fireModeAsString[it.value.currentValue] ?: FMValues.defaultFireModeString
                }.toMutableMap()
                Settings.suffixStorage[storageIndex].modesByShip[id] = aiManager.weaponGroupSuffixes.mapValues {
                    suffixDescriptions[it.value.currentValue] ?: defaultSuffixString
                }.toMutableMap()
            }
        }
    }

    private fun printMessage(message: String, frames: Int = Settings.uiDisplayFrames()) {
        drawable = font?.createText(message, baseColor = Color.GREEN)
        textFrameTimer = frames
    }

    private fun combineWeaponGroup() {
        // TODO
    }

    override fun init(engine: CombatEngineAPI?) {
        super.init(engine)
        if (null != engine) {
            try {
                font = LazyFont.loadFont("graphics/fonts/insignia15LTaa.fnt")
            } catch (e: FontException) {
                Global.getLogger(this.javaClass).error("Failed to load font, won't de displaying messages", e)
            }
            // don't init during title screen
            if(Global.getCurrentState() != GameState.TITLE) {
                this.engine = engine
                deployChecker = DeploymentChecker(engine)
                isInitialized = true
            }
        }
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        super.renderInUICoords(viewport)
        combatGui?.render()
        drawable?.apply {
            draw(Settings.uiMessagePositionX() * Global.getSettings().screenHeightPixels, Settings.uiMessagePositionY() * Global.getSettings().screenHeightPixels)
            textFrameTimer--
        }
        if ((textFrameTimer <= 0) && (Settings.uiDisplayFrames() >= 0)) {
            drawable = null
        }
    }
}