package com.dp.advancedgunnerycontrol.combatgui.buttongroups

/**
 * pass this to GuiLayout.addButtonGroup as refresh to enable radio-button behavior
 *
 * !Use triggeringButtonData in your action, data might contain multiple entries!
 *
 * Since the refresh action is performed after the action, this is unavoidable.
 * If e.g. one button is active and the user clicks on a second button, the executeAction
 * (AKA ButtonGroupAction) will get called with the data of both buttons as data and the clicked
 * button as triggeringButtonData.
 * Then, this refresh action will get triggered and un-click the first button.
 *
 * Attention: only works if all buttons have unique non-null data!
 */
class RadioButtonRefreshAction : RefreshButtonsAction {
    private var lastData: Any? = null
    override fun refreshButtons(group: DataButtonGroup) {
        if (group.getActiveButtonData().isEmpty() && lastData != null) {
            group.buttons.firstOrNull { it.data == lastData }?.isActive = true
        }
        val data = group.getActiveButtonData().firstOrNull { it != lastData } ?: return
        lastData = data
        group.buttons.filter { it.data != data }.forEach { it.isActive = false }
    }
}