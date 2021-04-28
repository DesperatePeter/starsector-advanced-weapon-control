package com.dp.advancedgunnerycontrol

import com.dp.advancedgunnerycontrol.enums.FireMode
import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global

public class WeaponControlBasePlugin : BaseModPlugin() {

    override fun onApplicationLoad() {
        super.onApplicationLoad()
        Global.getLogger(this.javaClass).info("Loaded AdvancedGunneryControl!")
        Settings().loadSettings()
        modifyFighterAndMissileModeDescriptionsToIncludeAIType()
        Global.getLogger(this.javaClass).info("Using cycle order:${Settings.cycleOrder}")
        Global.getLogger(this.javaClass).info(
            "Using custom AI: ${Settings.enableCustomAI}, force: ${Settings.forceCustomAI}, recursion lvl :${Settings.customAIRecursionLevel}"
        )
    }

    private fun modifyFighterAndMissileModeDescriptionsToIncludeAIType() {
        val postfix = if ( Settings.enableCustomAI ) {
            if ( Settings.forceCustomAI ) {
                "(override AI)"
            } else {
                "(custom AI)"
            }
        } else {
            "(base AI)"
        }
        Values.FIRE_MODE_DESCRIPTIONS[FireMode.FIGHTER] += postfix
        Values.FIRE_MODE_DESCRIPTIONS[FireMode.MISSILE] += postfix
    }
}