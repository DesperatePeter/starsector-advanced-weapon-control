package com.dp.advancedgunnerycontrol.utils

import com.dp.advancedgunnerycontrol.gui.groupAsString
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.*
import com.dp.advancedgunnerycontrol.weaponais.TagBasedAI
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI

enum class TagStorageModes{
    INDEX, WEAPON_COMPOSITION, WEAPON_COMPOSITION_GLOBAL
}

val tagStorageModeFromStr = mapOf(
    "Index" to TagStorageModes.INDEX,
    "WeaponComposition" to TagStorageModes.WEAPON_COMPOSITION,
    "WeaponCompositionGlobal" to TagStorageModes.WEAPON_COMPOSITION_GLOBAL
).withDefault { TagStorageModes.INDEX }

fun WeaponAPI.getAutofirePlugin() : AutofireAIPlugin?{
    return this.ship?.getWeaponGroupFor(this)?.getAutofirePlugin(this)
}
fun applyTagsToWeaponGroup(ship: ShipAPI, groupIndex: Int, tags: List<String>): Boolean {
    val weaponGroup = ship.weaponGroupsCopy?.getOrNull(groupIndex) ?: return false
    val plugins = weaponGroup.aiPlugins
    for (i in 0 until plugins.size) {
        if(Settings.weaponBlacklist.contains(weaponGroup.weaponsCopy?.getOrNull(i)?.id)) continue
        if (plugins[i] !is TagBasedAI) {
            plugins[i] = TagBasedAI(plugins[i])
        }
        (plugins[i] as? TagBasedAI)?.tags = createTags(tags, plugins[i].weapon).toMutableList()
    }
    return plugins.all { (it as? TagBasedAI)?.tags?.all { t -> t.isValid() } ?: true }
}

fun applyTagsToWeapon(weapon: WeaponAPI, tags: List<String>) {
    if(Settings.weaponBlacklist.contains(weapon.id)) return
    val weaponGroup = weapon.ship.getWeaponGroupFor(weapon)
    val plugin = weaponGroup.getAutofirePlugin(weapon)

    if (plugin !is TagBasedAI) {
        setAutofirePlugin(weapon, TagBasedAI(plugin, createTags(tags, weapon).toMutableList()))
    } else {
        val combinedTags = plugin.tags.toMutableSet()
        combinedTags.addAll(createTags(tags, weapon))
        plugin.tags = combinedTags.toMutableList()
    }

}

fun setAutofirePlugin(weapon: WeaponAPI, plugin: AutofireAIPlugin) {
    val weaponGroup = weapon.ship.getWeaponGroupFor(weapon)
    val index = weaponGroup.aiPlugins.indexOf(weaponGroup.getAutofirePlugin(weapon))
    weaponGroup.aiPlugins[index] = plugin
}

fun reloadAllShips(storageIndex: Int) {
    reloadShips(storageIndex, Global.getCombatEngine()?.ships)
}

fun reloadShips(storageIndex: Int, ships: List<ShipAPI?>?) {
    ships?.filter { it?.owner == 0 }?.filterNotNull().let { relevantShips ->
        relevantShips?.forEach { ship ->
            for (i in 0 until ship.weaponGroupsCopy.size) {
                if(Settings.autoApplySuggestedTags){
                    ship.fleetMember?.let { applySuggestedModes(it, storageIndex, false) }
                }
                val tags = loadTags(ship, i, storageIndex)
                applyTagsToWeaponGroup(ship, i, tags)
            }
            val shipModes = loadShipModes(ship, storageIndex)
            assignShipModes(shipModes, ship)
        }
    }

}

fun persistTemporaryShipData(storageIndex: Int, ships: List<ShipAPI?>?) {
    ships?.filter { it?.owner == 0 }?.filterNotNull().let {
        it?.forEach { ship ->
            for (i in 0 until ship.weaponGroupsCopy.size) {
                val tags = loadTags(ship, i, storageIndex)
                persistTags(ship.id, i, storageIndex, tags)
            }
            val modes = loadShipModes(ship, storageIndex)
            persistShipModes(ship.id, storageIndex, modes)
        }
    }
}

