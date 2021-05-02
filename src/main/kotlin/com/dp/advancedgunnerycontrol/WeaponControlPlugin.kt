@file:Suppress("UnusedImport")

package com.dp.advancedgunnerycontrol

import com.dp.advancedgunnerycontrol.typesandvalues.ControlEventType
import com.dp.advancedgunnerycontrol.keyboardinput.KeyStatusManager
import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import org.lazywizard.lazylib.ui.FontException
import org.lazywizard.lazylib.ui.LazyFont
import java.awt.Color


class WeaponControlPlugin : BaseEveryFrameCombatPlugin() {
    private lateinit var engine: CombatEngineAPI
    private var font: LazyFont? = null

    private var drawable: LazyFont.DrawableString? = null
    private val keyManager = KeyStatusManager()

    private var textFrameTimer: Int = 0
    private var isInitialized = false

    companion object {
        fun determineSelectedShip(engine: CombatEngineAPI): ShipAPI? {
            return if (engine.combatUI.isShowingCommandUI && engine.playerShip?.shipTarget?.owner == 0) {
                (engine.playerShip?.shipTarget) ?: engine.playerShip
            } else {
                engine.playerShip
            }
        }
    }

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        super.advance(amount, events)
        if (!isInitialized) return

        if (keyManager.parseInputEvents(events)) {
            processControlEvents()
        }
    }

    private fun processControlEvents() {
        when (keyManager.mkeyStatus.mcontrolEvent) {
            ControlEventType.COMBINE -> combineWeaponGroup()
            ControlEventType.CYCLE -> cycleWeaponGroupMode()
            ControlEventType.INFO -> {
                printShipInfo()
                if (Settings.enablePersistentModes) saveCurrentShipState()
            }
            ControlEventType.RESET -> {
                resetAiManager()
                if (Settings.enablePersistentModes) saveCurrentShipState()
                printShipInfo()
            }
            else -> printMessage("Unrecognized Command (please send bug report)")
        }
    }

    private fun cycleWeaponGroupMode() {
        val ship = determineSelectedShip(engine)
        val aiManager = initOrGetAIManager(ship) ?: return
        val index = keyManager.mkeyStatus.mpressedWeaponGroup - 1
        aiManager.cycleWeaponGroupMode(index)
        if (ship?.weaponGroupsCopy?.size ?: 0 <= index) {
            printMessage("Invalid Weapon Group")
            return
        }
        if (Settings.uiForceFullInfo) {
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
            if (ship_.customData?.containsKey(Values.WEAPON_AI_MANAGER_KEY) == true) {
                ship_.customData?.get(Values.WEAPON_AI_MANAGER_KEY)?.let { unsafeManager ->
                    (unsafeManager as? WeaponAIManager)?.let { return it }
                }
            }
            val aiManager = WeaponAIManager(engine, ship_)
            ship_.fleetMemberId?.let { id ->
                Settings.shipModeStorage.modesByShip[id]?.let {
                    aiManager.refresh(it)
                }
            }

            ship_.setCustomData(Values.WEAPON_AI_MANAGER_KEY, aiManager)
            return aiManager
        }
        return null
    }

    private fun printShipInfo() {
        determineSelectedShip(engine)?.let { ship ->
            val wpAiManager = initOrGetAIManager(ship)
            val shipInfo = ship.variant.fullDesignationWithHullNameForShip
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

    private fun saveCurrentShipState() {
        val ship = determineSelectedShip(engine) ?: return
        initOrGetAIManager(ship)?.let { aiManager ->
            ship.fleetMemberId?.let { id ->
                Settings.shipModeStorage.modesByShip[id] = aiManager.weaponGroupModes
            }
        }
    }

    private fun printMessage(message: String) {
        drawable = font?.createText(message, color = Color.GREEN)
        textFrameTimer = Settings.uiDisplayFrames
    }

    private fun combineWeaponGroup() {
        printMessage("Functionality not yet implemented (combineWeaponGroups)")
    }

    override fun init(engine: CombatEngineAPI?) {
        super.init(engine)
        if (null != engine) {
            try {
                font = LazyFont.loadFont("graphics/fonts/insignia15LTaa.fnt")
            } catch (e: FontException) {
                Global.getLogger(this.javaClass).error("Failed to load font, won't de displaying messages", e)
            }
            if (Settings.isFallbackToDefault) {
                printMessage("Failed to load Settings for ${Values.THIS_MOD_NAME}, check starsector.log for details")
            }

            this.engine = engine
            isInitialized = true
        }
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        super.renderInUICoords(viewport)
        drawable?.apply {
            draw(Settings.uiPositionX.toFloat(), Settings.uiPositionY.toFloat())
            textFrameTimer--
        }
        if ((textFrameTimer <= 0) && (Settings.uiDisplayFrames >= 0)) {
            drawable = null
        }
    }
}