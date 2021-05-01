package com.dp.advancedgunnerycontrol

import com.dp.advancedgunnerycontrol.enums.FireMode
import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import wisp.questgiver.wispLib.QuestGiver

public class WeaponControlBasePlugin : BaseModPlugin() {

    init {
        // only used for
        QuestGiver.initialize(Values.THIS_MOD_NAME)
    }

    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)
        QuestGiver.onGameLoad()
    }

    override fun onApplicationLoad() {
        super.onApplicationLoad()
        Settings().loadSettings()
        if (!Settings.enablePersistentModes) Settings.shipModeStorage.purge()
        modifyFighterAndMissileModeDescriptionsToIncludeAIType()
        logSettings()
    }

    private fun logSettings(){
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
        val postfix = if ( Settings.enableCustomAI ) {
            if ( Settings.forceCustomAI ) {
                " (override AI)"
            } else {
                " (custom AI)"
            }
        } else {
            " (base AI)"
        }
        Values.FIRE_MODE_DESCRIPTIONS[FireMode.FIGHTER] += postfix
        Values.FIRE_MODE_DESCRIPTIONS[FireMode.MISSILE] += postfix
    }
}