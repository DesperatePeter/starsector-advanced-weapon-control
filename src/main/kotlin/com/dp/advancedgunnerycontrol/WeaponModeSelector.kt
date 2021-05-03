package com.dp.advancedgunnerycontrol

import com.dp.advancedgunnerycontrol.typesandvalues.FMValues
import com.dp.advancedgunnerycontrol.weaponais.Fraction
import com.dp.advancedgunnerycontrol.settings.Settings

/**
 * Essentially a cyclic list iterator for the cycleOrder
 * Note: this class used to use actual iterators, but that caused problems with persisting the data
 */
class WeaponModeSelector() {
    private var currentIndex = 0
    var currentMode = Settings.cycleOrder().first()
        private set

    var fractionOfWeaponsInMode =
        Fraction() // this was kind of an afterthought, so it's not the most fitting in this class

    fun reset(){
        currentIndex = 0
        currentMode = Settings.cycleOrder().first()
    }

    fun cycleMode() {
        if (Settings.cycleOrder().size - 1 <= currentIndex) {
            reset()
        } else { // loop back to start
            currentIndex++
            currentMode = Settings.cycleOrder()[currentIndex]
        }
    }

    /**
     * @return something like "Group 2: [__X_] PD Mode"
     */
    fun currentModeAsString(): String {
        if (Settings.cycleOrder().size <= currentIndex) reset()
        // something like [__X_]
        var positionIndicator: String = " [" + "_".repeat(currentIndex) + "X" +
                "_".repeat(Settings.cycleOrder().size - 1 - currentIndex) + "] "

        return positionIndicator + FMValues.FIRE_MODE_DESCRIPTIONS[currentMode] +
                " ${fractionOfWeaponsInMode.asString()}"
    }
}