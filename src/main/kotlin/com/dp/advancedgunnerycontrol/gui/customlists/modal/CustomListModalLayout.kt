package com.dp.advancedgunnerycontrol.gui.customlists.modal

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.gui.session.*

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.foundation.*

import com.dp.advancedgunnerycontrol.gui.*


import kotlin.math.max

internal data class CustomListManagerHeaderLayout(
    val instructionLayout: WrappedLabelLayout,
    val instructionTop: Float,
    val renderHeight: Float,
)

internal object CustomListModalLayout {
    const val DEFAULT_MODAL_WIDTH = 620f
    const val MANAGER_MODAL_WIDTH = 532f
    const val DEBUG_COLOR_MODAL_WIDTH_MULTIPLIER = 1f

    private const val MANAGER_INSTRUCTION_MAX_LINES = 14
    // User-workshopped Manage Tags copy. Do not change unless behavior makes
    // it inaccurate or the user explicitly provides replacement text.
    private val MANAGER_INSTRUCTIONS = listOf(
        "- Tags can be added by clicking the \"Add Tag\" button",
        "- Tags can be edited by right clicking them",
        "- Tags can be marked for removal by clicking them",
        "- Ship modes can be added or marked for removal in their section",
        "- Active tags marked for deletion remain on weapon groups until toggled off, completing deletion afterwards",
        "- Weapon groups with active tags that have been edited will instantly start using the updated version of the tag",
    )

    fun targetWidth(mode: CustomListModalMode, nestedManagerEdit: Boolean): Float {
        return when (mode) {
            CustomListModalMode.EDIT_TAG -> CampaignGuiStyle.EDIT_TAG_MODAL_WIDTH
            CustomListModalMode.DEBUG_COLORS -> CampaignGuiStyle.EDIT_TAG_MODAL_WIDTH
            CustomListModalMode.RENAME_LOADOUT -> CampaignGuiStyle.EDIT_TAG_MODAL_WIDTH
            CustomListModalMode.CONFIRM_TAG_CHANGES -> MANAGER_MODAL_WIDTH
            CustomListModalMode.MANAGE_TAGS -> MANAGER_MODAL_WIDTH
        }
    }

    fun targetHeight(
        mode: CustomListModalMode,
        nestedManagerEdit: Boolean,
        editParameterCount: Int,
        managerRowCount: Int,
        changeReviewRowCount: Int,
    ): Float {
        return when (mode) {
            CustomListModalMode.EDIT_TAG -> if (nestedManagerEdit) {
                managerModalHeight(managerRowCount)
            } else {
                editModalHeight(editParameterCount)
            }
            CustomListModalMode.DEBUG_COLORS -> debugColorModalHeight()
            CustomListModalMode.RENAME_LOADOUT -> loadoutRenameModalHeight()
            CustomListModalMode.CONFIRM_TAG_CHANGES -> changeConfirmationModalHeight(changeReviewRowCount)
            CustomListModalMode.MANAGE_TAGS -> managerModalHeight(managerRowCount)
        }
    }

    fun maxModalHeight(screenHeight: Float): Float {
        return CampaignGuiStyle.maxModalHeight(screenHeight, CampaignGuiStyle.MODAL_ROW_HEIGHT)
    }

    fun modalHeightForBody(
        bodyHeight: Float,
        headingHeight: Float = CampaignGuiStyle.MODAL_HEADING_HEIGHT,
    ): Float {
        return CampaignGuiStyle.modalHeightForBody(
            bodyHeight = bodyHeight,
            headingHeight = headingHeight,
        )
    }

    fun editModalHeight(parameterCount: Int): Float {
        val componentCount = 1 + parameterCount
        return modalHeightForBody(componentStackHeight(componentCount))
    }

    fun managerModalHeight(rowCount: Int): Float {
        return modalHeightForBody(
            bodyHeight = CustomListModalListRenderer.listPanelHeight(rowCount),
            headingHeight = managerHeaderHeight()
        )
    }

    fun changeConfirmationModalHeight(rowCount: Int): Float {
        return modalHeightForBody(CustomListModalListRenderer.listPanelHeight(rowCount))
    }

    fun debugColorModalHeight(): Float {
        return modalHeightForBody(componentStackHeight(7))
    }

    fun debugColorModalWidth(): Float {
        return CampaignGuiStyle.EDIT_TAG_MODAL_WIDTH
    }

    fun loadoutRenameModalHeight(): Float {
        return modalHeightForBody(componentStackHeight(3))
    }

    fun managerInstructionLayout(dialogWidth: Float = MANAGER_MODAL_WIDTH): WrappedLabelLayout {
        return computeTextFitLayout(
            text = MANAGER_INSTRUCTIONS.joinToString("\n"),
            availableWidth = dialogWidth - 2f * CampaignGuiStyle.MODAL_PADDING,
            minRowHeight = CampaignGuiStyle.MODAL_ROW_HEIGHT,
            horizontalPadding = 0f,
            verticalPadding = 0f,
            approxCharWidthPx = CampaignGuiStyle.ACTION_LABEL_APPROX_CHAR_WIDTH,
            lineHeightPx = CampaignGuiStyle.MODAL_BODY_LINE_HEIGHT,
            maxLines = MANAGER_INSTRUCTION_MAX_LINES,
        )
    }

    fun managerHeaderLayout(dialogWidth: Float = MANAGER_MODAL_WIDTH): CustomListManagerHeaderLayout {
        val instructionLayout = managerInstructionLayout(dialogWidth)
        val instructionTop = CampaignGuiStyle.MODAL_HEADING_HEIGHT + CampaignGuiStyle.MODAL_ROW_GAP
        return CustomListManagerHeaderLayout(
            instructionLayout = instructionLayout,
            instructionTop = instructionTop,
            renderHeight = instructionTop + instructionLayout.renderHeight,
        )
    }

    fun managerHeaderHeight(dialogWidth: Float = MANAGER_MODAL_WIDTH): Float {
        return managerHeaderLayout(dialogWidth).renderHeight
    }

    private fun componentStackHeight(componentCount: Int): Float {
        return componentCount * CampaignGuiStyle.MODAL_ROW_HEIGHT +
            max(0, componentCount - 1) * CampaignGuiStyle.MODAL_ROW_GAP
    }
}
