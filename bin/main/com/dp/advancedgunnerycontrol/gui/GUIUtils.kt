package com.dp.advancedgunnerycontrol.gui

import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponGroupAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.loading.WeaponGroupSpec
import kotlin.math.roundToInt

fun groupAsString(group: WeaponGroupSpec, sh: FleetMemberAPI, includeCount: Boolean = true): String {
    val strings = group.slots.mapNotNull { sh.variant.getWeaponId(it) }.map {
        Global.getSettings().getWeaponSpec(it).weaponName
    }
    val set = strings.toSet().sorted()
    val occ = mutableMapOf<String, Int>()
    strings.forEach { occ[it] = occ[it]?.plus(1) ?: 1 }
    return if(includeCount){
        set.map { ("${occ[it] ?: "0"} x $it") }.toString()
    }else{
        set.toString()
    }
}

fun groupAsString(group: WeaponGroupAPI, sh: FleetMemberAPI, includeCount: Boolean = true): String {
    val weaponStrings = group.weaponsCopy.map {
        it.displayName
    }
    val occ = mutableMapOf<String, Int>()
    weaponStrings.forEach {
        occ[it] = occ[it]?.plus(1) ?: 1
    }
    return weaponStrings.toSet().sorted().map {
        if(includeCount) {("${occ[it] ?: "0"} x $it")} else {it}
    }.toString()
}

fun groupFluxCost(group: WeaponGroupSpec, sh: FleetMemberAPI): Int {
    return try {
        group.slots.mapNotNull { sh.variant.getWeaponId(it) }.map {
            Global.getSettings().getWeaponSpec(it).derivedStats.fluxPerSecond
        }.sum().roundToInt()
    }catch (e: Exception){
        0
    }

}

fun groupWeaponSpriteNames(group: WeaponGroupSpec, sh: FleetMemberAPI): List<String> {
    return group.slots.mapNotNull { sh.variant.getWeaponId(it) }.map {
        Global.getSettings().getWeaponSpec(it).turretSpriteName
    }.toSet().toList()
}

fun isElligibleForPD(groupIndex: Int, sh: FleetMemberAPI): Boolean {
    val group = sh.variant.weaponGroups[groupIndex]
    val hasIPDA = sh.variant.hasHullMod("pointdefenseai")
    return group.slots.mapNotNull { sh.variant.getWeaponId(it) }.map {
        val weapon = Global.getSettings().getWeaponSpec(it)
        val aiHints = weapon.aiHints
        (hasIPDA && weapon.size == WeaponAPI.WeaponSize.SMALL && weapon.type != WeaponAPI.WeaponType.MISSILE) ||
                aiHints.contains(WeaponAPI.AIHints.PD_ALSO) ||
                aiHints.contains(WeaponAPI.AIHints.PD) ||
                aiHints.contains(WeaponAPI.AIHints.PD_ONLY)
    }.contains(true)
}

fun isEverythingBlacklisted(groupIndex: Int, sh: FleetMemberAPI): Boolean {
    val group = sh.variant.weaponGroups[groupIndex]
    return (group.slots.mapNotNull { sh.variant.getWeaponId(it) }.all { Settings.weaponBlacklist.contains(it) })
}

fun usesAmmo(groupIndex: Int, sh: FleetMemberAPI): Boolean {
    val group = sh.variant.weaponGroups[groupIndex]
    return group.slots.mapNotNull { Global.getSettings().getWeaponSpec(sh.variant.getWeaponId(it)) }.any {
        it.usesAmmo()
    }
}
