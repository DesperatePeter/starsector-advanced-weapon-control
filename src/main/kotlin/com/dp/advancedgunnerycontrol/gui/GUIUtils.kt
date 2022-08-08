package com.dp.advancedgunnerycontrol.gui

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.FMValues
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.loading.WeaponGroupSpec
import kotlin.math.roundToInt
import com.dp.advancedgunnerycontrol.typesandvalues.FireMode

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
        Global.getSettings().getWeaponSpec(it).turretSpriteName
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

fun isBlacklisted(groupIndex: Int, sh: FleetMemberAPI) : Boolean {
    val group = sh.variant.weaponGroups[groupIndex]
    return (group.slots.mapNotNull { sh.variant.getWeaponId(it) }.all { Settings.weaponBlacklist.contains(it) })
}

fun usesAmmo(groupIndex: Int, sh: FleetMemberAPI) : Boolean {
    val group = sh.variant.weaponGroups[groupIndex]
    return group.slots.mapNotNull { Global.getSettings().getWeaponSpec(sh.variant.getWeaponId(it)) }.any {
        it.usesAmmo()
    }
}

fun shouldModeBeDisabled(groupIndex: Int, sh: FleetMemberAPI, mode: FireMode) : Boolean {
    if(mode == FireMode.PD_AMMO && !usesAmmo(groupIndex, sh)) return true
    return (FMValues.PDModes.contains(mode)) && !isElligibleForPD(groupIndex, sh)
}
