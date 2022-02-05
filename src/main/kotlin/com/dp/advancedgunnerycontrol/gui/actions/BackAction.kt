package com.dp.advancedgunnerycontrol.gui.actions

import com.dp.advancedgunnerycontrol.gui.GUIAttributes
import com.dp.advancedgunnerycontrol.gui.Level
import org.lwjgl.input.Keyboard

class BackAction (attributes: GUIAttributes) : GUIAction(attributes) {
    override fun execute() {
        attributes.level = Level.TOP
    }

    override fun getTooltip(): String = ""

    override fun getName(): String = "Back"

    override fun getShortcut(): Int = Keyboard.KEY_ESCAPE
}