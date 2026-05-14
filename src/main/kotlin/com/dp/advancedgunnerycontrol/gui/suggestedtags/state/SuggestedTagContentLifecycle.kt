package com.dp.advancedgunnerycontrol.gui.suggestedtags.state

import com.dp.advancedgunnerycontrol.gui.panels.content.replaceCampaignRootContentPanel
import com.dp.advancedgunnerycontrol.gui.suggestedtags.view.SuggestedTagGuiView
import com.dp.advancedgunnerycontrol.gui.suggestedtags.view.WeaponListView
import com.fs.starfarer.api.ui.CustomPanelAPI

data class SuggestedTagEditorContent(
    val panel: CustomPanelAPI,
    val view: SuggestedTagGuiView?,
)

internal object SuggestedTagContentLifecycle {
    fun rebuildContent(
        weaponListView: WeaponListView,
        sessionState: SuggestedTagSessionState,
        visibleTagList: List<String>,
        root: CustomPanelAPI,
        content: CustomPanelAPI?,
        onCleared: () -> Unit,
        buildOptionsPanel: (CustomPanelAPI) -> Unit,
        renderConfirmationModal: (CustomPanelAPI) -> Unit,
    ): SuggestedTagEditorContent {
        val nextContent = replaceCampaignRootContentPanel(
            root = root,
            content = content,
            plugin = SuggestedTagGuiView(
                weaponListView,
                sessionState.tagScrollOffsets,
                sessionState.tagExpandedCategoryTitles,
                sessionState.collapsedWeaponInfoSections,
                sessionState.expandedAdvancedWeaponInfoSections,
                visibleTagList = visibleTagList,
            ),
            onCleared = onCleared,
        )
        val nextView = nextContent.plugin as? SuggestedTagGuiView
        nextView?.buildIn(nextContent, buildOptionsPanel)
        weaponListView.markRendered()
        renderConfirmationModal(nextContent)
        return SuggestedTagEditorContent(nextContent, nextView)
    }

    fun refreshContent(
        weaponListView: WeaponListView,
        view: SuggestedTagGuiView?,
        visibleTagList: List<String>,
        buildOptionsPanel: (CustomPanelAPI) -> Unit,
    ): Boolean {
        if (view?.refreshInPlace(buildOptionsPanel, visibleTagList) != true) {
            return false
        }
        weaponListView.markRendered()
        return true
    }
}
