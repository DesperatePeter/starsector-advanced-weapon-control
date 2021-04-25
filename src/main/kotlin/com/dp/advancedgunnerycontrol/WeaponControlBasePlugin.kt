package com.dp.advancedgunnerycontrol

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global

public class WeaponControlBasePlugin : BaseModPlugin(){

    override fun onApplicationLoad() {
        super.onApplicationLoad()
        // Note: In the future, reading a setting file might go here
        Global.getLogger(this.javaClass).info("Loaded Advanced Weapon Control Mod")
    }
}