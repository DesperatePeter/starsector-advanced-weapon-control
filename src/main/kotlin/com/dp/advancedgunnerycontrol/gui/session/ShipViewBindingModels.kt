package com.dp.advancedgunnerycontrol.gui.session

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.gui.presets.*
import com.dp.advancedgunnerycontrol.gui.modals.*

import com.fs.starfarer.api.combat.ShipAPI

data class ShipViewConfig(
    val enableTagScroll: Boolean = true,
    val drawFrame: Boolean = true,
    val runtimeShip: ShipAPI? = null,
)

data class ShipViewInitialState(
    val tagScrollOffsets: Map<Int, Int> = emptyMap(),
    val tagExpandedCategoryTitles: Map<Int, Set<String>> = emptyMap(),
    val collapsedPanelIds: Set<String> = emptySet(),
    val optionsScrollOffset: Int = 0,
    val shipModeScrollOffset: Int = 0,
    val presetControlStates: Map<Int, PresetControlState> = emptyMap(),
)

data class ShipViewPresetBindings(
    val onPresetControlStateUpdate: ((Int, PresetControlState) -> Unit)? = null,
)

data class ShipViewExternalBindings(
    val confirmationModalProvider: (() -> CampaignConfirmationModalRequest?)? = null,
    val suppressOptionHover: (() -> Unit)? = null,
    val restoreOptionHover: (() -> Unit)? = null,
)

data class CustomListModalBindings(
    val modalModeProvider: (() -> CustomListModalMode?)? = null,
    val onModalModeUpdate: ((CustomListModalMode?) -> Unit)? = null,
    val draftDefinitionIdProvider: (() -> String?)? = null,
    val onDraftDefinitionIdUpdate: ((String?) -> Unit)? = null,
    val draftValuesProvider: (() -> MutableMap<String, String>)? = null,
    val onDraftValuesUpdate: ((Map<String, String>) -> Unit)? = null,
    val editSourceGroupProvider: (() -> Int?)? = null,
    val onEditSourceGroupUpdate: ((Int?) -> Unit)? = null,
    val editSourceTagProvider: (() -> String?)? = null,
    val onEditSourceTagUpdate: ((String?) -> Unit)? = null,
    val managerScrollOffsetProvider: (() -> Int)? = null,
    val onManagerScrollOffsetUpdate: ((Int) -> Unit)? = null,
    val changeReviewScrollOffsetProvider: (() -> Int)? = null,
    val onChangeReviewScrollOffsetUpdate: ((Int) -> Unit)? = null,
    val markedForRemovalProvider: (() -> MutableSet<String>)? = null,
    val onMarkedForRemovalUpdate: ((Set<String>) -> Unit)? = null,
)
