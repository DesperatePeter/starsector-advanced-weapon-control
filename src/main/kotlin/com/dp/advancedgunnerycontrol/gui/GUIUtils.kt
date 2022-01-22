package com.dp.advancedgunnerycontrol.gui

import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.loading.WeaponGroupSpec
import kotlin.math.roundToInt
import com.dp.advancedgunnerycontrol.typesandvalues.FireMode
import com.dp.advancedgunnerycontrol.utils.FireModeStorage
import com.dp.advancedgunnerycontrol.utils.SuffixStorage

fun groupAsString(group : WeaponGroupSpec, sh: FleetMemberAPI) : String {
    val strings = group.slots.mapNotNull { sh.variant.getWeaponId(it) }.map {
        Global.getSettings().getWeaponSpec(it).weaponName
    }
    val set = strings.toSet()
    val occ = mutableMapOf<String, Int>()
    strings.forEach { occ[it] = occ[it]?.plus(1) ?: 1 }
    return set.map { ("${occ[it] ?: "0"} x $it") }.toString()
}

fun groupFluxCost(group: WeaponGroupSpec, sh: FleetMemberAPI) : Int {
    return group.slots.mapNotNull { sh.variant.getWeaponId(it) }.map {
        Global.getSettings().getWeaponSpec(it).derivedStats.fluxPerSecond
    }.sum().roundToInt()
}

fun groupWeaponSpriteNames(group: WeaponGroupSpec, sh: FleetMemberAPI) : List<String> {
    return group.slots.mapNotNull { sh.variant.getWeaponId(it) }.map {
        Global.getSettings().getWeaponSpec(it).hardpointSpriteName
    }.toSet().toList()
}

fun isElligibleForPD(groupIndex: Int, sh: FleetMemberAPI) : Boolean {
    val group = sh.variant.weaponGroups[groupIndex]
    val hasIPDA = sh.variant.hasHullMod("pointdefenseai")
    return group.slots.mapNotNull { sh.variant.getWeaponId(it) }.map {
        val weapon = Global.getSettings().getWeaponSpec(it)
        val aiHints = weapon.aiHints
        (aiHints.contains(WeaponAPI.AIHints.PD_ALSO) || aiHints.contains(WeaponAPI.AIHints.PD) || aiHints.contains(WeaponAPI.AIHints.PD_ONLY)) ||
                (hasIPDA && weapon.size == WeaponAPI.WeaponSize.SMALL && weapon.type != WeaponAPI.WeaponType.MISSILE)
    }.contains(true)
}

fun shouldModeBeDisabled(groupIndex: Int, sh: FleetMemberAPI, mode: FireMode) : Boolean {
    return (mode == FireMode.PD || mode == FireMode.MISSILE) && !isElligibleForPD(groupIndex, sh)
}

fun applySuggestedModes(ship: FleetMemberAPI, storageIndex: Int){
    val groups = ship.variant.weaponGroups
    val modeStore = FireModeStorage[storageIndex]
    if(modeStore.modesByShip[ship.id] == null){
        modeStore.modesByShip[ship.id] = mutableMapOf()
    }
    val suffixStore = SuffixStorage[storageIndex]
    if(suffixStore.modesByShip[ship.id] == null){
        suffixStore.modesByShip[ship.id] = mutableMapOf()
    }
    groups.forEachIndexed { index, group ->
        val weaponID = group.slots.first()?.let { ship.variant.getWeaponId(it) } ?: ""
        val modeKey : String = if(Settings.suggestedModes.containsKey(weaponID)){
            weaponID
        }else {
            Settings.suggestedModes.keys.map { Regex(it) }.find { it.matches(weaponID) }.toString()
        }
        modeStore.modesByShip[ship.id]?.let { it[index] = Settings.suggestedModes[modeKey] ?: ""}

        val suffixKey : String = if(Settings.suggestedSuffixes.containsKey(weaponID)){
            weaponID
        }else {
            Settings.suggestedSuffixes.keys.map { Regex(it) }.find { it.matches(weaponID) }.toString()
        }
        suffixStore.modesByShip[ship.id]?.let { it[index] = Settings.suggestedSuffixes[suffixKey] ?: ""}
    }
}