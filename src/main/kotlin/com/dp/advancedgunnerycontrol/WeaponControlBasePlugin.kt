package com.dp.advancedgunnerycontrol

import com.dp.advancedgunnerycontrol.typesandvalues.FMValues
import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global

class WeaponControlBasePlugin : BaseModPlugin() {

    override fun onApplicationLoad() {
        super.onApplicationLoad()
        Settings().loadSettings()
        if (!Settings.enablePersistentModes) Settings.shipModeStorage.purge()
        modifyFighterAndMissileModeDescriptionsToIncludeAIType()
        logSettings()
    }

    private fun logSettings() {
        Global.getLogger(this.javaClass).info("Loaded AdvancedGunneryControl!")
        Global.getLogger(this.javaClass).info("Using cycle order:${Settings.cycleOrder}")
        Global.getLogger(this.javaClass).info(
            "Using custom AI: ${Settings.enableCustomAI}, force: ${Settings.forceCustomAI}, recursion lvl :${Settings.customAIRecursionLevel}"
        )
        Global.getLogger(this.javaClass).info("Persistent weapon modes: ${Settings.enablePersistentModes}")
//        Global.getLogger(this.javaClass).info(
//            "Custom AI trigger happiness: ${Settings.customAITriggerHappiness}," +
//                    " friendly fire caution: ${Settings.customAIFriendlyFireCaution}," +
//                    " friendly fire complexity: ${Settings.customAIFriendlyFireComplexity}"
//        )
        Global.getLogger(this.javaClass).info("Weapon blacklist: ${Settings.weaponBlacklist}")
    }

    private fun modifyFighterAndMissileModeDescriptionsToIncludeAIType() {
        val postfix = if (Settings.enableCustomAI) {
            if (Settings.forceCustomAI) {
                " (override AI)"
            } else {
                " (custom AI)"
            }
        } else {
            " (base AI)"
        }
        FMValues.modesAvailableForCustomAI.forEach {
            FMValues.FIRE_MODE_DESCRIPTIONS[it] += postfix
        }
    }
}