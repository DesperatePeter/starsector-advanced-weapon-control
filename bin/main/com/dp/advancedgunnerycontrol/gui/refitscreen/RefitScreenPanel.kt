package com.dp.advancedgunnerycontrol.gui.refitscreen

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import org.lwjgl.opengl.GL11
import org.magiclib.combatgui.MagicCombatGuiBase
import java.awt.Color

class RefitScreenPanel(private val gui: MagicCombatGuiBase, private val parent: UIPanelAPI): CustomUIPanelPlugin {
    var panel: UIPanelAPI? = null
    var pos: PositionAPI? = null
    override fun positionChanged(p: PositionAPI?) {
        pos = p
    }

    override fun renderBelow(amount: Float) {
        pos?.let { p ->
            GL11.glPushMatrix()
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glColor4f(0.3f, 0.3f, 0.3f, 0.4f)
            // GL11.glRectf(p.x, p.y, p.x + p.width, p.y + p.height)
            GL11.glRectf(0f, 0f,  Global.getSettings().screenWidth, Global.getSettings().screenHeight)
            GL11.glPopMatrix()
        }
    }

    override fun render(p0: Float) {
        gui.render()
    }

    override fun advance(p0: Float) {
        gui.advance()
    }

    override fun processInput(events: MutableList<InputEventAPI>?) {
        if(panel == null) return
        events?.forEach { event ->
            if(!event.isConsumed && event.isKeyboardEvent || event.isMouseEvent || event.isMouseDownEvent){
                event.consume()
            }
        }
    }

    override fun buttonPressed(p0: Any?) {
    }

    fun close(){
        parent.removeComponent(panel)
    }
}