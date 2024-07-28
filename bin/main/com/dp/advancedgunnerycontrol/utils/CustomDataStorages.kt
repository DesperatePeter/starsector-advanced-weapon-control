package com.dp.advancedgunnerycontrol.utils

import com.dp.advancedgunnerycontrol.weaponais.tags.WeaponAITagBase
import java.lang.ref.WeakReference

class InShipTagStorage {
    // Map<storageIndex, Map<WeaponGroupIndex, List<Tags>>>
    var tagsByIndex: MutableMap<Int, MutableMap<Int, List<String>>> = mutableMapOf()
}

class InShipShipModeStorage {
    // Map<storageIndex, List<ShipModes>>
    var modes: MutableMap<Int, MutableList<String>> = mutableMapOf()
}

class InEngineTagStorage {
    var tags: MutableSet<WeakReference<WeaponAITagBase>> = mutableSetOf()
}