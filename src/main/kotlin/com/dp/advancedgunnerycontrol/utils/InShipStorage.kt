package com.dp.advancedgunnerycontrol.utils

class InShipTagStorage {
    // Map<storageIndex, Map<WeaponGroupIndex, List<Tags>>>
    var tagsByIndex : MutableMap<Int, MutableMap<Int, List<String>>> = mutableMapOf()
}

class InShipShipModeStorage {
    // Map<storageIndex, List<ShipModes>>
    var modes: MutableMap<Int,MutableList<String>> = mutableMapOf()
}