package com.dp.advancedgunnerycontrol.gui.refitscreen

import com.dp.advancedgunnerycontrol.combatgui.agccombatgui.AGCGridLayout
import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import org.lazywizard.lazylib.ui.FontException
import org.lazywizard.lazylib.ui.LazyFont
import org.lwjgl.input.Keyboard
import org.magiclib.combatgui.buttons.MagicCombatActionButton
import org.magiclib.combatgui.buttons.MagicCombatButtonAction
import org.magiclib.combatgui.buttons.MagicCombatButtonInfo
import org.magiclib.combatgui.buttons.MagicCombatHoverTooltip

class ButtonHolderPanel(private val action: MagicCombatButtonAction, private val parent: UIPanelAPI, private val isGuiOpen: () -> Boolean)
    : CustomUIPanelPlugin {
    private var position: PositionAPI? = null
    private var button: MagicCombatActionButton? = null
    var panel: UIPanelAPI? = null
    private var wasGuiRecentlyOpened = false
    private var lastEventTime = 0L
    private val isRelevantEvent
        get() = Keyboard.getEventNanoseconds() > lastEventTime
    private val isAgcHotkey: Boolean
        get() {
            return (Keyboard.getEventCharacter().lowercaseChar() == Settings.guiHotkey()) && isRelevantEvent
        }
    private val isEsc
        get() = Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) && isRelevantEvent

    private val shouldClose
        get() = (isAgcHotkey || isEsc) && !wasGuiRecentlyOpened && isGuiOpen()
    companion object{
        private val font = try {
            LazyFont.loadFont("graphics/fonts/insignia17LTAaa.fnt")
        } catch (e: FontException) {
            Global.getLogger(this::class.java).error("Failed to load font, won't de displaying messages", e)
            null
        }
        fun createButtonInf(x: Float, y: Float): MagicCombatButtonInfo {
            return MagicCombatButtonInfo(
                x, y, 96f, 21f, 0.8f, "Gunnery (${Settings.guiHotkey().uppercaseChar()})", font, AGCGridLayout.color,
                MagicCombatHoverTooltip(0f, 0f, "")
            )
        }

    }
    override fun positionChanged(pos: PositionAPI?) {
        position = pos
        Keyboard.KEY_J
    }

    override fun renderBelow(p0: Float) {
    }

    override fun render(p0: Float) {
       button?.render()
    }

    override fun advance(p0: Float) {
        if(button == null){
            position?.let { p ->
                button = MagicCombatActionButton(action, createButtonInf(p.x, p.y))
            }
        }
        button?.advance()
        if(shouldClose) action.execute()
        lastEventTime = Keyboard.getEventNanoseconds()
    }

    override fun processInput(events: MutableList<InputEventAPI>?) {
        wasGuiRecentlyOpened = false
        events?.filter {
            !it.isConsumed && it.isKeyDownEvent
        }?.firstOrNull {
            it.eventChar.lowercaseChar() == Settings.guiHotkey()
        }?.let { event ->
            event.consume()
            action.execute()
            wasGuiRecentlyOpened = true
        }
    }

    override fun buttonPressed(p0: Any?) {
    }

    fun close(){
        button = null
        parent.removeComponent(panel)
    }
}