package com.dp.advancedgunnerycontrol.combatgui.agccombatgui

import com.dp.advancedgunnerycontrol.combatgui.GuiBase
import com.fs.starfarer.api.combat.ShipAPI
import org.lazywizard.lazylib.ui.LazyFont

class AGCCombatGui(ship: ShipAPI, font: LazyFont) : GuiBase(ship, AGCGridLayout) {
    override val title = font.createText("${ship.name}, ${ship.fleetMember.variant.fullDesignationWithHullNameForShip}", color)
}