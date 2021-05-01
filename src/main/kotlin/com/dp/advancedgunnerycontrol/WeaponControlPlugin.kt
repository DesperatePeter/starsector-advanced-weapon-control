@file:Suppress("UnusedImport")

package com.dp.advancedgunnerycontrol

import com.dp.advancedgunnerycontrol.enums.ControlEventType
import com.dp.advancedgunnerycontrol.keyboardinput.KeyStatusManager
import com.dp.advancedgunnerycontrol.WeaponAIManager
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
    // private lateinit var currentWeaponAIManager: WeaponAIManager
    private var font: LazyFont? = null

    private var drawable: LazyFont.DrawableString? = null
    private val keyManager = KeyStatusManager()
    private var currentShip: ShipAPI? = null

    private var textFrameTimer: Int = 0
    private var isInitialized = false
    private var shipID = ""

    companion object{
        fun determineSelectedShip(engine: CombatEngineAPI) : ShipAPI? {

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

        detectAndProcessShipChange()

        if (!keyManager.parseInputEvents(events)) return

        when (keyManager.mkeyStatus.mcontrolEvent) {
            ControlEventType.COMBINE -> combineWeaponGroup()
            ControlEventType.CYCLE -> cycleWeaponGroupMode()
            ControlEventType.INFO -> printShipInfo()
            else -> printMessage("Unrecognized Command (please send bug report)")
        }
    }

    private fun detectAndProcessShipChange() {
//        engine.playerShip?.let { ship ->
//            if (shipID == ship.fleetMemberId) return
//
//            shipID = ship.fleetMemberId
//            currentWeaponAIManager.reset()
//            currentShip?.setCustomData(Values.WEAPON_AI_MANAGER_KEY, currentWeaponAIManager)
//            currentWeaponAIManager = getWeaponAIManagerFromShip(ship) ?: WeaponAIManager(engine, ship)
//            currentWeaponAIManager.reset()
//            currentShip = ship
//            currentShip?.setCustomData(Values.WEAPON_AI_MANAGER_KEY, currentWeaponAIManager)
//        }
    }

    private fun getWeaponAIManagerFromShip(ship: ShipAPI?): WeaponAIManager? {
        ship?.customData?.get(Values.WEAPON_AI_MANAGER_KEY)?.let { unsafeManager ->
            return (unsafeManager as? WeaponAIManager)
        }
        return null
    }

    private fun cycleWeaponGroupMode() {
        val aiManager = getWeaponAIManagerFromShip(determineSelectedShip(engine)) ?: return
        val index = keyManager.mkeyStatus.mpressedWeaponGroup - 1
        aiManager.cycleWeaponGroupMode(index)
        if(Settings.uiForceFullInfo){
            printShipInfo()
        }else{
            printMessage(aiManager.getFireModeDescription(index))
        }

    }

    private fun initializeWeaponAiManagerForShip(ship: ShipAPI?){
        if (ship?.customData?.containsKey(Values.WEAPON_AI_MANAGER_KEY) == true) return
        val aiManager = WeaponAIManager(engine, ship)
        Settings.shipModeStorage.modesByShip[ship?.variant?.fullDesignationWithHullNameForShip]?.let {
            aiManager.refresh(it)
        }
        ship?.setCustomData(Values.WEAPON_AI_MANAGER_KEY, aiManager)
    }

    private fun printShipInfo() {
        determineSelectedShip(engine)?.let { ship ->
            initializeWeaponAiManagerForShip(ship)
            val wpAiManager = getWeaponAIManagerFromShip(ship)
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
            wpAiManager?.let {
                Settings.shipModeStorage.modesByShip[ship.variant.fullDesignationWithHullNameForShip] = it.weaponGroupModes
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
            // currentWeaponAIManager = WeaponAIManager(engine, null)
            // TODO: weapon AI manager only on ships
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