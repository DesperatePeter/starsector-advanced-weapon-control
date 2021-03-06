package com.dp.advancedgunnerycontrol

import com.dp.advancedgunnerycontrol.gui.GUIShower
import com.dp.advancedgunnerycontrol.typesandvalues.FMValues
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.utils.FireModeStorage
import com.dp.advancedgunnerycontrol.utils.ShipModeStorage
import com.dp.advancedgunnerycontrol.utils.SuffixStorage
import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global

class WeaponControlBasePlugin : BaseModPlugin() {

    override fun onApplicationLoad() {
        super.onApplicationLoad()
        Settings.loadSettings()
        modifyFighterAndMissileModeDescriptionsToIncludeAIType()
        logSettings()
    }

    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)

        ShipModeStorage.forEach {
            it.purgeIfNecessary()
        }
        FireModeStorage.forEach {
            it.purgeIfNecessary()
        }
        SuffixStorage.forEach {
            it.purgeIfNecessary()
        }

        if (Settings.enableTextInterface()){
            Global.getSector().addTransientScript(GUIShower())
        }
    }

    private fun logSettings() {
        Global.getLogger(this.javaClass).info("Loaded AdvancedGunneryControl!")
        Settings.printSettings()
        Global.getLogger(this.javaClass).info("Blacklisted weapons: ${Settings.weaponBlacklist}")
    }

    private fun modifyFighterAndMissileModeDescriptionsToIncludeAIType() {
        val postfix = if (Settings.enableCustomAI()) {
            if (Settings.forceCustomAI()) {
                " (override AI)"
            } else {
                " (custom AI)"
            }
        } else {
            " (base AI)"
        }
        FMValues.FIRE_MODE_DESCRIPTIONS.keys.forEach {
            FMValues.FIRE_MODE_DESCRIPTIONS[it] += if (it in FMValues.modesAvailableForCustomAI) postfix else " (base AI)"
        }
    }
}