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

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.*



internal data class CustomTagAddCandidate(
    val id: String,
    val label: String,
    val definition: EditableWeaponTagDefinition?,
    val directTag: String?,
)

internal data class CustomTagArchetype(
    val id: String,
    val label: String,
    val definition: EditableWeaponTagDefinition?,
    val directTag: String?,
    val category: WeaponTagCategory,
)

internal sealed class CustomTagManagerRow {
    data class ListSectionHeading(
        val id: String,
        val label: String,
        val expanded: Boolean,
        val active: Boolean,
        val count: Int,
    ) : CustomTagManagerRow()

    data class CategoryHeading(
        val category: WeaponTagCategory,
        val expanded: Boolean,
        val active: Boolean,
    ) : CustomTagManagerRow()

    data class ArchetypeHeading(
        val archetype: CustomTagArchetype,
        val expanded: Boolean,
        val active: Boolean,
    ) : CustomTagManagerRow()

    data class TagEntry(
        val archetype: CustomTagArchetype,
        val tag: String,
        val markedForRemoval: Boolean,
        val pendingAddition: Boolean,
        val pendingEdit: Boolean,
    ) : CustomTagManagerRow()

    data class AddTagEntry(
        val archetype: CustomTagArchetype,
    ) : CustomTagManagerRow()

    data class ShipModeEntry(
        val mode: String,
        val markedForRemoval: Boolean,
        val pendingAddition: Boolean,
        val pendingEdit: Boolean,
    ) : CustomTagManagerRow()

    data class AddShipModeEntry(
        val mode: String,
        val definition: EditableWeaponTagDefinition? = null,
    ) : CustomTagManagerRow()
}

internal enum class CustomTagManagerTagStatus(
    val sortRank: Int,
    val suffix: String,
    val kind: CampaignActionButtonKind,
) {
    ADDING(0, "(adding)", CampaignActionButtonKind.CONFIRM),
    EDITING(1, "(editing)", CampaignActionButtonKind.SAVE),
    REMOVING(2, "(removing)", CampaignActionButtonKind.CANCEL),
    NORMAL(3, "", CampaignActionButtonKind.UNCOLOURED),
}

internal data class CustomTagManagerTagState(
    val pendingRemovals: Set<String>,
    val pendingAdditions: Set<String>,
    val editedTargets: Set<String>,
) {
    fun isMarkedForRemoval(tag: String): Boolean = tag in pendingRemovals
    fun isPendingAddition(tag: String): Boolean = tag in pendingAdditions
    fun isPendingEdit(tag: String): Boolean = tag in editedTargets

    fun statusFor(tag: String): CustomTagManagerTagStatus {
        return when {
            isPendingAddition(tag) -> CustomTagManagerTagStatus.ADDING
            isPendingEdit(tag) && !isMarkedForRemoval(tag) -> CustomTagManagerTagStatus.EDITING
            isMarkedForRemoval(tag) -> CustomTagManagerTagStatus.REMOVING
            else -> CustomTagManagerTagStatus.NORMAL
        }
    }
}

internal enum class CustomTagChangeReviewSection(
    val id: String,
    val label: String,
    val kind: CampaignActionButtonKind,
) {
    ADDED("added", "Adding", CampaignActionButtonKind.CONFIRM),
    MODIFIED("modified", "Editing", CampaignActionButtonKind.SAVE),
    REMOVED("removed", "Removing", CampaignActionButtonKind.CANCEL),
    ;

    fun idFor(parentSectionId: String): String = "$parentSectionId:$id"
}

internal sealed class CustomTagChangeReviewRow {
    data class ListHeading(
        val id: String,
        val label: String,
        val count: Int,
        val expanded: Boolean,
    ) : CustomTagChangeReviewRow()

    data class Heading(
        val id: String,
        val section: CustomTagChangeReviewSection,
        val count: Int,
        val expanded: Boolean,
    ) : CustomTagChangeReviewRow()

    data class Entry(
        val label: String,
        val kind: CampaignActionButtonKind,
    ) : CustomTagChangeReviewRow()
}
