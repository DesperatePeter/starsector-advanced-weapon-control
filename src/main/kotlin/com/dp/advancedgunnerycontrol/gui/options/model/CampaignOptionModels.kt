package com.dp.advancedgunnerycontrol.gui.options.model

import com.dp.advancedgunnerycontrol.gui.modals.CampaignConfirmationTone
import com.dp.advancedgunnerycontrol.gui.style.CampaignActionButtonKind
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.dp.advancedgunnerycontrol.gui.layout.CampaignActionRowLayout
import java.awt.Color

data class CampaignOptionRow(
    val label: String,
    val tooltip: String,
    val shortcut: Int? = null,
    val activationShortcut: Int? = shortcut,
    val kind: CampaignActionButtonKind = CampaignActionButtonKind.UNCOLOURED,
    val rebuildAfter: Boolean = true,
    val confirmationKey: String? = null,
    val confirmationTitle: String? = null,
    val confirmationDescription: String? = null,
    val confirmationTone: CampaignConfirmationTone = CampaignConfirmationTone.CAUTION,
    val rightClickCallback: (() -> Unit)? = null,
    val textColor: Color = CampaignGuiStyle.DEFAULT_TEXT_COLOUR,
    val callback: () -> Unit,
)

data class CampaignOptionsRenderResult(
    val rowOffset: Int,
    val maxRowOffset: Int,
)

data class CampaignOptionsLayout(
    val rows: List<CampaignActionRowLayout<CampaignOptionRow>>,
    val requiredHeight: Float,
)
