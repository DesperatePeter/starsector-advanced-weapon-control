package com.dp.advancedgunnerycontrol.gui

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.loading.WeaponGroupSpec

fun groupAsString(group : WeaponGroupSpec, sh: FleetMemberAPI) : String {
    val strings = group.slots.mapNotNull { sh.variant.getWeaponId(it) }.map {
        Global.getSettings().getWeaponSpec(it).weaponName
    }
    val set = strings.toSet()
    val occ = mutableMapOf<String, Int>()
    strings.forEach { occ[it] = occ[it]?.plus(1) ?: 1 }
    return set.map { ("${occ[it] ?: "0"} x $it") }.toString()
}