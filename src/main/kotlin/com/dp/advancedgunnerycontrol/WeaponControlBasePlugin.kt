package com.dp.advancedgunnerycontrol

import com.dp.advancedgunnerycontrol.gui.GUIShower
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.utils.ShipModeStorage
import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global

class WeaponControlBasePlugin : BaseModPlugin() {

    override fun onApplicationLoad() {
        super.onApplicationLoad()
        Settings.loadSettings()
        logSettings()
    }

    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)

        ShipModeStorage.forEach {
            it.purgeIfNecessary<List<String>>()
        }
        Settings.tagStorage.forEach {
            it.purgeIfNecessary<List<String>>()
        }

        Global.getSector().addTransientScript(GUIShower())
    }

    private fun logSettings() {
        Global.getLogger(this.javaClass).info("Loaded AdvancedGunneryControl!")
        Settings.printSettings()
        Global.getLogger(this.javaClass).info("Blacklisted weapons: ${Settings.weaponBlacklist}")
    }

}