package com.dp.advancedgunnerycontrol.combatgui.agccombatgui

import com.dp.advancedgunnerycontrol.combatgui.GuiLayout
import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.Global
import java.awt.Color

val AGCGridLayout = GuiLayout(
    Settings.uiAnchorX(), Settings.uiAnchorY(), 100f, 20f, 0.5f, Global.getSettings().basePlayerColor, 5f,
    Settings.uiMessagePositionX(), Settings.uiMessagePositionY(), 25f, "graphics/fonts/insignia15LTaa.fnt",
    0f, 0f
)