package com.dp.advancedgunnerycontrol.combatgui.buttongroups

/**
 * pass this to GuiLayout.addButtonGroup as refresh to enable radio-button behavior
 * @note only works if all buttons have unique non-null data.
 */
class RadioButtonRefreshAction : RefreshButtonsAction() {
    private var lastData : Any? = null
    override fun refreshButtons(group: DataButtonGroup) {
        val data = group.getActiveButtonData().firstOrNull { it != lastData } ?: return
        lastData = data
        group.buttons.filter { it.data != data }.forEach { it.isActive = false }
    }
}