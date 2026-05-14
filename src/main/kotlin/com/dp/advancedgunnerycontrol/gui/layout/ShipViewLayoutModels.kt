package com.dp.advancedgunnerycontrol.gui.layout

internal data class WeaponGroupPanelLayout(
    val innerWidth: Float,
    val saveLoadPanelTop: Float,
    val weaponsHeadingTop: Float,
    val weaponPanelTop: Float,
    val weaponPanelHeight: Float,
    val weaponPanelVisible: Boolean,
    val tagListPanelTop: Float,
    val tagListPanelHeight: Float,
)

internal data class ShipEditorLayout(
    val leftColumnWidth: Float,
    val weaponGroupsWidth: Float,
    val shipPanelHeight: Float,
    val spacerHeight: Float,
    val optionsHeight: Float,
    val modifiersHeight: Float,
    val shipModeHeight: Float,
)
