package com.dp.advancedgunnerycontrol.weaponais

import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import java.util.Locale
import kotlin.math.min

fun ShipAPI.hasPhaseCloak(): Boolean{
    return hullSpec.isPhase && phaseCloak != null
}

fun WeaponAPI.getMaxSpreadForNextBurst(): Float{
    spec ?: return currSpread
    if(spec.burstSize <= 1) return currSpread
    val mrm = ship.mutableStats?.maxRecoilMult?.mult ?: 1f
    val rbm = ship.mutableStats?.recoilPerShotMult?.mult ?: 1f
    return min(spec.maxSpread * mrm, currSpread + (spec.burstSize - 1).toFloat() * spec.spreadBuildup * rbm)
}

fun isPD(weapon: WeaponAPI): Boolean {
    if (weapon.hasAIHint(WeaponAPI.AIHints.PD) || weapon.hasAIHint(WeaponAPI.AIHints.PD_ONLY)
    ) return true
    return ((weapon.ship?.variant?.hasHullMod("pointdefenseai") == true)
            && (weapon.size == WeaponAPI.WeaponSize.SMALL) && (weapon.type != WeaponAPI.WeaponType.MISSILE))
}

fun isAimable(weapon: WeaponAPI): Boolean {
    return weapon.spec?.trackingStr?.lowercase(Locale.getDefault()) in setOf(null, "", "none") &&
            !(weapon.hasAIHint(WeaponAPI.AIHints.DO_NOT_AIM))
}

fun isInvalid(aiPlugin: AutofireAIPlugin): Boolean {
    (aiPlugin as? SpecificAIPluginBase)?.let {
        if (it.weapon.id in Settings.weaponBlacklist) return true
        return !it.isValid()
    }
    // if it's note one of my plugins it's safe to assume that it's valid (at least it's not my job)
    return false
}

fun ammoLevel(weapon: WeaponAPI): Float {
    if (!weapon.usesAmmo()) return 1.0f
    if (weapon.maxAmmo <= 0) return 1.0f
    return weapon.ammo.toFloat() / weapon.maxAmmo.toFloat()
}

fun WeaponAPI.sizeAsFloat(): Float{
    return when(size){
        WeaponAPI.WeaponSize.SMALL -> 1f
        WeaponAPI.WeaponSize.MEDIUM -> 2f
        WeaponAPI.WeaponSize.LARGE -> 4f
        null -> 0f
    }
}