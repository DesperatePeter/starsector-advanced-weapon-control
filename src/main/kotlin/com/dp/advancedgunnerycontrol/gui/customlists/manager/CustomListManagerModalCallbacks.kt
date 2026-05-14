package com.dp.advancedgunnerycontrol.gui.customlists.manager

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

import com.dp.advancedgunnerycontrol.gui.session.CustomListModalMode
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI

internal data class CustomListManagerModalCallbacks(
    val renderEditor: (CustomPanelAPI, FleetMemberAPI, ShipEditorPersistenceContext, Float, Float) -> Unit,
    val addDialogShield: (CustomPanelAPI, Float, Float, Float, Float) -> ButtonAPI?,
    val currentTags: (ShipEditorPersistenceContext) -> List<String>,
    val currentShipModes: (ShipEditorPersistenceContext) -> List<String>,
    val scrollOffset: (CustomListModalScrollTarget) -> Int,
    val setScrollOffset: (CustomListModalScrollTarget, Int) -> Unit,
    val refresh: () -> Unit,
    val close: () -> Unit,
    val switchMode: (CustomListModalMode) -> Unit,
    val applyChanges: (FleetMemberAPI, ShipEditorPersistenceContext) -> Unit,
    val startDefinitionEdit: (EditableWeaponTagDefinition) -> Unit,
    val startShipModeDefinitionEdit: (EditableWeaponTagDefinition) -> Unit,
    val startTagEdit: (FleetMemberAPI, ShipEditorPersistenceContext, String, Boolean, String) -> Unit,
    val startShipModeEdit: (String, Boolean, String) -> Unit,
)
