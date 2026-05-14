package com.dp.advancedgunnerycontrol.gui.customlists.context

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*

import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext

internal class CustomListCurrentValuesProvider {
    fun currentTagsForActiveContext(context: ShipEditorPersistenceContext?): List<String> {
        return context?.let(::currentTagsFor).orEmpty()
    }

    fun currentTagsFor(context: ShipEditorPersistenceContext): List<String> {
        val shipId = context.shipId.takeIf { it.isNotBlank() } ?: return emptyList()
        return CustomWeaponTagListStore.getSupportedCustomTags(shipId)
    }

    fun currentShipModesForActiveContext(context: ShipEditorPersistenceContext?): List<String> {
        return context?.let(::currentShipModesFor).orEmpty()
    }

    fun currentShipModesFor(context: ShipEditorPersistenceContext): List<String> {
        val shipId = context.shipId.takeIf { it.isNotBlank() } ?: return emptyList()
        return CustomShipModeListStore.getCustomModeNamesForEditing(shipId)
    }
}
