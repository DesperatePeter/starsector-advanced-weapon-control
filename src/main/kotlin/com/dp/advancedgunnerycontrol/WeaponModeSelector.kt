package com.dp.advancedgunnerycontrol

/**
 * Essentially a cyclic list iterator for the cycleOrder
 */
class WeaponModeSelector {
    private var currentIterator = WeaponControlBasePlugin.cycleOrder.iterator()
    var currentMode = WeaponControlBasePlugin.cycleOrder.first()
        private set
    var currentIndex = 0 // This is only used for the visual representation

    fun cycleMode() {
        if(currentIterator.hasNext()){
            currentMode = currentIterator.next()
            currentIndex++
        }else{ // loop back to start
            currentMode = WeaponControlBasePlugin.cycleOrder.first()
            currentIterator = WeaponControlBasePlugin.cycleOrder.iterator()
            currentIndex = 0
        }
    }

    fun asString(){

    }
}