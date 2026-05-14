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

import com.dp.advancedgunnerycontrol.gui.*



internal object CustomTagEditLauncher {
    fun startManagerTagEdit(
        state: CustomListModalStateController,
        tag: String,
        pendingAddition: Boolean,
        sourceTag: String = tag,
    ): Boolean {
        val canonicalTag = canonicalizeWeaponTagName(tag)
        val canonicalSourceTag = canonicalizeWeaponTagName(sourceTag)
        val parsed = EditableWeaponTagDefinitions.parse(canonicalTag)
        val definition = parsed
            ?.let { EditableWeaponTagDefinitions.definitionById(it.definitionId) }
            ?: EditableWeaponTagDefinitions.definitionForTemplate(canonicalTag)
            ?: return false
        state.beginManagerEditFromTag(
            definition = definition,
            sourceTag = canonicalSourceTag,
            pendingAddition = pendingAddition,
            parameterValues = parsed?.parameterValues.orEmpty(),
        )
        return true
    }

    fun startManagerShipModeEdit(
        state: CustomListModalStateController,
        mode: String,
        pendingAddition: Boolean,
        sourceMode: String = mode,
    ): Boolean {
        val canonicalMode = canonicalizeShipModeName(mode)
        val canonicalSourceMode = canonicalizeShipModeName(sourceMode)
        val parsed = EditableShipModeDefinitions.parse(canonicalMode)
        val definition = parsed
            ?.let { EditableShipModeDefinitions.definitionById(it.definitionId) }
            ?: EditableShipModeDefinitions.definitionForMode(canonicalMode)
            ?: return false
        state.beginManagerEditFromShipMode(
            definition = definition,
            sourceMode = canonicalSourceMode,
            pendingAddition = pendingAddition,
            parameterValues = parsed?.parameterValues.orEmpty(),
        )
        return true
    }

    fun startVisibleShipModeEdit(
        state: CustomListModalStateController,
        mode: String,
    ): Boolean {
        val canonicalMode = canonicalizeShipModeName(mode)
        val parsed = EditableShipModeDefinitions.parse(canonicalMode)
        val definition = parsed
            ?.let { EditableShipModeDefinitions.definitionById(it.definitionId) }
            ?: EditableShipModeDefinitions.definitionForMode(canonicalMode)
            ?: return false
        state.beginRightClickEditShipMode(
            definition = definition,
            mode = canonicalMode,
            parameterValues = parsed?.parameterValues.orEmpty(),
        )
        return true
    }

    fun startVisibleWeaponTagEdit(
        state: CustomListModalStateController,
        groupIndex: Int,
        tag: String,
    ) {
        val canonicalTag = canonicalizeWeaponTagName(tag)
        val parsed = EditableWeaponTagDefinitions.parse(canonicalTag)
        val definition = parsed
            ?.let { EditableWeaponTagDefinitions.definitionById(it.definitionId) }
            ?: EditableWeaponTagDefinitions.definitionForTemplate(canonicalTag)
        state.beginRightClickEdit(
            definition = definition,
            tag = canonicalTag,
            sourceGroupIndex = groupIndex,
            parameterValues = parsed?.parameterValues.orEmpty(),
        )
    }
}
