package com.dp.advancedgunnerycontrol.combatgui.agccombatgui

import com.dp.advancedgunnerycontrol.combatgui.GuiLayout
import com.dp.advancedgunnerycontrol.settings.Settings
import java.awt.Color

val AGCGridLayout = GuiLayout(
    Settings.uiAnchorX(), Settings.uiAnchorY(), 105f, 20f, 0.5f, Color.GREEN, 5f,
    Settings.uiMessagePositionX(), Settings.uiMessagePositionY(), 25f, "graphics/fonts/insignia15LTaa.fnt",
    0f, 0f
)