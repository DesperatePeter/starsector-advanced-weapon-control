package com.dp.advancedgunnerycontrol.combatgui

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.tagTooltips
import com.dp.advancedgunnerycontrol.typesandvalues.tags
import com.dp.advancedgunnerycontrol.utils.*
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import org.lazywizard.lazylib.ui.LazyFont
import java.awt.Color

class GuiLayout(private val ship: ShipAPI, private val font: LazyFont) {
    private var storageIndex = 0

    private fun createGroupAction(ship: ShipAPI, index: Int): ButtonGroupAction {
        return object : ButtonGroupAction() {
            override fun execute(data: List<Any>) {
                val tagStrings = data.filterIsInstance<String>()
                applyTagsToWeaponGroup(ship, index, tagStrings)
                if(Settings.enableCombatChangePersistance()){
                    val id = ship.fleetMemberId?: ""
                    persistTags(id, index, storageIndex, tagStrings)
                }else{
                    saveTagsInShip(ship, index, tagStrings)
                }
                Global.getLogger(this.javaClass).info(index)
                Global.getLogger(this.javaClass).info(tagStrings.toString())
            }

        }
    }

    private fun createDescriptionText(index: Int): String {
        return "Group ${index + 1}:"
    }

    private fun fetchCurrentTags(index: Int) : List<String>{
        if(Settings.enableCombatChangePersistance()){
            return loadPersistentTags(ship.fleetMemberId, index, storageIndex)
        }
        return loadTagsFromShip(ship, index)
    }

    private val xSpacing = 100f
    private val ySpacing = 50f
    private val xTooltip = 500f
    private val yTooltip = 500f
    private val color = Color.GREEN
    private val buttonGroups = List(ship.variant.weaponGroups.size) { index ->
        DataButtonGroup(
            Settings.uiPositionX().toFloat(), Settings.uiPositionY().toFloat() - index * ySpacing, 50f,
            20f, 0.5f, font, color, 5f, createGroupAction(ship, index), xTooltip, yTooltip, createDescriptionText(index)
        )
    }

    init {
        buttonGroups.forEachIndexed { index, buttonGroup ->
            val currentTags = fetchCurrentTags(index)
            tags.forEach {
                buttonGroup.addButton(it, it, tagTooltips[it] ?: "", currentTags.contains(it))
            }
        }
    }

    fun advance(){
        buttonGroups.forEach { it.advance() }
    }
    fun render(){
        buttonGroups.forEach { it.render() }
    }
}