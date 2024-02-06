package com.dp.advancedgunnerycontrol.combatgui.agccombatgui


import com.dp.advancedgunnerycontrol.settings.Settings
import org.magiclib.combatgui.MagicCombatGuiLayout
import java.awt.Color

val AGCGridLayout = MagicCombatGuiLayout(
    Settings.uiAnchorX(), Settings.uiAnchorY(), 105f, 20f, 0.5f, Color.GREEN, 5f,
    Settings.uiMessagePositionX(), Settings.uiMessagePositionY(), 25f, "graphics/fonts/insignia15LTaa.fnt",
    0.3f, 0.3f
)