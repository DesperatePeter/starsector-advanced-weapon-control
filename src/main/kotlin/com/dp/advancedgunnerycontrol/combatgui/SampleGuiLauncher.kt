package com.dp.advancedgunnerycontrol.combatgui

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI

/**
 * Class that, when added to engine via e.g. addPlugin, will open/close GUI when specified hotkey is pressed.
 * Extend this class by overriding [constructGui] to construct a GuiObject that extends GuiBase.
 * This class is mainly intended as an example or to quickly get started. In the long term, you probably want to implement
 * your own GUI launching logic in order to be able to customize things.
 * @param hotkey lowercase char representation of hotkey to press to open/close the GUI. Make sure that key is not being used by starsector!
 */
abstract class SampleGuiLauncher(private val hotkey: Char) : BaseEveryFrameCombatPlugin() {
    private var gui: GuiBase? = null

    /**
     * override this to return a new GUI object from this function, e.g. "return new MyGui()"
     */
    abstract fun constructGui(): GuiBase

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        super.advance(amount, events)
        gui?.advance()
        if (wasHotkeyPressed(events)) {
            if (null == gui) {
                gui = constructGui()
                val engine = Global.getCombatEngine() ?: return
                engine.isPaused = true
                engine.viewport?.isExternalControl = true
            } else {
                gui = null
                Global.getCombatEngine()?.viewport?.isExternalControl = false
            }
        }
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        super.renderInUICoords(viewport)
        gui?.render()
    }

    private fun wasHotkeyPressed(events: MutableList<InputEventAPI>?): Boolean {
        events ?: return false
        return events.any { !it.isConsumed && it.isKeyDownEvent && it.eventChar.lowercaseChar() == hotkey }
    }
}