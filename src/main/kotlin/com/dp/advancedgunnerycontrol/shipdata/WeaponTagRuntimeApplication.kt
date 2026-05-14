package com.dp.advancedgunnerycontrol.shipdata

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.shipmodes.assignShipModes
import com.dp.advancedgunnerycontrol.shipmodes.loadShipModes
import com.dp.advancedgunnerycontrol.shipmodes.persistShipModes
import com.dp.advancedgunnerycontrol.weapontags.applySuggestedWeaponTags
import com.dp.advancedgunnerycontrol.weapontags.canonicalizeWeaponTagNames
import com.dp.advancedgunnerycontrol.weapontags.createWeaponAiTag
import com.dp.advancedgunnerycontrol.weapontags.createWeaponAiTags
import com.dp.advancedgunnerycontrol.weaponais.TagBasedAI
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

fun WeaponAPI.getAutofirePlugin() : AutofireAIPlugin?{
    return this.ship?.getWeaponGroupFor(this)?.getAutofirePlugin(this)
}

fun applyTagsToWeaponGroup(ship: ShipAPI, groupIndex: Int, tags: List<String>): Boolean {
    val weaponGroup = ship.weaponGroupsCopy?.getOrNull(groupIndex) ?: return false
    val canonicalTags = canonicalizeWeaponTagNames(tags)
    val plugins = weaponGroup.aiPlugins
    for (pluginIndex in 0 until plugins.size) {
        if(Settings.weaponBlacklist.contains(weaponGroup.weaponsCopy?.getOrNull(pluginIndex)?.id)) continue
        if (plugins[pluginIndex] !is TagBasedAI) {
            plugins[pluginIndex] = TagBasedAI(plugins[pluginIndex])
        }
        (plugins[pluginIndex] as? TagBasedAI)?.tags = createWeaponAiTags(canonicalTags, plugins[pluginIndex].weapon).toMutableList()
    }
    return plugins.all { plugin -> (plugin as? TagBasedAI)?.tags?.all { tag -> tag.isValid() } ?: true }
}

fun applyTagsToWeapon(weapon: WeaponAPI, tags: List<String>) {
    if(Settings.weaponBlacklist.contains(weapon.id)) return
    val canonicalTags = canonicalizeWeaponTagNames(tags)
    val weaponGroup = weapon.ship?.getWeaponGroupFor(weapon) ?: return
    val plugin = weaponGroup.getAutofirePlugin(weapon) ?: return

    if (plugin !is TagBasedAI) {
        setAutofirePlugin(weapon, TagBasedAI(plugin, createWeaponAiTags(canonicalTags, weapon).toMutableList()))
    } else {
        val combinedTags = plugin.tags.toMutableSet()
        combinedTags.addAll(createWeaponAiTags(canonicalTags, weapon))
        plugin.tags = combinedTags.toMutableList()
    }

}

fun setAutofirePlugin(weapon: WeaponAPI, plugin: AutofireAIPlugin) {
    val weaponGroup = weapon.ship?.getWeaponGroupFor(weapon) ?: return
    val currentPlugin = weaponGroup.getAutofirePlugin(weapon) ?: return
    val index = weaponGroup.aiPlugins.indexOf(currentPlugin)
    if (index < 0) return
    weaponGroup.aiPlugins[index] = plugin
}

fun reloadAllShips(storageIndex: Int) {
    reloadShips(storageIndex, Global.getCombatEngine()?.ships)
}

fun reloadShips(storageIndex: Int, ships: List<ShipAPI?>?) {
    ships?.filter { it?.owner == 0 }?.filterNotNull().let { relevantShips ->
        relevantShips?.forEach { ship ->
            if(Settings.autoApplySuggestedTags){
                ship.fleetMember?.let { applySuggestedWeaponTags(it, storageIndex, false, agcStableShipId(ship)) }
            }
            for (groupIndex in 0 until ship.weaponGroupsCopy.size) {
                val tags = loadTags(ship, groupIndex, storageIndex)
                applyTagsToWeaponGroup(ship, groupIndex, tags)
            }
            val shipModes = loadShipModes(ship, storageIndex)
            assignShipModes(shipModes, ship)
        }
    }

}

fun persistTemporaryShipData(storageIndex: Int, ships: List<ShipAPI?>?) {
    ships?.filter { it?.owner == 0 }?.filterNotNull().let {
        it?.forEach { ship ->
            // Runtime ships, especially modules, must round-trip through the same universal id used by loadTags().
            val shipId = agcStableShipId(ship)
            if (shipId.isBlank()) return@forEach
            for (groupIndex in 0 until ship.weaponGroupsCopy.size) {
                val tags = loadTags(ship, groupIndex, storageIndex)
                persistTags(shipId, groupIndex, storageIndex, tags)
            }
            val modes = loadShipModes(ship, storageIndex)
            persistShipModes(shipId, storageIndex, modes)
        }
    }
}

fun WeaponAPI.hasAgcTag(tag: String): Boolean{
    val dummyTag = createWeaponAiTag(tag, this) ?: return false
    return (getAutofirePlugin() as? TagBasedAI)?.tags?.any { it::class == dummyTag::class } == true
}

fun WeaponAPI.hasAnyAgcTag(vararg tags: String): Boolean{
    return tags.any { hasAgcTag(it) }
}
