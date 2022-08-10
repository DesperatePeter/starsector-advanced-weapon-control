@file:Suppress("UnusedImport")

package com.dp.advancedgunnerycontrol

import com.dp.advancedgunnerycontrol.combatgui.GuiLayout
import com.dp.advancedgunnerycontrol.keyboardinput.ControlEventType
import com.dp.advancedgunnerycontrol.keyboardinput.KeyStatusManager
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.*
import com.dp.advancedgunnerycontrol.utils.*
import com.dp.advancedgunnerycontrol.weaponais.TagBasedAI
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
    private var timeFrameTimer: Int = 1
    private val optionApplicationFrameInterval = 100
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

        if(timeFrameTimer++ >= optionApplicationFrameInterval){
            timeFrameTimer = 0
            applyOptionsToEnemies()
        }

        if (Settings.enableAutoSaveLoad()) initNewlyDeployedShips(deployChecker.checkDeployment())

        TagBasedAI.getTagsRegisteredForEveryFrameAdvancement().forEach { it.advance() }

        if (keyManager.parseInputEvents(events)) {
            processControlEvents()
        }
    }

    private fun initAllShips() : Boolean{
        if(engine.ships.none { it.owner == 0 }){
            return false
        }
        reloadAllShips(Values.storageIndex)
        return true
    }

    private fun applyOptionsToEnemies(){
        engine.ships.filterNotNull().filter { it.owner == 1 }.forEach {
            if(it.customData.containsKey(Values.CUSTOM_SHIP_DATA_OPTIONS_TO_APPLY_KEY)){
                val toApply = determineTagsByGroup(it)
                toApply.forEach { (k, v) ->
                    applyTagsToWeaponGroup(it, k, v)
                }
                it.removeCustomData(Values.CUSTOM_SHIP_DATA_OPTIONS_TO_APPLY_KEY)
                it.setCustomData(Values.CUSTOM_SHIP_DATA_OPTIONS_HAVE_BEEN_APPLIED_KEY, "DONE")
            }
        }
    }

    private fun initNewlyDeployedShips(deployedShips: List<String>?){
        deployedShips?.let { fleetShips ->
            if (fleetShips.isEmpty()) return
            // at least when deploying multiple ships, this should be faster than searching each time
            val ships = engine.ships.associateBy { it.fleetMemberId }
                .filter { it.value?.owner == 0 }.filter { fleetShips.contains(it.key) }
            if(ships.isEmpty()) return
            reloadShips(Values.storageIndex, ships.values.toList())
        }
    }

    private fun processControlEvents() {
        when (keyManager.mkeyStatus.mcontrolEvent) {
            ControlEventType.INFO -> {
                if (combatGui == null){
                    engine.isPaused = true
                    engine.viewport?.isExternalControl = true
                    combatGui = determineSelectedShip(engine)?.let { font?.let { f -> GuiLayout(it, f) } }
                }else{
                    combatGui = null
                    engine.viewport?.isExternalControl = false
                }
            }
            else -> printMessage("Unrecognized Command")
        }
    }

    private fun printMessage(message: String, frames: Int = Settings.uiDisplayFrames()) {
        drawable = font?.createText(message, baseColor = Color.GREEN)
        textFrameTimer = frames
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