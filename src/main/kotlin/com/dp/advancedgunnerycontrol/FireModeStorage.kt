package com.dp.advancedgunnerycontrol

import wisp.questgiver.wispLib.PersistentMapData

class FireModeStorage (){
    var modesByShip : MutableMap<String, MutableMap<Int, WeaponModeSelector>> = PersistentMapData<String, MutableMap<Int, WeaponModeSelector>>(
        "weaponModes"
    ).withDefault { mutableMapOf() }

    fun purge(){
        modesByShip = PersistentMapData<String, MutableMap<Int, WeaponModeSelector>>(
            "weaponModes"
        ).withDefault { mutableMapOf() }
    }
}