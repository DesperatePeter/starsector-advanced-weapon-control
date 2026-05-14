package com.dp.advancedgunnerycontrol

import com.dp.advancedgunnerycontrol.gui.entrypoints.GUIShower
import com.dp.advancedgunnerycontrol.settings.LunaSettingHandler
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.settings.addLunaSettingListener
import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global

class WeaponControlBasePlugin : BaseModPlugin() {

    override fun onApplicationLoad() {
        super.onApplicationLoad()
        Settings.loadSettings()
        logSettings()
        if(LunaSettingHandler.isLunaLibPresent){
            addLunaSettingListener { Settings.loadSettings() }
        }
    }

    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)
        Settings.shipModeStorage.forEach {
            it.purgeIfNecessary<List<String>>(
                keyIsValid = { key -> key is Int },
                valueIsValid = ::isStringListStorageValue,
            )
        }
        Settings.tagStorage.forEach {
            it.purgeIfNecessary<List<String>>(
                keyIsValid = { key -> key is Int },
                valueIsValid = ::isStringListStorageValue,
            )
        }

        Global.getSector()?.addTransientScript(GUIShower())
    }

    private fun logSettings() {
        Global.getLogger(this.javaClass).info("Loaded AdvancedGunneryControl!")
        Settings.printSettings()
        Global.getLogger(this.javaClass).info("Blacklisted weapons: ${Settings.weaponBlacklist}")
    }

    private fun isStringListStorageValue(value: Any?): Boolean =
        value is List<*> && value.all { entry -> entry is String }

}
