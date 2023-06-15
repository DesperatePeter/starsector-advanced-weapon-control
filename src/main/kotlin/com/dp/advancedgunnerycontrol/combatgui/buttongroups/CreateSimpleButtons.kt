package com.dp.advancedgunnerycontrol.combatgui.buttongroups

/**
 * Simple implementation of CreateButtonsAction interface that creates
 * a button for each entry in names.
 * @param names list of display names of buttons. Must not be null and defines the number of buttons created
 * @param data list of data that buttons shall contain. If null or too short, button names will be used as data.
 * @param tooltips list of tooltips to use for buttons. If null or too short, no tooltip will be used.
 */
class CreateSimpleButtons(
    private val names: List<String>,
    private val data: List<Any>?,
    private val tooltips: List<String>?
) : CreateButtonsAction {
    override fun createButtons(group: DataButtonGroup) {
        names.forEachIndexed { index, s ->
            val d = data?.getOrNull(index) ?: s
            val tt = tooltips?.getOrNull(index) ?: ""
            group.addButton(s, d, tt, false)
        }
    }
}