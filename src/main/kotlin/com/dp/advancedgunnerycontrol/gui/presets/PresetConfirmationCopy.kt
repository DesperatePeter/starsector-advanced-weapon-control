package com.dp.advancedgunnerycontrol.gui.presets

import com.dp.advancedgunnerycontrol.gui.modals.CampaignHighlightedText
import com.dp.advancedgunnerycontrol.gui.modals.confirmationFooterText
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI

internal object PresetConfirmationCopy {
    fun body(
        groupIndex: Int,
        state: PresetControlState,
        ship: FleetMemberAPI?,
        runtimeShip: ShipAPI?,
        activePersistenceContext: ShipEditorPersistenceContext?,
        loadoutIndex: Int,
        presetPeekCache: PresetPeekCache? = null,
    ): List<CampaignHighlightedText> {
        // Protected user-workshopped preset modal copy. Do not rewrite, shorten,
        // remove, or bypass these templates unless the user explicitly asks, or
        // a behavior change makes the wording wrong and the user is told why.
        return PresetConfirmationBodyCopy.body(
            groupIndex,
            state,
            ship,
            runtimeShip,
            activePersistenceContext,
            loadoutIndex,
            presetPeekCache,
        ) + confirmationFooterText()
    }

    fun reviewBody(
        groupIndex: Int,
        state: PresetControlState,
        ship: FleetMemberAPI?,
        runtimeShip: ShipAPI?,
        activePersistenceContext: ShipEditorPersistenceContext?,
        loadoutIndex: Int,
        presetPeekCache: PresetPeekCache? = null,
    ): PresetActionReviewBody? {
        return PresetConfirmationReviewCopy.reviewBody(
            groupIndex,
            state,
            ship,
            runtimeShip,
            activePersistenceContext,
            loadoutIndex,
            presetPeekCache,
        )
    }
}
