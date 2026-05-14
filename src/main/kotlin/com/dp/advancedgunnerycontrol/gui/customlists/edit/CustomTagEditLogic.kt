package com.dp.advancedgunnerycontrol.gui.customlists.edit

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.gui.*


internal object CustomTagEditLogic {
    fun availability(
        preview: String,
        canonicalTag: String?,
        isValid: Boolean,
        editSourceState: CustomTagEditSourceState,
        existingTags: Set<String>,
        action: CustomTagEditAction,
        fixedTagEdit: Boolean = false,
    ): CustomTagEditAvailability {
        val canonicalSource = editSourceState.canonicalTag
        if (!isValid || canonicalTag == null) {
            return CustomTagEditAvailability(enabled = false, tooltip = preview)
        }
        return when (action) {
            CustomTagEditAction.CONFIRM -> confirmAvailability(
                canonicalTag = canonicalTag,
                canonicalSource = canonicalSource,
                existingTags = existingTags,
                fixedTagEdit = fixedTagEdit,
            )
            CustomTagEditAction.COPY -> copyAvailability(
                canonicalTag = canonicalTag,
                canonicalSource = canonicalSource,
                existingTags = existingTags,
                fixedTagEdit = fixedTagEdit,
            )
            CustomTagEditAction.DELETE -> deleteAvailability(
                canonicalSource = canonicalSource,
                editSourceState = editSourceState,
            )
        }
    }

    private fun confirmAvailability(
        canonicalTag: String,
        canonicalSource: String?,
        existingTags: Set<String>,
        fixedTagEdit: Boolean,
    ): CustomTagEditAvailability {
        if (fixedTagEdit) {
            return CustomTagEditAvailability(
                enabled = false,
                tooltip = "$canonicalTag has no editable values."
            )
        }
        if (canonicalSource != null && canonicalTag == canonicalSource) {
            return CustomTagEditAvailability(
                enabled = false,
                tooltip = "$canonicalTag is unchanged."
            )
        }
        if (canonicalSource != null && canonicalTag != canonicalSource && canonicalTag in existingTags) {
            return duplicateTagAvailability(canonicalTag)
        }
        if (canonicalSource == null && canonicalTag in existingTags) {
            return duplicateTagAvailability(canonicalTag)
        }
        return CustomTagEditAvailability(
            enabled = true,
            tooltip = if (canonicalSource == null) {
                "Add $canonicalTag to the current Custom weapon-tag list."
            } else {
                "Replace $canonicalSource with $canonicalTag in the current Custom list and saved weapon-group tags."
            }
        )
    }

    private fun copyAvailability(
        canonicalTag: String,
        canonicalSource: String?,
        existingTags: Set<String>,
        fixedTagEdit: Boolean,
    ): CustomTagEditAvailability {
        if (fixedTagEdit) {
            return CustomTagEditAvailability(
                enabled = false,
                tooltip = "$canonicalTag has no editable values to copy as a variant."
            )
        }
        if (canonicalSource == null) {
            return CustomTagEditAvailability(enabled = false, tooltip = "Copy is only available when editing an existing custom tag.")
        }
        if (canonicalTag == canonicalSource) {
            return CustomTagEditAvailability(
                enabled = false,
                tooltip = "$canonicalTag is unchanged."
            )
        }
        if (canonicalTag in existingTags) {
            return duplicateTagAvailability(canonicalTag)
        }
        return CustomTagEditAvailability(
            enabled = true,
            tooltip = "Add $canonicalTag as a new custom tag without changing $canonicalSource."
        )
    }

    private fun deleteAvailability(
        canonicalSource: String?,
        editSourceState: CustomTagEditSourceState,
    ): CustomTagEditAvailability {
        if (canonicalSource == null) {
            return CustomTagEditAvailability(enabled = false, tooltip = "Delete is only available when editing an existing custom tag.")
        }
        if (!editSourceState.inCustomList && !editSourceState.activeOnShip) {
            return CustomTagEditAvailability(
                enabled = false,
                tooltip = "$canonicalSource is not in the current Custom list and is not active on this ship."
            )
        }
        val target = when {
            editSourceState.inCustomList && editSourceState.activeOnShip -> {
                "from the current Custom list and from every weapon group and loadout on this ship"
            }
            editSourceState.inCustomList -> "from the current Custom weapon-tag list"
            else -> "from every weapon group and loadout on this ship"
        }
        return CustomTagEditAvailability(
            enabled = true,
            tooltip = "Remove $canonicalSource $target."
        )
    }

    private fun duplicateTagAvailability(canonicalTag: String): CustomTagEditAvailability {
        return CustomTagEditAvailability(
            enabled = false,
            tooltip = "$canonicalTag is already in the current Custom weapon-tag list."
        )
    }
}
