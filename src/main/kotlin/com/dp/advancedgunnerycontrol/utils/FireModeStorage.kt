package com.dp.advancedgunnerycontrol.utils

import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.fs.starfarer.api.Global

object FireModeStorage : StorageBase<WeaponModeSelector>("$" + Values.THIS_MOD_NAME + "weaponModes"){
}