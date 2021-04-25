package com.dp.advancedgunnerycontrol

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import org.lazywizard.lazylib.ui.LazyFont
import java.awt.Color


class WeaponControlPlugin : BaseEveryFrameCombatPlugin() {
    private val textDisplayTimeInFrames = 100

    private lateinit var engine: CombatEngineAPI
    private lateinit var weaponAIManager: WeaponAIManager
    private lateinit var font: LazyFont

    private var drawable: LazyFont.DrawableString? = null
    private var textFrameTimer: Int = 0
    private var isInitialized = false


    private val keyManager = KeyStatusManager()
    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        super.advance(amount, events)
        if (!isInitialized) return

        if (!keyManager.parseInputEvents(events)) return

        when (keyManager.mkeyStatus.mcontrolEvent) {
            ControlEventType.COMBINE -> combineWeaponGroup()
            ControlEventType.CYCLE -> cycleWeaponGroupMode()
            else -> printMessage("Unrecognized Command")
        }
    }

    private fun cycleWeaponGroupMode() {
        val index = keyManager.mkeyStatus.mpressedWeaponGroup - 1
        weaponAIManager.cycleWeaponGroupMode(index)
        printMessage("Weapon Group " + weaponAIManager.getFireModeDescription(index))
    }

    private fun printMessage(message: String) {
        drawable = font.createText(message, color = Color.GREEN)
        textFrameTimer = textDisplayTimeInFrames
    }


    private fun combineWeaponGroup() {
        printMessage("Functionality not yet implemented (combineWeaponGroups")
    }

    override fun init(engine: CombatEngineAPI?) {
        super.init(engine)
        if (null != engine) {
            font = LazyFont.loadFont("graphics/fonts/insignia15LTaa.fnt")
            this.engine = engine
            weaponAIManager = WeaponAIManager(engine)
            isInitialized = true
        }
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        super.renderInUICoords(viewport)
        if (null != drawable) {
            drawable?.draw(600f, 600f)
            textFrameTimer--
        }

        if (textFrameTimer <= 0) {
            drawable = null
        }
    }
}