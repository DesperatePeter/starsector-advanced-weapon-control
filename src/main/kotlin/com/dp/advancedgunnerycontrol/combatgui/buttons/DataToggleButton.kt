package com.dp.advancedgunnerycontrol.combatgui.buttons

import org.lazywizard.lazylib.ui.LazyFont
import java.awt.Color

class DataToggleButton(private val data : Any, info: ButtonInfo
) : ButtonBase(info) {
    override fun advance() : Boolean {
        if (isClicked()){
            isActive = !isActive
            return true
        }
        return false
    }
    fun getData() : Any?{
        return if (isActive) data else null
    }
}