package com.dp.advancedgunnerycontrol.settings

import com.fs.starfarer.api.Global

class LunaSettingHandler<T>(private val key: String, private val defaultValue: T) {
    companion object{
        private const val LUNALIB_MOD_ID = "lunalib"
        const val LUNALIB_AGC_KEY_PREFIX = "agc_"
        val isLunaLibPresent: Boolean
            get() = Global.getSettings().modManager.isModEnabled(LUNALIB_MOD_ID)
    }
    operator fun invoke(): T?{
        if(!isLunaLibPresent) return null
        return loadLunaSetting(LUNALIB_AGC_KEY_PREFIX + key, defaultValue)
    }
}