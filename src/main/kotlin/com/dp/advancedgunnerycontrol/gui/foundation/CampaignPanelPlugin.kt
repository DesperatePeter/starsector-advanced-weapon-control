package com.dp.advancedgunnerycontrol.gui.foundation

import com.dp.advancedgunnerycontrol.gui.style.CampaignBorderMode
import com.dp.advancedgunnerycontrol.gui.style.CampaignPanelType
import com.dp.advancedgunnerycontrol.gui.style.campaignBorderModeByType

import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin
import com.fs.starfarer.api.ui.PositionAPI
import java.awt.Color
import org.lwjgl.opengl.GL11

/**
 * Rectangular panel background/border component.
 * Used for AGC containers, modal backdrops/dialogs, black bordered lists, and
 * transparent input shields.
 */
class CampaignPanelPlugin(
    private val panelType: CampaignPanelType,
    private val lineWidth: Float = 1f,
    private val fillColor: Color? = null,
    private val borderColor: Color? = null,
) : BaseCustomUIPanelPlugin() {
    private var position: PositionAPI? = null

    override fun positionChanged(position: PositionAPI?) {
        this.position = position
    }

    override fun renderBelow(alphaMult: Float) {
        val color = fillColor ?: return
        val pos = position ?: return
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT or GL11.GL_COLOR_BUFFER_BIT)
        GL11.glPushMatrix()
        try {
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glColor4f(
                color.red / 255f,
                color.green / 255f,
                color.blue / 255f,
                color.alpha / 255f * alphaMult
            )
            GL11.glBegin(GL11.GL_QUADS)
            GL11.glVertex2f(pos.x, pos.y)
            GL11.glVertex2f(pos.x + pos.width, pos.y)
            GL11.glVertex2f(pos.x + pos.width, pos.y + pos.height)
            GL11.glVertex2f(pos.x, pos.y + pos.height)
            GL11.glEnd()
        } finally {
            GL11.glPopMatrix()
            GL11.glPopAttrib()
        }
    }

    override fun render(alphaMult: Float) {
        val mode = campaignBorderModeByType.getValue(panelType)
        if (mode == CampaignBorderMode.NONE) return
        val pos = position ?: return
        val outline = borderColor ?: panelType.outlineColor
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT or GL11.GL_COLOR_BUFFER_BIT or GL11.GL_LINE_BIT)
        GL11.glPushMatrix()
        try {
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glColor4f(
                outline.red / 255f,
                outline.green / 255f,
                outline.blue / 255f,
                0.95f * alphaMult
            )
            GL11.glBegin(GL11.GL_LINES)
            GL11.glLineWidth(lineWidth)

            if (mode == CampaignBorderMode.FULL || mode == CampaignBorderMode.SIDES || mode == CampaignBorderMode.SIDES_AND_BOTTOM) {
                GL11.glVertex2f(pos.x, pos.y + pos.height)
                GL11.glVertex2f(pos.x, pos.y)
                GL11.glVertex2f(pos.x + pos.width, pos.y + pos.height)
                GL11.glVertex2f(pos.x + pos.width, pos.y)
            }

            if (mode == CampaignBorderMode.FULL) {
                GL11.glVertex2f(pos.x, pos.y + pos.height)
                GL11.glVertex2f(pos.x + pos.width, pos.y + pos.height)
            }

            if (mode == CampaignBorderMode.FULL || mode == CampaignBorderMode.SIDES_AND_BOTTOM) {
                GL11.glVertex2f(pos.x, pos.y)
                GL11.glVertex2f(pos.x + pos.width, pos.y)
                GL11.glVertex2f(pos.x, pos.y)
            }

            GL11.glEnd()
        } finally {
            GL11.glPopMatrix()
            GL11.glPopAttrib()
        }
    }
}
