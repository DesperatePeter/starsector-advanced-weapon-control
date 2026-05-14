package com.dp.advancedgunnerycontrol.gui.panels.weapongroups

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.gui.controls.weapontags.TagButton
import com.dp.advancedgunnerycontrol.gui.panels.taglists.PinnedTagListRenderer

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*

import com.dp.advancedgunnerycontrol.gui.entrypoints.AGCGUI

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.*


import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI

/**
 * Weapon-group tag-list component.
 * Renders Active Tags and Inactive Tags sections for each weapon group and owns
 * per-group tag scroll/category state inside ShipView.
 */
class WeaponGroupTagListRenderer(
    initialScrollOffsets: Map<Int, Int>,
    initialExpandedCategoryTitles: Map<Int, Set<String>> = emptyMap(),
    private val buttons: MutableList<ButtonBase<*>>,
    private val runtimeShip: ShipAPI? = null,
    private val onMissingRefreshContext: () -> Unit = {},
    private val onGroupTagsChanged: (Int, List<String>) -> Unit = { _, _ -> },
) {
    private data class WeaponGroupTagListContext(
        val ship: FleetMemberAPI,
        val groupIndex: Int,
    )

    private data class VisibleTagBase(
        val shipId: String,
        val advancedMode: Boolean,
        val defaultTags: List<String>,
        val customListVersion: Int,
        val configuredTags: List<String>,
        val configuredTagSet: Set<String>,
    )

    private var visibleTagBase: VisibleTagBase? = null
    private val loadedTagsByGroup = mutableMapOf<Int, List<String>>()
    private var persistenceContext: ShipEditorPersistenceContext? = null

    private val renderer = PinnedTagListRenderer<Int, WeaponGroupTagListContext>(
        initialScrollOffsets = initialScrollOffsets,
        initialExpandedCategoryTitles = initialExpandedCategoryTitles,
        scrollStep = CampaignGuiStyle.WEAPON_TAG_SCROLL_STEP,
        selectedTags = { context -> loadCurrentTags(context.ship, context.groupIndex) },
        availableTags = { context -> availableTagsForShip(context.ship, context.groupIndex) },
        sanitizedSelectedTags = { context, allVisibleTags ->
            TagButton.repairAndPersistSelectedTags(
                context.ship,
                context.groupIndex,
                runtimeShip,
                allVisibleTags,
                loadedTagsOverride = loadCurrentTags(context.ship, context.groupIndex),
                persistenceContext = persistenceContextFor(context.ship),
            ).also { sanitized ->
                loadedTagsByGroup[context.groupIndex] = sanitized
            }
        },
        unavailableTags = { context, candidateTags, selectedTags ->
            TagButton.unavailableTagsForSelection(
                context.ship,
                context.groupIndex,
                candidateTags,
                selectedTags,
            )
        },
        removeButtons = { context ->
            buttons.removeAll { it is TagButton && it.group == context.groupIndex }
        },
        addTagButtons = { context, panel, tags, pinned, allVisibleTags, selectedTags, unavailableTags, onSelectionChanged ->
            buttons.addAll(
                TagButton.createCampaignTagButtonGroup(
                    context.ship,
                    context.groupIndex,
                    panel,
                    tags,
                    pinned,
                    runtimeShip,
                    allVisibleTags,
                    onSelectionChanged = { updatedTags ->
                        loadedTagsByGroup[context.groupIndex] = updatedTags
                        onSelectionChanged()
                        onGroupTagsChanged(context.groupIndex, updatedTags)
                    },
                    sanitizedTagsOverride = selectedTags,
                    unavailableTagsOverride = unavailableTags,
                    persistenceContext = persistenceContextFor(context.ship),
                )
            )
        },
        addScrollIndicatorButton = { _, button -> buttons.add(button) },
        removeScrollIndicatorButton = { _, button -> buttons.remove(button) },
        onMissingRefreshContext = onMissingRefreshContext,
    )

    fun bindPersistenceContext(context: ShipEditorPersistenceContext?) {
        if (persistenceContext === context) return
        persistenceContext = context
        visibleTagBase = null
        loadedTagsByGroup.clear()
    }

    fun clear() {
        renderer.clear()
        visibleTagBase = null
        loadedTagsByGroup.clear()
    }

    fun captureScrollOffsets(): Map<Int, Int> = renderer.captureScrollOffsets()

    fun captureExpandedCategoryTitles(): Map<Int, Set<String>> = renderer.captureExpandedCategoryTitles()

    fun processInput(events: MutableList<InputEventAPI>?, panelPos: PositionAPI?): Boolean =
        renderer.processInput(events, panelPos)

    fun build(
        panel: CustomPanelAPI,
        ship: FleetMemberAPI,
        groupIndex: Int,
        relativeScrollLeft: Float,
        relativeScrollRight: Float,
        relativeScrollBottom: Float,
        relativeScrollTop: Float,
    ) {
        renderer.build(
            key = groupIndex,
            data = WeaponGroupTagListContext(ship, groupIndex),
            panel = panel,
            relativeScrollLeft = relativeScrollLeft,
            relativeScrollRight = relativeScrollRight,
            relativeScrollBottom = relativeScrollBottom,
            relativeScrollTop = relativeScrollTop,
        )
    }

    fun refreshGroup(groupIndex: Int) {
        loadedTagsByGroup.remove(groupIndex)
        visibleTagBase = null
        renderer.refreshVisibleList(groupIndex)
    }

    private fun loadCurrentTags(ship: FleetMemberAPI, groupIndex: Int): List<String> {
        return loadedTagsByGroup.getOrPut(groupIndex) {
            persistenceContextFor(ship).loadWeaponTags(groupIndex, AGCGUI.storageIndex)
        }
    }

    private fun availableTagsForShip(ship: FleetMemberAPI, groupIndex: Int): List<String> {
        val base = visibleTagBaseForShip(ship)
        if (!base.advancedMode) return base.configuredTags
        val activeOffListTags = loadCurrentTags(ship, groupIndex)
            .filter(CustomWeaponTagListStore::isSupportedTag)
            .filterNot { it in base.configuredTagSet }
        // List switching is intentionally non-destructive: active off-list tags
        // are pinned so they can be seen and disabled, then disappear naturally.
        return canonicalizeWeaponTagNames(base.configuredTags + activeOffListTags)
    }

    private fun visibleTagBaseForShip(ship: FleetMemberAPI): VisibleTagBase {
        val defaultTags = Settings.getCurrentWeaponTagList()
        val advancedMode = Settings.isAdvancedMode
        val customListVersion = if (advancedMode) CustomWeaponTagListStore.currentListVersion() else 0
        val shipId = if (advancedMode) persistenceContextFor(ship).shipId else ""
        visibleTagBase?.let { cached ->
            if (
                cached.shipId == shipId &&
                cached.advancedMode == advancedMode &&
                cached.defaultTags == defaultTags &&
                cached.customListVersion == customListVersion
            ) {
                return cached
            }
        }
        val configuredTags = if (advancedMode) {
            CustomWeaponTagListStore.effectiveTagsForShipOrDefault(shipId, defaultTags)
        } else {
            defaultTags
        }
        return VisibleTagBase(
            shipId = shipId,
            advancedMode = advancedMode,
            defaultTags = defaultTags,
            customListVersion = customListVersion,
            configuredTags = configuredTags,
            configuredTagSet = configuredTags.toSet()
        ).also { visibleTagBase = it }
    }

    private fun persistenceContextFor(ship: FleetMemberAPI): ShipEditorPersistenceContext {
        return persistenceContext?.takeIf { it.member === ship }
            ?: ShipEditorPersistenceContext(ship, runtimeShip)
    }
}
