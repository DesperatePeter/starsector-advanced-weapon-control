package com.dp.advancedgunnerycontrol.gui.suggestedtags.view

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.gui.controls.buttons.CampaignButtonCallbackPoller
import com.dp.advancedgunnerycontrol.gui.controls.suppression.CampaignButtonSuppression
import com.dp.advancedgunnerycontrol.gui.foundation.CampaignPanelPlugin
import com.dp.advancedgunnerycontrol.gui.foundation.CustomView
import com.dp.advancedgunnerycontrol.gui.layout.ShipEditorLayoutCalculator
import com.dp.advancedgunnerycontrol.gui.suggestedtags.controls.SuggestedTagButton
import com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers.SuggestedTagListRenderer
import com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers.SuggestedWeaponInfoRenderer
import com.dp.advancedgunnerycontrol.gui.style.CampaignPanelType
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI

/**
 * Main Customize Suggested Tags view component.
 * Renders the left Options/Filter column and the paged weapon panels that each
 * contain a suggested-tag list.
 */
class SuggestedTagGuiView(
    private val weaponListView: WeaponListView,
    private val initialTagScrollOffsets: Map<String, Int> = emptyMap(),
    private val initialTagExpandedCategoryTitles: Map<String, Set<String>> = emptyMap(),
    initialCollapsedWeaponInfoSections: Set<String> = emptySet(),
    initialExpandedAdvancedWeaponInfoSections: Set<String> = emptySet(),
    private var visibleTagList: List<String>,
) : CustomView() {
    companion object {
        private const val TOTAL_COLUMNS = 5
    }

    private var scrollDirty = false
    private val buttons: MutableList<ButtonBase<*>> = mutableListOf()
    private val collapsedWeaponInfoSections = initialCollapsedWeaponInfoSections.toMutableSet()
    private val expandedAdvancedWeaponInfoSections = initialExpandedAdvancedWeaponInfoSections.toMutableSet()
    private val weaponInfoRenderer = SuggestedWeaponInfoRenderer(
        buttons = buttons,
        collapsedWeaponInfoSections = collapsedWeaponInfoSections,
        expandedAdvancedWeaponInfoSections = expandedAdvancedWeaponInfoSections,
        onStateChanged = { scrollDirty = true },
    )
    private val tagListRenderer = SuggestedTagListRenderer(
        initialTagScrollOffsets,
        initialTagExpandedCategoryTitles,
        buttons,
        onMissingRefreshContext = { scrollDirty = true }
    )
    private var observedTagSelectionVersion = SuggestedTagButton.suggestedTagSelectionVersion
    private val buttonCallbackPoller = CampaignButtonCallbackPoller(buttons)
    private var contentPanel: CustomPanelAPI? = null
    private var leftColumnPanel: CustomPanelAPI? = null
    private var weaponPanelsPanel: CustomPanelAPI? = null

    override fun advance(amount: Float) {
        buttonCallbackPoller.processIfRequested()
    }

    override fun processInput(events: MutableList<InputEventAPI>?) {
        buttonCallbackPoller.requestPollFromInput(events)
        tagListRenderer.processInput(events, pos)
    }

    override fun buttonPressed(buttonId: Any?) {}

    fun captureTagScrollOffsets(): Map<String, Int> = tagListRenderer.captureScrollOffsets()
    fun captureTagExpandedCategoryTitles(): Map<String, Set<String>> =
        tagListRenderer.captureExpandedCategoryTitles()
    fun captureCollapsedWeaponInfoSections(): Set<String> = collapsedWeaponInfoSections.toSet()
    fun captureExpandedAdvancedWeaponInfoSections(): Set<String> = expandedAdvancedWeaponInfoSections.toSet()

    fun shouldRegenerate(): Boolean =
        scrollDirty || observedTagSelectionVersion != SuggestedTagButton.suggestedTagSelectionVersion

    private fun buildSuggestedWeaponPanel(
        panel: CustomPanelAPI,
        weaponId: String?,
        relativeLeft: Float,
        contentHeight: Float,
    ) {
        val infoPanel = panel.createCustomPanel(
            panel.position.width,
            weaponInfoRenderer.suggestedWeaponInfoHeight(panel.position.width, panel.position.height, weaponId),
            CampaignPanelPlugin(CampaignPanelType.SUGGESTED_WEAPON_PANEL)
        )
        panel.addComponent(infoPanel)
        infoPanel.position.inTL(0f, 0f)
        weaponInfoRenderer.buildWeaponInfo(infoPanel, weaponId)

        val tagTop = infoPanel.position.height + SuggestedWeaponInfoRenderer.WEAPON_INFO_TO_TAG_GAP
        val tagHeight = (panel.position.height - tagTop).coerceAtLeast(0f)
        if (tagHeight < SuggestedWeaponInfoRenderer.MIN_RENDERABLE_TAG_LIST_HEIGHT) return
        val tagPanel = panel.createCustomPanel(
            panel.position.width,
            tagHeight,
            CampaignPanelPlugin(CampaignPanelType.SUGGESTED_TAG_LIST_PANEL)
        )
        panel.addComponent(tagPanel)
        tagPanel.position.inTL(0f, tagTop)
        tagListRenderer.build(
            panel = tagPanel,
            weaponId = weaponId,
            relativeLeft = relativeLeft,
            relativeBottom = contentHeight - tagTop - tagHeight,
            visibleTags = visibleTagList,
        )
    }

    private fun buildSuggestedWeaponPanels(panel: CustomPanelAPI, contentHeight: Float, leftColumnWidth: Float) {
        val ids = weaponListView.currentIds()
        val cardWidth = panel.position.width / TOTAL_COLUMNS
        repeat(TOTAL_COLUMNS) { index ->
            val effectiveWidth = if (index == TOTAL_COLUMNS - 1) {
                panel.position.width - cardWidth * (TOTAL_COLUMNS - 1)
            } else {
                cardWidth
            }
            val column = panel.createCustomPanel(
                effectiveWidth,
                panel.position.height,
                CampaignPanelPlugin(CampaignPanelType.SUGGESTED_WEAPON_PANEL)
            )
            panel.addComponent(column)
            column.position.inTL(index * cardWidth, 0f)
            buildSuggestedWeaponPanel(
                column,
                ids.getOrNull(index),
                relativeLeft = leftColumnWidth + index * cardWidth,
                contentHeight = contentHeight
            )
        }
    }

    fun buildIn(
        panel: CustomPanelAPI,
        buildOptionsPanel: (CustomPanelAPI) -> Unit,
    ) {
        contentPanel = panel
        refreshInPlace(buildOptionsPanel)
    }

    fun refreshInPlace(
        buildOptionsPanel: (CustomPanelAPI) -> Unit,
        visibleTagList: List<String> = this.visibleTagList,
    ): Boolean {
        this.visibleTagList = visibleTagList
        val panel = contentPanel ?: return false
        clearRenderedState(panel)
        scrollDirty = false
        observedTagSelectionVersion = SuggestedTagButton.suggestedTagSelectionVersion

        val leftColumnWidth = ShipEditorLayoutCalculator.sharedLeftColumnWidth(panel.position.width)
        val weaponPanelsWidth = panel.position.width - leftColumnWidth

        val leftColumnPanel = panel.createCustomPanel(
            leftColumnWidth,
            panel.position.height,
            CampaignPanelPlugin(CampaignPanelType.LEFT_COLUMN_PANEL)
        )
        this.leftColumnPanel = leftColumnPanel
        panel.addComponent(leftColumnPanel)
        leftColumnPanel.position.inTL(0f, 0f)
        buildOptionsPanel(leftColumnPanel)

        val weaponPanelsPanel = panel.createCustomPanel(
            weaponPanelsWidth,
            panel.position.height,
            CampaignPanelPlugin(CampaignPanelType.SUGGESTED_WEAPON_PANELS_PANEL)
        )
        this.weaponPanelsPanel = weaponPanelsPanel
        panel.addComponent(weaponPanelsPanel)
        weaponPanelsPanel.position.rightOfTop(leftColumnPanel, 0f)
        buildSuggestedWeaponPanels(weaponPanelsPanel, panel.position.height, leftColumnWidth)
        return true
    }

    private fun clearRenderedState(panel: CustomPanelAPI) {
        listOfNotNull(leftColumnPanel, weaponPanelsPanel).forEach { child ->
            runCatching { panel.removeComponent(child) }
                .onFailure { ex ->
                    Global.getLogger(SuggestedTagGuiView::class.java)
                        .warn("[AGC_SUGGESTED_TAGS] Failed to remove stale suggested-tags panel", ex)
                }
        }
        leftColumnPanel = null
        weaponPanelsPanel = null
        CampaignButtonSuppression.clearRegisteredCampaignButtons()
        buttons.clear()
        tagListRenderer.clear()
    }

    override fun render(alpha: Float) {}
}