fun loadTags(ship: ShipAPI, index: Int, storageIndex: Int): List<String> {
    if (Settings.enableCombatChangePersistence() || !doesShipHaveLocalTags(ship, storageIndex)) {
        val shipId = generateUniversalFleetMemberId(ship)
        return loadPersistentTags(shipId, index, storageIndex)
    }
    return loadTagsFromShip(ship, index, storageIndex)
}

/**
 * loads all tag types that have been used for a given ship.
 * the returned list won't contain duplicates
 * this is used to hot-load tags when viewing a ship in GUI
 */
fun loadAllTags(ship: FleetMemberAPI, universalId: String? = null): List<String> {
    val tags = mutableSetOf<String>()
    val shipId = universalId ?: ship.id ?: ""
    for (si in 0 until Settings.maxLoadouts()) {
        for (i in 0 until ship.variant.weaponGroups.size) {
            tags.addAll(loadPersistentTags(shipId, i, si))
        }
    }
    return tags.toList()
}

fun saveTags(ship: ShipAPI, groupIndex: Int, loadoutIndex: Int, tags: List<String>) {
    if (Settings.enableCombatChangePersistence()) {
        val shipId = generateUniversalFleetMemberId(ship)
        persistTags(shipId, groupIndex, loadoutIndex, tags)
    }
    saveTagsInShip(ship, groupIndex, tags, loadoutIndex)
}

/**
 * generate unique & persistent fleetMemberId
 * return fleetMemberId-equivalent for modules of big ships
 * return fleetMemberId for regular ships
 * return empty string if something goes wrong
 */
fun generateUniversalFleetMemberId(ship: ShipAPI): String {
    if (!ship.isStationModule) return ship.fleetMemberId ?: ""
    val parentShip = ship.parentStation ?: return ""
    val parentId = parentShip.fleetMemberId ?: return ""
    val index = parentShip.childModulesCopy?.indexOf(ship) ?: -1
    if (index < 0) return ""
    return parentId + index.toString()
}


fun persistTags(shipId: String, groupIndex: Int, loadoutIndex: Int, tags: List<String>) {
    if (shipId == "") return
    when(Settings.tagStorageMode){
        TagStorageModes.INDEX -> persistTagsByIndex(shipId, groupIndex, loadoutIndex, tags)
        TagStorageModes.WEAPON_COMPOSITION -> persistTagsByWeaponComposition(shipId, groupIndex, loadoutIndex, tags)
        TagStorageModes.WEAPON_COMPOSITION_GLOBAL -> persistTagsByWeaponCompositionGlobal(shipId, groupIndex, loadoutIndex, tags)
    }
}

fun persistTagsByIndex(shipId: String, groupIndex: Int, loadoutIndex: Int, tags: List<String>){
    if (!Settings.tagStorage[loadoutIndex].modesByShip.containsKey(shipId)) {
        Settings.tagStorage[loadoutIndex].modesByShip[shipId] = mutableMapOf()
    }
    Settings.tagStorage[loadoutIndex].modesByShip[shipId]?.set(groupIndex, tags.toSet().toList())
}

fun persistTagsByWeaponComposition(shipId: String, groupIndex: Int, loadoutIndex: Int, tags: List<String>, shipKey: String = shipId){
    val key = getWeaponCompositionString(shipId, groupIndex)
    if(!Settings.tagStorageByWeaponComposition[loadoutIndex].modesByShip.containsKey(shipKey)){
        Settings.tagStorageByWeaponComposition[loadoutIndex].modesByShip[shipKey] = mutableMapOf()
    }
    Settings.tagStorageByWeaponComposition[loadoutIndex].modesByShip[shipKey]?.set(key, tags.toSet().toList())
}

fun persistTagsByWeaponCompositionGlobal(shipId: String, groupIndex: Int, loadoutIndex: Int, tags: List<String>){
    persistTagsByWeaponComposition(shipId, groupIndex, loadoutIndex, tags, "Global")
}

fun getFleetMemberByUniversalShipId(universalShipId: String): FleetMemberAPI?{
    // why do modules exist? -_-
    return Global.getSector().playerFleet.membersWithFightersCopy?.firstOrNull { it.id == universalShipId } ?:
        run { // if the ship doesn't exist, we assume it's a module and try to find its parent ship
            // modules are only supported in combat, so we assume that we are in combat
            val parentId = universalShipId.substring(0, universalShipId.length - 1) // let's assume ships with more than 10 modules don't exist
            val moduleIndex = universalShipId.last().toString().toIntOrNull() ?: return null
            val parentShip = Global.getCombatEngine()?.ships?.firstOrNull { it.fleetMemberId == parentId }
            parentShip?.childModulesCopy?.getOrNull(moduleIndex)?.fleetMember
        }
}

