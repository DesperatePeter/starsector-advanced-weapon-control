package com.dp.advancedgunnerycontrol.combatgui.buttongroups

abstract class ButtonGroupAction {
    abstract fun execute(data : List<Any>, triggeringButtonData: Any? = null)
}