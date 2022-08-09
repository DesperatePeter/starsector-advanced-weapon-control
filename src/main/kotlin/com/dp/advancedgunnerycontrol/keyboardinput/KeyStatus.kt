package com.dp.advancedgunnerycontrol.keyboardinput

enum class ControlEventType {
    INFO, NONE
}

class KeyStatus {
    var mcontrolEvent: ControlEventType = ControlEventType.NONE
    fun reset() {
        mcontrolEvent = ControlEventType.NONE
    }
}
