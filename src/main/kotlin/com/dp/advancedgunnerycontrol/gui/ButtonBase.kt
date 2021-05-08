package com.dp.advancedgunnerycontrol.gui

import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.ButtonAPI


abstract class ButtonBase<T>(protected var ship: FleetMemberAPI, protected var group : Int, protected var associatedValue : T, var button: ButtonAPI) {
    private var active = false
    protected var sameGroupButtons : List<ButtonBase<T>> = emptyList()
        set(value) {field = value.filter { it.associatedValue != this.associatedValue }}

    public fun executeCallbackIfChecked(){
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

    private fun uncheck(){
        active = false
        button.isChecked = false
    }

    private fun callback(){
        sameGroupButtons.forEach { it.uncheck() }
        onActivate()
    }
    abstract fun onActivate()
}