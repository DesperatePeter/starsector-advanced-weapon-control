package com.dp.advancedgunnerycontrol.gui.suggestedtags.actions

import com.dp.advancedgunnerycontrol.gui.modals.CampaignConfirmationModalRequest
import com.dp.advancedgunnerycontrol.gui.modals.CampaignConfirmationTone
import com.dp.advancedgunnerycontrol.gui.modals.warningBodyWithFooter
import com.dp.advancedgunnerycontrol.gui.suggestedtags.io.backupSuggestedTagsToJson
import com.dp.advancedgunnerycontrol.gui.suggestedtags.io.restoreSuggestedTagsFromJson
import com.dp.advancedgunnerycontrol.settings.Settings

internal object SuggestedTagDangerousActionRequests {
    fun suggestedDangerousActionConfirmationRequest(
        pending: SuggestedTagDangerousAction,
        onClear: () -> Unit,
    ): CampaignConfirmationModalRequest {
        return CampaignConfirmationModalRequest(
            title = pending.warningTitle(),
            body = "",
            richBody = warningBodyWithFooter(pending.confirmationBody()),
            tone = CampaignConfirmationTone.WARNING,
            onConfirm = {
                when (pending) {
                    SuggestedTagDangerousAction.RESET -> Settings.customSuggestedTags = Settings.defaultSuggestedTags
                    SuggestedTagDangerousAction.BACKUP -> backupSuggestedTagsToJson()
                    SuggestedTagDangerousAction.RESTORE -> restoreSuggestedTagsFromJson()
                }
                onClear()
            },
            onCancel = onClear
        )
    }
}
