package com.dp.advancedgunnerycontrol.gui.actions

import com.dp.advancedgunnerycontrol.gui.GUIAttributes
import org.lwjgl.input.Keyboard

class ExitAction (attributes: GUIAttributes) : GUIAction(attributes) {
    override fun execute() {
        attributes.dialog?.dismiss()
    }

    override fun getTooltip(): String = ""

    override fun getName(): String = "Exit"

    override fun getShortcut(): Int = Keyboard.KEY_ESCAPE
}