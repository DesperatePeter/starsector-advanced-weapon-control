package com.dp.advancedgunnerycontrol.keyboardinput

enum class ControlEventType {
    MERGE, INFO, NONE
}

class KeyStatus {
    var mcontrolEvent: ControlEventType = ControlEventType.NONE
    fun reset() {
        mcontrolEvent = ControlEventType.NONE
    }
}
