package com.dp.advancedgunnerycontrol.gui.customlists.modal

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.gui.style.CampaignActionButtonKind

internal data class ModalFooterButtonSpec(
    val data: Any,
    val kind: CampaignActionButtonKind,
    val enabled: Boolean = true,
    val labelText: String,
    val tooltip: String? = null,
    val showTooltipWhileInactive: Boolean = false,
    val onClick: () -> Unit,
)
