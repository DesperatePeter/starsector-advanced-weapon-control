package com.dp.advancedgunnerycontrol
import com.dp.advancedgunnerycontrol.enums.ControlEventType
import com.dp.advancedgunnerycontrol.keyboardinput.KeyStatusManager
import com.dp.advancedgunnerycontrol.WeaponAIManager // to suppress false positive
import com.fs.starfarer.api.Global

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import org.lazywizard.lazylib.ui.FontException
import org.lazywizard.lazylib.ui.LazyFont
import java.awt.Color


class WeaponControlPlugin : BaseEveryFrameCombatPlugin() {
    private val textDisplayTimeInFrames = 150

    private lateinit var engine: CombatEngineAPI
    private lateinit var weaponAIManager: WeaponAIManager
    private var font: LazyFont? = null

    private var drawable: LazyFont.DrawableString? = null
    private val keyManager = KeyStatusManager()

    private var textFrameTimer: Int = 0
    private var isInitialized = false
    private var shipID = ""



    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        super.advance(amount, events)
        if (!isInitialized) return

        detectShipChange()

        if (!keyManager.parseInputEvents(events)) return

        when (keyManager.mkeyStatus.mcontrolEvent) {
            ControlEventType.COMBINE -> combineWeaponGroup()
            ControlEventType.CYCLE -> cycleWeaponGroupMode()
            else -> printMessage("Unrecognized Command")
        }
    }

    private fun detectShipChange(){
        engine.playerShip?.let {
            if (shipID != it.id){
                shipID = it.id
                weaponAIManager.reset()
            }
        }
    }

    private fun cycleWeaponGroupMode() {
        val index = keyManager.mkeyStatus.mpressedWeaponGroup - 1
        weaponAIManager.cycleWeaponGroupMode(index)
        printMessage(weaponAIManager.getFireModeDescription(index))
    }

    private fun printMessage(message: String) {
        drawable = font?.createText(message, color = Color.GREEN)
        textFrameTimer = textDisplayTimeInFrames
    }


    private fun combineWeaponGroup() {
        printMessage("Functionality not yet implemented (combineWeaponGroups)")
    }

    override fun init(engine: CombatEngineAPI?) {
        super.init(engine)
        if (null != engine) {
            try {
                font = LazyFont.loadFont("graphics/fonts/insignia15LTaa.fnt")
            } catch (e: FontException){
                Global.getLogger(this.javaClass).error("Failed to load font, won't de displaying messages", e)
            }
            if(Settings.isFallbackToDefault){
                printMessage("Failed to load Settings for ${Values.THIS_MOD_NAME}, check starsector.log for details")
            }

            this.engine = engine
            weaponAIManager = WeaponAIManager(engine)
            isInitialized = true
        }
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        super.renderInUICoords(viewport)
        drawable?.apply {
            draw(600f, 600f)
            textFrameTimer--
        }
        if (textFrameTimer <= 0) {
            drawable = null
        }
    }
}