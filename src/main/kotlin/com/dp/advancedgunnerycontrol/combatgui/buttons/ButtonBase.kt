package com.dp.advancedgunnerycontrol.combatgui.buttons

import com.fs.starfarer.api.Global
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * Only use this directly if you know what you are doing
 */
abstract class ButtonBase(val info: ButtonInfo) {
    var isActive = false
    var isDisabled = false
    var wasMouseReleased = true
    var wasHover = false
    abstract fun advance(): Boolean
    private fun determineColor(): Color {
        return if (isDisabled) Color.GRAY else info.color
    }

    fun render() {
        val buttonText = info.font?.createText(info.txt, baseColor = determineColor())
        buttonText?.draw(info.x, info.y + info.h)

        renderGLButton()

        if (isHover()) {
            val tooltipText = info.font?.createText(info.tooltip.txt, baseColor = determineColor())
            tooltipText?.draw(info.tooltip.x, info.tooltip.y)
            if (!wasHover) {
                Global.getSoundPlayer().playUISound("ui_button_mouseover", 1f, 1f)
                wasHover = true
            }
        } else {
            wasHover = false
        }

    }

    protected fun isClicked(): Boolean {
        if (Mouse.isButtonDown(0) && isHover() && wasMouseReleased) {
            wasMouseReleased = false
            if (isDisabled) {
                Global.getSoundPlayer().playUISound("ui_button_disabled_pressed", 1f, 1f)
                return false
            }
            Global.getSoundPlayer().playUISound("ui_button_pressed", 1f, 1f)
            return true
        }
        wasMouseReleased = !Mouse.isButtonDown(0)
        return false
    }

    protected fun isHover(): Boolean {
        val ssm = Global.getSettings().screenScaleMult
        val mx = Mouse.getX().toFloat() / ssm
        val my = Mouse.getY().toFloat() / ssm
        return mx >= info.x && mx <= info.x + info.w && my >= info.y && my <= info.y + info.h
    }

    private fun renderGLButton() {
        GL11.glPushMatrix()
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        val alpha = determineColor().alpha.toFloat() / 255f * if (isActive) 1f else 0.2f
        GL11.glColor4f(
            determineColor().red.toFloat() / 255f, determineColor().green.toFloat() / 255f,
            determineColor().blue.toFloat() / 255f, alpha
        )
        GL11.glBegin(GL11.GL_LINES)
        val width = if (isActive) 1f else 0.5f
        GL11.glLineWidth(width)
        GL11.glVertex2f(info.x, info.y + info.h)
        GL11.glVertex2f(info.x + info.w, info.y + info.h)
        GL11.glVertex2f(info.x + info.w, info.y + info.h)
        GL11.glVertex2f(info.x + info.w, info.y)
        GL11.glVertex2f(info.x + info.w, info.y)
        GL11.glVertex2f(info.x, info.y)
        GL11.glVertex2f(info.x, info.y)
        GL11.glVertex2f(info.x, info.y + info.h)
        GL11.glEnd()
        GL11.glPopMatrix()
    }
}