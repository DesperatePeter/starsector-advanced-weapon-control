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

/**
 * Refit-screen AGC launcher button component.
 * Keeps the MagicLib button positioned on the vanilla refit UI and forwards
 * clicks to the refit GUI open/close action.
 */
class ButtonHolderPanel(private val action: MagicCombatButtonAction, private val parent: UIPanelAPI, private val isGuiOpen: () -> Boolean)
    : CustomUIPanelPlugin {
    private var position: PositionAPI? = null
    private var button: MagicCombatActionButton? = null
    var panel: UIPanelAPI? = null
    companion object{
        private val font = try {
            LazyFont.loadFont("graphics/fonts/insignia17LTAaa.fnt")
        } catch (e: FontException) {
            Global.getLogger(this::class.java).error("Failed to load font, won't de displaying messages", e)
            null
        }
        fun createButtonInf(x: Float, y: Float): MagicCombatButtonInfo {
            return MagicCombatButtonInfo(
                x, y, 96f, 21f, 0.8f, "Gunnery (${Keyboard.getKeyName(Settings.guiHotkey())})", font, AGCGridLayout.color,
                MagicCombatHoverTooltip(0f, 0f, "Open Advanced Gunnery Control.")
            )
        }

    }
    override fun positionChanged(pos: PositionAPI?) {
        position = pos
    }

    override fun renderBelow(alphaMult: Float) {
    }

    override fun render(alphaMult: Float) {
       if(Settings.showRefitScreenButton() && !isGuiOpen()) button?.render()
    }

    override fun advance(amount: Float) {
        if(button == null){
            position?.let { panelPosition ->
                button = MagicCombatActionButton(action, createButtonInf(panelPosition.x, panelPosition.y))
            }
        }
        if(Settings.showRefitScreenButton() && !isGuiOpen()) button?.advance()
    }

    override fun processInput(events: MutableList<InputEventAPI>?) {
        if (isGuiOpen()) return
        events?.filter {
            !it.isConsumed && it.isKeyDownEvent
        }?.firstOrNull {
            it.eventValue == Settings.guiHotkey()
        }?.let { event ->
            event.consume()
            action.execute()
        }
    }

    override fun buttonPressed(buttonId: Any?) {
    }

    fun close(){
        button = null
        panel?.let { existingPanel ->
            runCatching { parent.removeComponent(existingPanel) }
                .onFailure { ex ->
                    Global.getLogger(ButtonHolderPanel::class.java)
                        .warn("[AGC_REFIT_BUTTON] Failed to remove button holder panel", ex)
                }
        }
        panel = null
    }
}
