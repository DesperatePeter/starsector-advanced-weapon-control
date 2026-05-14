package com.dp.advancedgunnerycontrol.gui.customlists.modal

import com.dp.advancedgunnerycontrol.gui.customlists.state.CustomListDraftKeys
import com.dp.advancedgunnerycontrol.gui.session.CustomListModalMode

internal class CustomListModalSessionController(
    private val modalModeProvider: (() -> CustomListModalMode?)?,
    private val draftValuesProvider: (() -> Map<String, String>)?,
    private val onModalModeUpdate: ((CustomListModalMode?) -> Unit)?,
    private val onDraftDefinitionIdUpdate: ((String?) -> Unit)?,
    private val onDraftValuesUpdate: ((Map<String, String>) -> Unit)?,
    private val onManagerScrollOffsetUpdate: ((Int) -> Unit)?,
    private val onChangeReviewScrollOffsetUpdate: ((Int) -> Unit)?,
    private val onMarkedForRemovalUpdate: ((Set<String>) -> Unit)?,
    private val clearEditSource: () -> Unit,
    private val onChanged: () -> Unit,
) {
    fun closeModal() {
        val closingMode = modalModeProvider?.invoke()
        onModalModeUpdate?.invoke(null)
        onDraftDefinitionIdUpdate?.invoke(null)
        onDraftValuesUpdate?.invoke(draftValuesAfterClose(closingMode))
        clearEditSource()
        onManagerScrollOffsetUpdate?.invoke(0)
        onChangeReviewScrollOffsetUpdate?.invoke(0)
        onMarkedForRemovalUpdate?.invoke(emptySet())
        onChanged()
    }

    private fun draftValuesAfterClose(closingMode: CustomListModalMode?): Map<String, String> {
        if (closingMode != CustomListModalMode.DEBUG_COLORS) return emptyMap()
        return draftValuesProvider
            ?.invoke()
            ?.filterKeys { it in debugColorDraftKeys }
            .orEmpty()
    }

    companion object {
        private val debugColorDraftKeys = setOf(
            CustomListDraftKeys.DebugColor.INDEX,
            CustomListDraftKeys.DebugColor.RED,
            CustomListDraftKeys.DebugColor.GREEN,
            CustomListDraftKeys.DebugColor.BLUE,
            CustomListDraftKeys.DebugColor.PERSISTENCE,
        )
    }
}
