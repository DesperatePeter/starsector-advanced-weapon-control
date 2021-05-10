package com.dp.advancedgunnerycontrol.utils

import com.dp.advancedgunnerycontrol.typesandvalues.FMValues
import com.dp.advancedgunnerycontrol.weaponais.Fraction
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.FireMode

/**
 * Essentially a cyclic list iterator for the cycleOrder
 * Note: this class used to use actual iterators, but that caused problems with persisting the data
 */
class WeaponModeSelector : CycleSelectorBase<FireMode>{
    // this was kind of an afterthought, so it's not the most fitting in this class
    var fractionOfWeaponsInMode = Fraction()

    constructor() : super(Settings.cycleOrder())

    constructor(mode: FireMode?) : super(Settings.cycleOrder(), (mode ?: FireMode.DEFAULT))

    /**
     * @return something like "Group 2: [__X_] PD Mode"
     */
    override fun currentValueAsString(): String {
        if (Settings.cycleOrder().size <= currentIndex) reset()
        // something like [__X_]
        var positionIndicator: String = " [" + "_".repeat(currentIndex) + "X" +
                "_".repeat(Settings.cycleOrder().size - 1 - currentIndex) + "] "

        return positionIndicator + FMValues.FIRE_MODE_DESCRIPTIONS[currentValue] +
                " ${fractionOfWeaponsInMode.asString()}"
    }
}