package com.dp.advancedgunnerycontrol.utils

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.dp.advancedgunnerycontrol.typesandvalues.assignShipMode
import com.dp.advancedgunnerycontrol.typesandvalues.createTags
import com.dp.advancedgunnerycontrol.typesandvalues.loadShipModes
import com.dp.advancedgunnerycontrol.weaponais.TagBasedAI
import com.dp.advancedgunnerycontrol.weaponais.times_
import com.dp.advancedgunnerycontrol.weaponais.vectorFromAngleDeg
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs

fun applyTagsToWeaponGroup(ship: ShipAPI, groupIndex: Int, tags: List<String>) : Boolean {
    val weaponGroup = ship.weaponGroupsCopy[groupIndex]
    val plugins = weaponGroup.aiPlugins
    if(tags.isEmpty()){
        for (i in 0 until plugins.size) {
            if (plugins[i] is TagBasedAI){
                plugins[i] = (plugins[i] as? TagBasedAI)?.baseAI ?: plugins[i]
            }
        }
    }
    for (i in 0 until plugins.size) {
        if (plugins[i] !is TagBasedAI) {
            plugins[i] = TagBasedAI(plugins[i])
        }
        (plugins[i] as? TagBasedAI)?.tags = createTags(tags, plugins[i].weapon).toMutableList()
    }
    return plugins.all { (it as? TagBasedAI)?.tags?.all { t -> t.isValid() } ?: true }
}

fun reloadAllShips(storageIndex: Int) : MutableList<ShipAPI>{
    return reloadShips(storageIndex, Global.getCombatEngine()?.ships)
}

/**
 * returns all ships that did not have any tags/shipmodes
 */
fun reloadShips(storageIndex: Int, ships: List<ShipAPI?>?) : MutableList<ShipAPI>{
    val toReturn = mutableListOf<ShipAPI>()
    ships?.filter { it?.owner == 0 }?.filterNotNull().let{
        it?.forEach { ship->
            for(i in 0 until ship.weaponGroupsCopy.size){
                val tags = loadTags(ship, i, storageIndex)
                applyTagsToWeaponGroup(ship, i, tags)
                val shipModes = loadShipModes(ship, storageIndex)
                assignShipMode(shipModes, ship)
                if(tags.isEmpty() && shipModes.isEmpty()){
                    toReturn.add(ship)
                }
            }
        }
    }
    return toReturn
}

fun loadTags(ship: ShipAPI, index: Int, storageIndex: Int) : List<String>{
    if(Settings.enableCombatChangePersistance() || !doesShipHaveLocalTags(ship, storageIndex)){
        return loadPersistentTags(ship.fleetMemberId, index, storageIndex)
    }
    return loadTagsFromShip(ship, index, storageIndex)
}

/**
 * @return approximate angular distance of target from current weapon facing in rad
 * note: approximation works well for small values and is off by a factor of PI/2 for 180°
 * @param entity: Relative coordinates (velocity-compensated)
 */
fun angularDistanceFromWeapon(entity: Vector2f, weapon: WeaponAPI): Float {
    val weaponDirection = vectorFromAngleDeg(weapon.currAngle)
    val distance = entity - weapon.location
    val entityDirection = distance times_ (1f / distance.length())
    return (weaponDirection - entityDirection).length()
}
fun linearDistanceFromWeapon(entity: Vector2f, weapon: WeaponAPI): Float {
    return (weapon.location - entity).length()
}
/**
 * @param entity: In relative coordinates
 * @param collRadius: Include any tolerances in here
 * @param aimPoint: Point the weapon is aiming at, deduced from current weapon facing if not provided
 */
fun determineIfShotWillHit(entity: Vector2f, collRadius: Float, weapon: WeaponAPI, aimPoint: Vector2f? = null) : Boolean{
    val apd = aimPoint?.let { angularDistanceFromWeapon(it, weapon) } ?: 0f
    val lateralOffset = abs(angularDistanceFromWeapon(entity, weapon) - apd) * linearDistanceFromWeapon(entity, weapon)
    return lateralOffset < collRadius
}

fun saveTags(ship: ShipAPI, groupIndex: Int, loadoutIndex: Int, tags: List<String>){
    if(Settings.enableCombatChangePersistance()){
        persistTags(ship.fleetMemberId?: "", groupIndex, loadoutIndex, tags)
    }
    saveTagsInShip(ship, groupIndex, tags, loadoutIndex)
}

fun persistTags(shipId: String, groupIndex: Int, loadoutIndex: Int, tags: List<String>){
    if(!Settings.tagStorage[loadoutIndex].modesByShip.containsKey(shipId)){
        Settings.tagStorage[loadoutIndex].modesByShip[shipId] = mutableMapOf()
    }
    Settings.tagStorage[loadoutIndex].modesByShip[shipId]?.set(groupIndex, tags.toSet().toList())
}

fun saveTagsInShip(ship: ShipAPI, groupIndex: Int, tags: List<String>, storageIndex: Int){
    if(!ship.customData.containsKey(Values.CUSTOM_SHIP_DATA_WEAPONS_TAG_KEY)){
        ship.customData[Values.CUSTOM_SHIP_DATA_WEAPONS_TAG_KEY] = InShipTagStorage()
    }
    (ship.customData[Values.CUSTOM_SHIP_DATA_WEAPONS_TAG_KEY] as? InShipTagStorage)?.tagsByIndex?.get(storageIndex)?.set(groupIndex, tags.toSet().toList())
}

fun loadPersistentTags(shipId: String, groupIndex: Int, loadoutIndex: Int) : List<String>{
    return Settings.tagStorage[loadoutIndex].modesByShip[shipId]?.get(groupIndex) ?: emptyList()
}

fun loadTagsFromShip(ship: ShipAPI, groupIndex: Int, storageIndex: Int) : List<String>{
    return (ship.customData[Values.CUSTOM_SHIP_DATA_WEAPONS_TAG_KEY] as? InShipTagStorage)?.tagsByIndex?.get(storageIndex)?.get(groupIndex) ?: emptyList()
}

fun doesShipHaveLocalTags(ship: ShipAPI, storageIndex: Int) : Boolean{
    return ship.customData.containsKey(Values.CUSTOM_SHIP_DATA_WEAPONS_TAG_KEY)
            && (ship.customData[Values.CUSTOM_SHIP_DATA_WEAPONS_TAG_KEY] as? InShipTagStorage)?.tagsByIndex?.containsKey(storageIndex) ?: false
}

fun doesShipHaveLocalShipModes(ship: ShipAPI, storageIndex: Int) : Boolean{
    return ship.customData.containsKey(Values.CUSTOM_SHIP_DATA_SHIP_MODES_KEY)
            && (ship.customData[Values.CUSTOM_SHIP_DATA_SHIP_MODES_KEY] as? InShipShipModeStorage)?.modes?.containsKey(storageIndex) ?: false
}