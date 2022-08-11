package com.dp.advancedgunnerycontrol.combatgui.buttons

import org.lazywizard.lazylib.ui.LazyFont
import java.awt.Color

class ActionButton(private val action : ButtonAction? = null, info: ButtonInfo
) : ButtonBase(info)
{
    override fun advance() : Boolean{
        if(isClicked()){
            action?.run { execute() }
            isActive = true
            return true
        }
        isActive = false
        return false
    }
}