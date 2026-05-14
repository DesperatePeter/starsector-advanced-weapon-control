package com.dp.advancedgunnerycontrol.gui.actions


import com.dp.advancedgunnerycontrol.gui.session.GUIAttributes
import com.fs.starfarer.api.Global
import org.lwjgl.input.Keyboard

class NextShipAction(attributes: GUIAttributes) : GUIAction(attributes) {
    override fun execute() {
        selectShip(previous = isWholeFleetKeyHeld())
    }

    override fun supportsRightClick(): Boolean = true

    override fun executeRightClick(): Boolean {
        selectShip(previous = !isWholeFleetKeyHeld())
        return true
    }

    private fun selectShip(previous: Boolean) {
        val shipList = editableShips()
        if (shipList.isEmpty()) return

        val index = shipList.indexOf(attributes.ship)
        val nextIndex = if (index < 0) {
            if (previous) shipList.lastIndex else 0
        } else if (previous) {
            if (index == 0) shipList.lastIndex else index - 1
        } else {
            if (index >= shipList.lastIndex) 0 else index + 1
        }
        attributes.ship = shipList[nextIndex]
    }

    private fun editableShips() = Global.getSector()?.playerFleet?.membersWithFightersCopy
        ?.filterNot { m -> m.isFighterWing }
        .orEmpty()

    private fun positionText(): String {
        val shipList = editableShips()
        if (shipList.isEmpty()) return "0/0"
        val index = shipList.indexOf(attributes.ship)
        val current = if (index >= 0) index + 1 else 0
        return "$current/${shipList.size}"
    }

    override fun getTooltip(): String {
        return "Select the next ship in your fleet. Right-click to select the previous ship. Hold $wholeFleetKey to show and select the previous ship instead; right-click then selects the next ship."
    }

    override fun getName(): String {
        val direction = if (isWholeFleetKeyHeld()) "Previous Ship" else "Next Ship"
        return "$direction [${positionText()}]"
    }

    override fun getStableLayoutName(): String = STABLE_LAYOUT_NAME

    override fun getShortcut(): Int = Keyboard.KEY_TAB

    companion object {
        const val STABLE_LAYOUT_NAME = "Previous Ship [999/999]"
    }
}
