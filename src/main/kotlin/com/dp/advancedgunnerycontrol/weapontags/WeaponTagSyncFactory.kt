package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.weaponais.tags.SyncFireMode
import com.dp.advancedgunnerycontrol.weaponais.tags.SynchronizedFireTag
import com.dp.advancedgunnerycontrol.weaponais.tags.WeaponAITagBase
import com.fs.starfarer.api.combat.WeaponAPI

internal object WeaponTagSyncFactory {
    fun create(canonicalName: String, weapon: WeaponAPI): WeaponAITagBase? {
        return when {
            parseSyncTagOptions(canonicalName) != null -> {
                val options = parseSyncTagOptions(canonicalName) ?: return null
                SynchronizedFireTag(
                    weapon = weapon,
                    mode = syncFireModeForName(options.family),
                    requireShipTarget = options.requireShipTarget,
                    systemTriggers = options.systemTriggers,
                )
            }

            else -> null
        }
    }

    private fun syncFireModeForName(modeName: String): SyncFireMode {
        return when (modeName) {
            "SyncVolley" -> SyncFireMode.VOLLEY
            "Ambush" -> SyncFireMode.AMBUSH
            else -> SyncFireMode.WINDOW
        }
    }
}
