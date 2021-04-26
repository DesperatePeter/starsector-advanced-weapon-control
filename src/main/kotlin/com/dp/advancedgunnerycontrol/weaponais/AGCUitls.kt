package com.dp.advancedgunnerycontrol.weaponais

import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI

fun isPD(weapon: WeaponAPI) : Boolean {
    return weapon.hasAIHint(WeaponAPI.AIHints.PD_ALSO) || weapon.hasAIHint(WeaponAPI.AIHints.PD) || weapon.hasAIHint(
        WeaponAPI.AIHints.PD_ONLY)
}

fun isAimable(weapon: WeaponAPI) : Boolean {
    return !weapon.hasAIHint(WeaponAPI.AIHints.DO_NOT_AIM)
}

fun isInvalid(aiPlugin : AutofireAIPlugin) : Boolean {
    (aiPlugin as? SpecificAIPluginBase)?.let { return !it.isValid() }
        ?: return false // if it's note one of my plugins it's safe to assume that it's valid
}

fun isHostile(entity: CombatEntityAPI) : Boolean{
    return entity.owner == 1
}