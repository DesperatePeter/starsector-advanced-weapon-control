package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.weaponais.tags.AvoidDebrisTag
import com.dp.advancedgunnerycontrol.weaponais.tags.AvoidPhaseTag
import com.dp.advancedgunnerycontrol.weaponais.tags.AvoidShieldAtTotalFluxTag
import com.dp.advancedgunnerycontrol.weaponais.tags.AvoidShieldTag
import com.dp.advancedgunnerycontrol.weaponais.tags.BigShipTag
import com.dp.advancedgunnerycontrol.weaponais.tags.ConserveAmmoTag
import com.dp.advancedgunnerycontrol.weaponais.tags.ConservePDAmmoTag
import com.dp.advancedgunnerycontrol.weaponais.tags.CustomAITag
import com.dp.advancedgunnerycontrol.weaponais.tags.DisableTagsTag
import com.dp.advancedgunnerycontrol.weaponais.tags.DoNotShootTag
import com.dp.advancedgunnerycontrol.weaponais.tags.FighterTag
import com.dp.advancedgunnerycontrol.weaponais.tags.ForceAutofireTag
import com.dp.advancedgunnerycontrol.weaponais.tags.InterdictBeamsTag
import com.dp.advancedgunnerycontrol.weaponais.tags.MergeTag
import com.dp.advancedgunnerycontrol.weaponais.tags.NoFighterTag
import com.dp.advancedgunnerycontrol.weaponais.tags.NoMissileTag
import com.dp.advancedgunnerycontrol.weaponais.tags.NoPDTag
import com.dp.advancedgunnerycontrol.weaponais.tags.NoShieldTag
import com.dp.advancedgunnerycontrol.weaponais.tags.OpportunistTag
import com.dp.advancedgunnerycontrol.weaponais.tags.OverloadTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PDTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PrioritizeBigTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PrioritizeCloseTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PrioritizeDense
import com.dp.advancedgunnerycontrol.weaponais.tags.PrioritizeFarTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PrioritizeFightersTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PrioritizeFocusedTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PrioritizeHealthyTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PrioritizeHullTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PrioritizeMissilesTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PrioritizePDTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PrioritizeShipTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PrioritizeShieldsTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PrioritizeWoundedPDTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PrioritizeWoundedTag
import com.dp.advancedgunnerycontrol.weaponais.tags.ShipTargetTag
import com.dp.advancedgunnerycontrol.weaponais.tags.SmallShipTag
import com.dp.advancedgunnerycontrol.weaponais.tags.SyncFireMode
import com.dp.advancedgunnerycontrol.weaponais.tags.SynchronizedFireTag
import com.dp.advancedgunnerycontrol.weaponais.tags.TargetPhaseTag
import com.dp.advancedgunnerycontrol.weaponais.tags.TargetShieldAtTotalFluxTag
import com.dp.advancedgunnerycontrol.weaponais.tags.TargetShieldTag
import com.dp.advancedgunnerycontrol.weaponais.tags.WeaponAITagBase
import com.fs.starfarer.api.combat.WeaponAPI

internal object WeaponTagSimpleFactory {
    fun create(canonicalName: String, weapon: WeaponAPI): WeaponAITagBase? {
        return when (canonicalName) {
            "PD" -> PDTag(weapon)
            "PrioSmall", "PrioPD", "PrioritizePD", "PrioritisePD" -> PrioritizePDTag(weapon)
            "PrioBig" -> PrioritizeBigTag(weapon)
            "NoPD" -> NoPDTag(weapon)
            "TargetFighter" -> FighterTag(weapon)
            "AvoidShield", "AvoidShields" -> AvoidShieldTag(weapon)
            "TargetShield", "TargetShields" -> TargetShieldTag(weapon)
            "AvoidShield+", "AvdShields+" -> AvoidShieldTag(weapon, 0.02f)
            "TargetShield+", "TgtShields+" -> TargetShieldTag(weapon, 0.01f)
            "NoFighter" -> NoFighterTag(weapon)
            "ConserveAmmo" -> ConserveAmmoTag(weapon)
            "ConservePDAmmo", "CnsrvPDAmmo" -> ConservePDAmmoTag(weapon)
            "Opportunist" -> OpportunistTag(weapon)
            "AvoidDebris" -> AvoidDebrisTag(weapon)
            "TargetBig", "BigShip", "BigShips" -> BigShipTag(weapon)
            "TargetSmall", "SmallShip", "SmallShips" -> SmallShipTag(weapon)
            "ForceAutoFire", "ForceAF" -> ForceAutofireTag(weapon)
            "DoNotShoot" -> DoNotShootTag(weapon)
            "AvoidPhased" -> AvoidPhaseTag(weapon)
            "TargetPhase" -> TargetPhaseTag(weapon)
            "ShipTarget" -> ShipTargetTag(weapon)
            "TgtShieldsFT" -> TargetShieldAtTotalFluxTag(weapon)
            "AvdShieldsFT" -> AvoidShieldAtTotalFluxTag(weapon)
            "NoMissile" -> NoMissileTag(weapon)
            "TargetOverloaded" -> OverloadTag(weapon)
            "NoShield", "ShieldOff", "ShieldsOff" -> NoShieldTag(weapon)
            "Merge" -> MergeTag(weapon)
            "DisableTags" -> DisableTagsTag(weapon)
            "SyncWindow" -> SynchronizedFireTag(weapon, SyncFireMode.WINDOW)
            "SyncVolley" -> SynchronizedFireTag(weapon, SyncFireMode.VOLLEY)
            "Ambush" -> SynchronizedFireTag(weapon, SyncFireMode.AMBUSH)
            "PrioFighter" -> PrioritizeFightersTag(weapon)
            "PrioMissile" -> PrioritizeMissilesTag(weapon)
            "PrioShip", "PrioShips" -> PrioritizeShipTag(weapon)
            "PrioWounded" -> PrioritizeWoundedTag(weapon)
            "PrioWoundedPD" -> PrioritizeWoundedPDTag(weapon)
            "PrioHealthy" -> PrioritizeHealthyTag(weapon)
            "PrioFocused" -> PrioritizeFocusedTag(weapon)
            "PrioShields" -> PrioritizeShieldsTag(weapon)
            "PrioHull" -> PrioritizeHullTag(weapon)
            "PrioClose" -> PrioritizeCloseTag(weapon)
            "PrioFar" -> PrioritizeFarTag(weapon)
            "BlockBeams" -> InterdictBeamsTag(weapon)
            "CustomAI" -> CustomAITag(weapon)
            "PrioDense" -> PrioritizeDense(weapon)
            else -> null
        }
    }
}
