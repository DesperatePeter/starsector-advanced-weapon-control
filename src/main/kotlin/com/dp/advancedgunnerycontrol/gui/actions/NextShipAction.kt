package com.dp.advancedgunnerycontrol.gui.actions

import com.dp.advancedgunnerycontrol.gui.GUIAttributes
import com.fs.starfarer.api.Global
import org.lwjgl.input.Keyboard

class NextShipAction(attributes: GUIAttributes) : GUIAction(attributes) {
    override fun execute() {
        val shipList = Global.getSector().playerFleet.membersWithFightersCopy.filterNot { m -> m.isFighterWing }
        val index = shipList.indexOf(attributes.ship)
        if (isWholeFleetKeyHeld()) {
            attributes.ship = if (index == 0) {
                shipList.last()
            } else {
                shipList[index - 1]
            }
        } else {
            attributes.ship = if (index >= shipList.size - 1) {
                shipList.first()
            } else {
                shipList[index + 1]
            }
        }

    }

    override fun getTooltip(): String {
        return "Select the next ship in your fleet. Hold $wholeFleetKey to select previous ship instead"
    }

    override fun getName(): String = if(isWholeFleetKeyHeld()) "Previous Ship" else "Next Ship"

    override fun getShortcut(): Int = Keyboard.KEY_TAB
}