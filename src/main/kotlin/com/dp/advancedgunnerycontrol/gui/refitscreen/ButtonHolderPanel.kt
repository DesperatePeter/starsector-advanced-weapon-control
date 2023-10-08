package com.dp.advancedgunnerycontrol.gui.refitscreen

import com.dp.advancedgunnerycontrol.combatgui.agccombatgui.AGCGridLayout
import com.dp.advancedgunnerycontrol.combatgui.buttons.ActionButton
import com.dp.advancedgunnerycontrol.combatgui.buttons.ButtonAction
import com.dp.advancedgunnerycontrol.combatgui.buttons.ButtonInfo
import com.dp.advancedgunnerycontrol.combatgui.buttons.HoverTooltip
import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import org.lazywizard.lazylib.ui.FontException
import org.lazywizard.lazylib.ui.LazyFont

class ButtonHolderPanel(private val action: ButtonAction, private val parent: UIPanelAPI): CustomUIPanelPlugin {
    private var position: PositionAPI? = null
    private var button: ActionButton? = null
    var panel: UIPanelAPI? = null
    companion object{
        val font = try {
            LazyFont.loadFont("graphics/fonts/insignia17LTAaa.fnt")
        } catch (e: FontException) {
            Global.getLogger(this::class.java).error("Failed to load font, won't de displaying messages", e)
            null
        }
        fun createButtonInf(x: Float, y: Float): ButtonInfo{
            return ButtonInfo(
                x, y, 96f, 21f, 0.8f, "Gunnery (${Settings.guiHotkey().uppercaseChar()})", font, AGCGridLayout.color,
                HoverTooltip(0f, 0f, "")
            )
        }

    }
    override fun positionChanged(pos: PositionAPI?) {
        position = pos
    }

    override fun renderBelow(p0: Float) {
    }

    override fun render(p0: Float) {
       button?.render()
    }

    override fun advance(p0: Float) {
        if(button == null){
            position?.let { p ->
                button = ActionButton(action, createButtonInf(p.x, p.y))
            }
        }
        button?.advance()
    }

    override fun processInput(p0: MutableList<InputEventAPI>?) {
    }

    override fun buttonPressed(p0: Any?) {
    }

    fun close(){
        button = null
        parent.removeComponent(panel)
    }
}