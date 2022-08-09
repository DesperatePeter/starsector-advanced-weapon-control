package com.dp.advancedgunnerycontrol.gui.actions

import com.dp.advancedgunnerycontrol.gui.GUIAttributes
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.utils.ShipModeStorage
import org.lwjgl.input.Keyboard

class ResetAction(attributes: GUIAttributes) : GUIAction(attributes) {
    override fun execute() {
        affectedLoadouts().forEach { index ->
            affectedShips().forEach { ship ->
                ShipModeStorage[index].modesByShip[ship.id]?.clear()
                Settings.tagStorage[index].modesByShip[ship.id]?.clear()
            }
        }
    }

    override fun getTooltip(): String {
        return "Resets all fire modes, fire modes and suffixes.\n$modifiersBoilerplateText "
    }

    override fun getName(): String = "Reset"

    override fun getShortcut(): Int = Keyboard.KEY_DELETE
}