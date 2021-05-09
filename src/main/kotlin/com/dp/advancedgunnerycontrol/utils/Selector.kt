package com.dp.advancedgunnerycontrol.utils

import com.dp.advancedgunnerycontrol.settings.Settings

abstract class CycleSelectorBase<T>(protected val selectionList: List<T>) {
    protected var currentIndex = 0
    var currentValue = selectionList.first()
        protected set

    constructor(selectionList: List<T>, startingValue: T) : this(selectionList){
        currentValue = startingValue
        currentIndex = selectionList.indexOf(startingValue)
    }

    fun reset(){
        currentIndex = 0
        currentValue = selectionList.first()
    }

    fun cycle() {
        if (selectionList.size - 1 <= currentIndex) {
            reset()
        } else { // loop back to start
            currentIndex++
            currentValue = selectionList[currentIndex]
        }
    }

    abstract fun currentValueAsString(): String
}