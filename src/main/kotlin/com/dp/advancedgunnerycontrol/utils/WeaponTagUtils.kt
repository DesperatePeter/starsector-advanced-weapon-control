package com.dp.advancedgunnerycontrol.utils

import com.dp.advancedgunnerycontrol.gui.groupAsString
import com.dp.advancedgunnerycontrol.gui.refitscreen.ModuleIdManager
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.*
import com.dp.advancedgunnerycontrol.weaponais.TagBasedAI
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.campaign.fleet.FleetMember

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
                persistTags(ship.id, ship.fleetMember, i, storageIndex, tags)
            }
            val modes = loadShipModes(ship, storageIndex)
            persistShipModes(ship.id, storageIndex, modes)
        }
    }
}

fun loadTags(ship: ShipAPI, index: Int, storageIndex: Int): List<String> {
    if (Settings.enableCombatChangePersistence() || !doesShipHaveLocalTags(ship, storageIndex)) {
        if(ship.fleetMember == null) return emptyList()
        val shipId = generateUniversalFleetMemberId(ship)
        return loadPersistentTags(shipId, ship.fleetMember, index, storageIndex)
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
            tags.addAll(loadPersistentTags(shipId, ship, i, si))
        }
    }
    return tags.toList()
}

fun saveTags(ship: ShipAPI, groupIndex: Int, loadoutIndex: Int, tags: List<String>) {
    if (Settings.enableCombatChangePersistence()) {
        val shipId = generateUniversalFleetMemberId(ship)
        if(ship.fleetMember == null) return
        persistTags(shipId, ship.fleetMember, groupIndex, loadoutIndex, tags)
    }
    saveTagsInShip(ship, groupIndex, tags, loadoutIndex)
}

fun generateUniversalFleetMemberId(parentId: String, moduleIndex: Int): String{
    if (moduleIndex < 0) return ""
    return parentId + moduleIndex.toString()
}

/**
 * generate unique & persistent fleetMemberId
 * return fleetMemberId-equivalent for modules of big ships
 * return fleetMemberId for regular ships
 * return empty string if something goes wrong
 */
fun generateUniversalFleetMemberId(ship: ShipAPI): String {
    (ModuleIdManager.getUniversalIdIfApplicable(ship))?.let {
        return it
    }
    if (!ship.isStationModule) return ship.fleetMemberId ?: ""
    val parentShip = ship.parentStation ?: return ""
    val parentId = parentShip.fleetMemberId ?: return ""
    val index = parentShip.childModulesCopy?.indexOf(ship) ?: -1
    return generateUniversalFleetMemberId(parentId, index)
}

fun generateUniversalFleetMemberId(ship: FleetMemberAPI): String{
    return ((ship as? FleetMember)?.instantiateForCombat(null, 0, null) as? ShipAPI)?.let {
        generateUniversalFleetMemberId(it)
    } ?: ship.id
}

fun persistTags(shipId: String, member: FleetMemberAPI, groupIndex: Int, loadoutIndex: Int, tags: List<String>) {
    if (shipId == "") return
    when(Settings.tagStorageMode){
        TagStorageModes.INDEX -> persistTagsByIndex(shipId, groupIndex, loadoutIndex, tags)
        TagStorageModes.WEAPON_COMPOSITION -> persistTagsByWeaponComposition(shipId, member, groupIndex, loadoutIndex, tags)
        TagStorageModes.WEAPON_COMPOSITION_GLOBAL -> persistTagsByWeaponCompositionGlobal(shipId, member, groupIndex, loadoutIndex, tags)
    }
}

fun WeaponAPI.hasAgcTag(tag: String): Boolean{
    val dummyTag = createTag(tag, this) ?: return false
    return (getAutofirePlugin() as? TagBasedAI)?.tags?.any { it::class == dummyTag::class } == true
}

fun WeaponAPI.hasAnyAgcTag(vararg tags: String): Boolean{
    return tags.any { hasAgcTag(it) }
}

fun persistTagsByIndex(shipId: String, groupIndex: Int, loadoutIndex: Int, tags: List<String>){
    if (!Settings.tagStorage[loadoutIndex].modesByShip.containsKey(shipId)) {
        Settings.tagStorage[loadoutIndex].modesByShip[shipId] = mutableMapOf()
    }
    Settings.tagStorage[loadoutIndex].modesByShip[shipId]?.set(groupIndex, tags.toSet().toList())
}

fun persistTagsByWeaponComposition(universalShipId: String, member: FleetMemberAPI, groupIndex: Int, loadoutIndex: Int, tags: List<String>, shipKey: String = universalShipId){
    val key = getWeaponCompositionString(member, groupIndex)
    if(key == "") return
    if(!Settings.tagStorageByWeaponComposition[loadoutIndex].modesByShip.containsKey(shipKey)){
        Settings.tagStorageByWeaponComposition[loadoutIndex].modesByShip[shipKey] = mutableMapOf()
    }
    Settings.tagStorageByWeaponComposition[loadoutIndex].modesByShip[shipKey]?.set(key, tags.toSet().toList())
}

fun persistTagsByWeaponCompositionGlobal(universalShipId: String, member: FleetMemberAPI, groupIndex: Int, loadoutIndex: Int, tags: List<String>){
    persistTagsByWeaponComposition(universalShipId, member, groupIndex, loadoutIndex, tags, "Global")
}

fun getWeaponCompositionString(member: FleetMemberAPI, groupIndex: Int): String{
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


fun loadPersistentTags(universalShipId: String, ship: FleetMemberAPI, groupIndex: Int, loadoutIndex: Int): List<String> {
    if(universalShipId == "") return emptyList()
    return when(Settings.tagStorageMode){
        TagStorageModes.INDEX -> loadPersistentTagsByIndex(universalShipId, groupIndex, loadoutIndex)
        TagStorageModes.WEAPON_COMPOSITION -> loadPersistentTagsByWeaponComposition(ship, universalShipId, groupIndex, loadoutIndex)
        TagStorageModes.WEAPON_COMPOSITION_GLOBAL -> loadPersistentTagsByWeaponCompositionGlobal(ship, universalShipId, groupIndex, loadoutIndex)
    }
}

fun loadPersistentTagsByIndex(universalShipId: String, groupIndex: Int, loadoutIndex: Int): List<String> {
    return Settings.tagStorage[loadoutIndex].modesByShip[universalShipId]?.get(groupIndex) ?: emptyList()
}

fun loadPersistentTagsByWeaponComposition(member: FleetMemberAPI, universalShipId: String, groupIndex: Int, loadoutIndex: Int, shipKey: String = universalShipId): List<String>{
    val key = getWeaponCompositionString(member, groupIndex)
    if(key == "") return emptyList()
    return Settings.tagStorageByWeaponComposition[loadoutIndex].modesByShip[shipKey]?.get(key) ?: emptyList()
}

fun loadPersistentTagsByWeaponCompositionGlobal(member: FleetMemberAPI, universalShipId: String, groupIndex: Int, loadoutIndex: Int): List<String>{
    return loadPersistentTagsByWeaponComposition(member, universalShipId, groupIndex, loadoutIndex, "Global")
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