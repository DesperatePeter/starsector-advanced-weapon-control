package com.dp.advancedgunnerycontrol

class KeyStatus() {
    var mcontrolEvent: ControlEventType = ControlEventType.NONE
    var mpressedWeaponGroup: Int = 0 // 0 for inactive/unknown
    fun reset() {
        mcontrolEvent = ControlEventType.NONE
        mpressedWeaponGroup = 0
    }
}
