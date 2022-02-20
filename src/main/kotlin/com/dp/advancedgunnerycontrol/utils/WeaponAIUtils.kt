package com.dp.advancedgunnerycontrol.utils

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.dp.advancedgunnerycontrol.typesandvalues.createTags
import com.dp.advancedgunnerycontrol.weaponais.TagBasedAI
import com.fs.starfarer.api.combat.ShipAPI

fun applyTagsToWeaponGroup(ship: ShipAPI, groupIndex: Int, tags: List<String>) {
    val weaponGroup = ship.weaponGroupsCopy[groupIndex]
    val plugins = weaponGroup.aiPlugins

    for (i in 0 until plugins.size) {
        if (plugins[i] !is TagBasedAI) {
            plugins[i] = TagBasedAI(plugins[i])
        }
        (plugins[i] as? TagBasedAI)?.tags = createTags(tags, plugins[i].weapon).toMutableList()
    }
}

fun persistTags(shipId: String, groupIndex: Int, loadoutIndex: Int, tags: List<String>){
    if(!Settings.tagStorage[loadoutIndex].modesByShip.containsKey(shipId)){
        Settings.tagStorage[loadoutIndex].modesByShip[shipId] = mutableMapOf()
    }
    Settings.tagStorage[loadoutIndex].modesByShip[shipId]?.set(groupIndex, tags)
}

fun saveTagsInShip(ship: ShipAPI, groupIndex: Int, tags: List<String>){
    if(!ship.customData.containsKey(Values.CUSTOM_SHIP_DATA_TAG_KEY)){
        ship.customData[Values.CUSTOM_SHIP_DATA_TAG_KEY] = InShipTagStorage()
    }
    (ship.customData[Values.CUSTOM_SHIP_DATA_TAG_KEY] as? InShipTagStorage)?.tagsByIndex?.set(groupIndex, tags)
}

fun loadPersistentTags(shipId: String, groupIndex: Int, loadoutIndex: Int) : List<String>{
    return Settings.tagStorage[loadoutIndex].modesByShip[shipId]?.get(groupIndex) ?: emptyList()
}

fun loadTagsFromShip(ship: ShipAPI, groupIndex: Int) : List<String>{
    return (ship.customData[Values.CUSTOM_SHIP_DATA_TAG_KEY] as? InShipTagStorage)?.tagsByIndex?.get(groupIndex) ?: emptyList()
}