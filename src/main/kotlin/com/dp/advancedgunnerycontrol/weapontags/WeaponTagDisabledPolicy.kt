package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.shipdata.isEligibleForPdTag
import com.dp.advancedgunnerycontrol.shipdata.isEverythingBlacklisted
import com.dp.advancedgunnerycontrol.shipdata.usesAmmo
import com.dp.advancedgunnerycontrol.shipdata.usesAmmoNonMissile
import com.fs.starfarer.api.fleet.FleetMemberAPI

val tagsRequiringPointDefenseWeapon = listOf("PD", "NoPD", "PD(TF>N%)", "PD(SF>N%)", "PD(HF>N%)", "NoMissile")
val tagsRequiringAmmoWeapon = listOf("ConserveAmmo", "Opportunist(A<N%)")
val tagsRequiringNonMissileAmmoWeapon = listOf("ConservePDAmmo", "PD(A<N%)")

fun shouldTagBeDisabled(groupIndex: Int, sh: FleetMemberAPI, tag: String): Boolean {
    return tagDisabledReasonForGroup(groupIndex, sh, tag) != null
}

fun tagDisabledReasonForGroup(groupIndex: Int, sh: FleetMemberAPI, tag: String): String? {
    val modTag = tagNameToRegexName(tag)
    if (isEverythingBlacklisted(groupIndex, sh)) {
        return "this weapon group has no AGC-supported weapons."
    }
    if (tagsRequiringPointDefenseWeapon.contains(modTag) && !isEligibleForPdTag(groupIndex, sh)) {
        return "PD tags require a point defence weapon in this group."
    }
    if (tagsRequiringAmmoWeapon.contains(modTag) && !usesAmmo(groupIndex, sh)) {
        return "ammo tags require a weapon that uses ammo."
    }
    if (tagsRequiringNonMissileAmmoWeapon.contains(modTag) && !usesAmmoNonMissile(groupIndex, sh)) {
        return "PD ammo tags require a non-missile weapon that uses ammo."
    }
    return null
}

fun disabledTagsForGroup(groupIndex: Int, sh: FleetMemberAPI, tags: List<String>): Set<String> {
    if (tags.isEmpty()) return emptySet()
    if (isEverythingBlacklisted(groupIndex, sh)) return tags.toSet()
    val pointDefenseEligible = isEligibleForPdTag(groupIndex, sh)
    val ammoWeaponEligible = usesAmmo(groupIndex, sh)
    val nonMissileAmmoWeaponEligible = usesAmmoNonMissile(groupIndex, sh)
    return tags
        .filter { tag ->
            val modTag = tagNameToRegexName(tag)
            (modTag in tagsRequiringPointDefenseWeapon && !pointDefenseEligible) ||
                (modTag in tagsRequiringAmmoWeapon && !ammoWeaponEligible) ||
                (modTag in tagsRequiringNonMissileAmmoWeapon && !nonMissileAmmoWeaponEligible)
        }
        .toSet()
}
