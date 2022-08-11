package com.dp.advancedgunnerycontrol.combatgui

abstract class ButtonGroupAction {
    abstract fun execute(data : List<Any>, triggeringButtonData: Any? = null)
}