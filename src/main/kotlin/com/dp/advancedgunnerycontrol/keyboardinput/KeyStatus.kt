package com.dp.advancedgunnerycontrol.keyboardinput

enum class ControlEventType {
    CYCLE, COMBINE, INFO, RESET, LOAD, NONE
}

class KeyStatus {
    var mcontrolEvent: ControlEventType = ControlEventType.NONE
    var mpressedWeaponGroup: Int = 0 // 0 for inactive/unknown
    fun reset() {
        mcontrolEvent = ControlEventType.NONE
        mpressedWeaponGroup = 0
    }
}
