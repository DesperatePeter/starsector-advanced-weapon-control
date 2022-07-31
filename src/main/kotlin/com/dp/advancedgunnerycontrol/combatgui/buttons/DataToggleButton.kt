package com.dp.advancedgunnerycontrol.combatgui.buttons

class DataToggleButton(val data : Any, info: ButtonInfo
) : ButtonBase(info) {
    override fun advance() : Boolean {
        if (isClicked()){
            isActive = !isActive
            return true
        }
        return false
    }
    fun getDataIfActive() : Any?{
        return if (isActive) data else null
    }
}