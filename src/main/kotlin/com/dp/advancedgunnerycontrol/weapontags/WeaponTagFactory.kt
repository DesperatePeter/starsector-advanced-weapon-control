package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.WeaponControlPlugin
import com.dp.advancedgunnerycontrol.weaponais.tags.WeaponAITagBase
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.WeaponAPI

var unknownTagWarnCounter = 0

fun createWeaponAiTag(name: String, weapon: WeaponAPI): WeaponAITagBase? {
    val canonicalName = canonicalizeWeaponTagName(name)
    return WeaponTagFluxFactory.create(canonicalName, weapon)
        ?: WeaponTagShieldFactory.create(canonicalName, weapon)
        ?: WeaponTagUtilityFactory.create(canonicalName, weapon)
        ?: WeaponTagPriorityFactory.create(canonicalName, weapon)
        ?: WeaponTagSyncFactory.create(canonicalName, weapon)
        ?: WeaponTagSimpleFactory.create(canonicalName, weapon)
        ?: warnUnknownTag(canonicalName, name)
}

fun createWeaponAiTags(names: List<String>, weapon: WeaponAPI): List<WeaponAITagBase> {
    return names.mapNotNull { createWeaponAiTag(it, weapon) }.filter { it.isValid() }
}

private fun warnUnknownTag(canonicalName: String, sourceName: String): WeaponAITagBase? {
    unknownTagWarnCounter++
    when {
        unknownTagWarnCounter < 10 -> Global.getLogger(WeaponControlPlugin.Companion::class.java)
            .warn("Unknown weapon tag: $canonicalName (from: $sourceName)! Will be ignored.")

        unknownTagWarnCounter == 10 -> Global.getLogger(WeaponControlPlugin.Companion::class.java).warn(
            "Unknown weapon tag: $canonicalName (from: $sourceName)! Future warnings of this type will be skipped."
        )
    }
    return null
}
