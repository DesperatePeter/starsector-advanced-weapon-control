package com.dp.advancedgunnerycontrol

/**
 * Essentially a cyclic list iterator for the cycleOrder
 */
class WeaponModeSelector {
    private var currentIterator = WeaponControlBasePlugin.cycleOrder.iterator()
    var currentMode = currentIterator.next()
        private set
    var currentIndex = 0 // This is only used for the visual representation

    fun cycleMode() {
        if(currentIterator.hasNext()){
            currentMode = currentIterator.next()
            currentIndex++
        }else{ // loop back to start
            currentIterator = WeaponControlBasePlugin.cycleOrder.iterator()
            currentMode = currentIterator.next()
            currentIndex = 0
        }
    }

    /**
     * @return something like "Group 2: [__X_] PD Mode"
     */
    fun currentModeAsString(weaponGroupIndex : Int) : String{
        // something like [__X_]
        var positionIndicator : String= " [" + "_".repeat(currentIndex) + "X" +
                "_".repeat(WeaponControlBasePlugin.cycleOrder.size - 1 - currentIndex) + "] "

        return "Group ${weaponGroupIndex+1}: " + positionIndicator + Values.FIRE_MODE_DESCRIPTIONS[currentMode]
    }
}