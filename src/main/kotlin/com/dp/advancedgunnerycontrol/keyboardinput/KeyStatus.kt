package com.dp.advancedgunnerycontrol.keyboardinput

enum class ControlEventType {
    CYCLE, COMBINE, INFO, RESET, LOAD, SUFFIX, CYCLE_LOADOUT, HELP, NONE
}

class KeyStatus {
    var mcontrolEvent: ControlEventType = ControlEventType.NONE
    var mpressedWeaponGroup: Int = 0 // 0 for inactive/unknown
    var lastPressedWeaponGroup = 0
    fun reset() {
        mcontrolEvent = ControlEventType.NONE
        mpressedWeaponGroup = 0
    }
}