fun getWeaponCompositionString(shipId: String, groupIndex: Int): String{
    val member = getFleetMemberByUniversalShipId(shipId) ?: return ""
    return member.variant?.weaponGroups?.getOrNull(groupIndex)?.let { group ->
         groupAsString(group, member, false)
    } ?: ""
}

fun saveTagsInShip(ship: ShipAPI, groupIndex: Int, tags: List<String>, storageIndex: Int) {
    if (!ship.customData.containsKey(Values.CUSTOM_SHIP_DATA_WEAPONS_TAG_KEY)) {
        ship.setCustomData(Values.CUSTOM_SHIP_DATA_WEAPONS_TAG_KEY, InShipTagStorage())
    }
    (ship.customData[Values.CUSTOM_SHIP_DATA_WEAPONS_TAG_KEY] as? InShipTagStorage)?.tagsByIndex?.get(storageIndex)
        ?.set(groupIndex, tags.toSet().toList())
}

fun loadPersistentTags(shipId: String, groupIndex: Int, loadoutIndex: Int): List<String> {
    if(shipId == "") return emptyList()
    return when(Settings.tagStorageMode){
        TagStorageModes.INDEX -> loadPersistentTagsByIndex(shipId, groupIndex, loadoutIndex)
        TagStorageModes.WEAPON_COMPOSITION -> loadPersistentTagsByWeaponComposition(shipId, groupIndex, loadoutIndex)
        TagStorageModes.WEAPON_COMPOSITION_GLOBAL -> loadPersistentTagsByWeaponCompositionGlobal(shipId, groupIndex, loadoutIndex)
    }
}

fun loadPersistentTagsByIndex(shipId: String, groupIndex: Int, loadoutIndex: Int): List<String> {
    return Settings.tagStorage[loadoutIndex].modesByShip[shipId]?.get(groupIndex) ?: emptyList()
}

fun loadPersistentTagsByWeaponComposition(shipId: String, groupIndex: Int, loadoutIndex: Int, shipKey: String = shipId): List<String>{
    val key = getWeaponCompositionString(shipId, groupIndex)
    return Settings.tagStorageByWeaponComposition[loadoutIndex].modesByShip[shipKey]?.get(key) ?: emptyList()
}

fun loadPersistentTagsByWeaponCompositionGlobal(shipId: String, groupIndex: Int, loadoutIndex: Int): List<String>{
    return loadPersistentTagsByWeaponComposition(shipId, groupIndex, loadoutIndex, "Global")
}

fun getWeaponGroupIndex(weapon: WeaponAPI): Int {
    return weapon.ship.weaponGroupsCopy.indexOf(weapon.ship.getWeaponGroupFor(weapon))
}

fun loadTagsFromShip(ship: ShipAPI, groupIndex: Int, storageIndex: Int): List<String> {
    return (ship.customData[Values.CUSTOM_SHIP_DATA_WEAPONS_TAG_KEY] as? InShipTagStorage)?.tagsByIndex?.get(
        storageIndex
    )?.get(groupIndex) ?: emptyList()
}

fun doesShipHaveLocalTags(ship: ShipAPI, storageIndex: Int): Boolean {
    return ship.customData.containsKey(Values.CUSTOM_SHIP_DATA_WEAPONS_TAG_KEY)
            && (ship.customData[Values.CUSTOM_SHIP_DATA_WEAPONS_TAG_KEY] as? InShipTagStorage)?.tagsByIndex?.containsKey(
        storageIndex
    ) ?: false
}

fun doesShipHaveLocalShipModes(ship: ShipAPI, storageIndex: Int): Boolean {
    return ship.customData.containsKey(Values.CUSTOM_SHIP_DATA_SHIP_MODES_KEY)
            && (ship.customData[Values.CUSTOM_SHIP_DATA_SHIP_MODES_KEY] as? InShipShipModeStorage)?.modes?.containsKey(
        storageIndex
    ) ?: false
}