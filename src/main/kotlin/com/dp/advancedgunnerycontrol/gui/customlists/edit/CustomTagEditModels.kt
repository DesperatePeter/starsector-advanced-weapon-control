package com.dp.advancedgunnerycontrol.gui.customlists.edit

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


internal data class CustomTagEditAvailability(
    val enabled: Boolean,
    val tooltip: String,
)

internal data class CustomTagEditRenderState(
    val definition: EditableWeaponTagDefinition?,
    val fixedTagEdit: Boolean,
    val draftValues: Map<String, String>,
    val preview: String,
    val canonicalTag: String?,
    val isValid: Boolean,
    val editSource: Pair<Int, String>?,
    val isManagerEdit: Boolean,
    val managerEditSource: String?,
    val managerEditSourceIsPendingAddition: Boolean,
    val editSourceTag: String?,
    val existingTags: Set<String>,
    val confirmAvailability: CustomTagEditAvailability,
    val copyAvailability: CustomTagEditAvailability,
    val deleteAvailability: CustomTagEditAvailability,
)

internal data class CustomTagEditSourceState(
    val canonicalTag: String?,
    val inCustomList: Boolean,
    val activeOnShip: Boolean,
)

internal enum class CustomTagEditAction {
    CONFIRM,
    COPY,
    DELETE,
}
