package com.dp.advancedgunnerycontrol.hullmods

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.ShowDefaultVisual
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.PositionAPI
import org.lwjgl.opengl.GL11
import java.awt.Color

class Size{
    var x = 0
    var y = 0
    var h = 0
    var z = 0
}

class AGCUI : CustomUIPanelPlugin{
    protected var p: PositionAPI? = null
    private var color: Color = Color.GREEN


    override fun positionChanged(p0: PositionAPI?) {
        p = p0
    }

    override fun renderBelow(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun render(p0: Float) {
        GL11.glPushMatrix()
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glColor4f(color.red/255f, color.green/255f, color.blue/255f, color.alpha/255f*p0)
        GL11.glBegin(GL11.GL_LINES)
        p?.let {
            GL11.glVertex2f(it.x, it.height + it.y)
            GL11.glVertex2f(it.x + it.width, it.height + it.y)
            GL11.glVertex2f(it.x + it.width, it.y)
            GL11.glVertex2f(it.x, it.y)
            GL11.glVertex2f(it.x, it.y)
        }

    }

    override fun advance(p0: Float) {
    }

    override fun processInput(p0: MutableList<InputEventAPI>?) {
    }

}

class AGCHullmod : BaseHullMod() {
    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)
//        val dialogue = ShowDefaultVisual()
//        dialogue.exe
//        dialogue.execute("agcinterface", )
        val gui = AGCUI()
        gui.render(1.0f)
    }
}