package com.dp.advancedgunnerycontrol.gui

import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.ButtonAPI


abstract class ButtonBase<T>(protected var ship: FleetMemberAPI, protected var group : Int, protected var associatedValue : T, var button: ButtonAPI) {
    protected var active = false
    protected var sameGroupButtons : List<ButtonBase<T>> = emptyList()
        set(value) {field = value.filter { it.associatedValue != this.associatedValue }}

    open fun executeCallbackIfChecked(){
        if (!active && button.isChecked){
            check()
        }
        button.isChecked = active
    }

    protected fun check(){
        callback()
        active = true
        button.isChecked = true
    }

    fun disable(){
        button.isEnabled = false
    }

    protected fun uncheck(){
        active = false
        button.isChecked = false
    }

    private fun callback(){
        sameGroupButtons.forEach { it.uncheck() }
        onActivate()
    }
    abstract fun onActivate()
}