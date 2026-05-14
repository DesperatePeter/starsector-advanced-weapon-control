package com.dp.advancedgunnerycontrol.gui.customlists.context

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext

internal class CustomListContextController(
    private val state: CustomListModalStateController,
) {
    private val currentValues = CustomListCurrentValuesProvider()
    private val rowBuilder = CustomListContextRowBuilder(
        managerState = state,
        changeReviewState = state,
        currentValues = currentValues,
    )

    fun currentTagsForActiveContext(context: ShipEditorPersistenceContext?): List<String> {
        return currentValues.currentTagsForActiveContext(context)
    }

    fun currentTagsFor(context: ShipEditorPersistenceContext): List<String> {
        return currentValues.currentTagsFor(context)
    }

    fun currentShipModesForActiveContext(context: ShipEditorPersistenceContext?): List<String> {
        return currentValues.currentShipModesForActiveContext(context)
    }

    fun currentShipModesFor(context: ShipEditorPersistenceContext): List<String> {
        return currentValues.currentShipModesFor(context)
    }

    fun managerRowsFor(context: ShipEditorPersistenceContext): List<CustomTagManagerRow> {
        return rowBuilder.managerRowsFor(context)
    }

    fun changeReviewRowsFor(context: ShipEditorPersistenceContext): List<CustomTagChangeReviewRow> {
        return rowBuilder.changeReviewRowsFor(context)
    }

    fun hasPendingRemovals(context: ShipEditorPersistenceContext): Boolean {
        return rowBuilder.hasPendingRemovals(context)
    }
}
