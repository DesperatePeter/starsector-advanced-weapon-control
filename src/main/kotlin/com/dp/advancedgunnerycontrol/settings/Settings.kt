package com.dp.advancedgunnerycontrol.settings

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*

import com.dp.advancedgunnerycontrol.persistence.StorageBaseIntKey
import com.fs.starfarer.api.Global
import org.lwjgl.input.Keyboard
import org.magiclib.combatgui.buttons.MagicCombatButtonBase
import org.magiclib.util.MagicSettings
import kotlin.math.max
import kotlin.math.min

object Settings : SettingsDefinition() {
    private val classicTagList = addSetting<List<String>>("classicTagList", listOf(), false)
    private val noviceTagList = addSetting<List<String>>("noviceTagList", listOf(), false)
    private val completeTagList = addSetting<List<String>>("completeTagList", listOf(), false)
    private val simpleTagList = addSetting<List<String>>("simpleTagList", listOf(), false)
    private val shipModeList = addSetting<List<String>>("shipModeList", listOf(), false)
    private val noviceShipModeList = addSetting<List<String>>("noviceShipModeList", listOf(), false)
    private val listVariant = addSetting("listVariant", "classic")
    val enableCustomAI = addSetting<Boolean>("enableCustomAI", true)
    val customAIRecursionLevel = addSetting<Int>("customAIRecursionLevel", 1)
    val forceCustomAI = addSetting<Boolean>("forceCustomAI", false)
    val customAITriggerHappiness = addSetting<Float>("customAITriggerHappiness", 1.0f)
    val customAIPerfectTargetLeading = addSetting<Boolean>("customAIAlwaysUsesBestTargetLeading", false)
    val customAIFriendlyFireCaution = addSetting<Float>("customAIFriendlyFireCaution", 1.25f)
    val customAIFriendlyFireComplexity = addSetting<Int>("customAIFriendlyFireAlgorithmComplexity", 1, true)
    val uiDisplayFrames = addSetting<Int>("messageDisplayDuration", 250)
    val uiMessagePositionX = addSetting<Float>("messagePositionX", 0.2f)
    val uiMessagePositionY = addSetting<Float>("messagePositionY", 0.4f)
    val uiAnchorX = addSetting<Float>("combatUiAnchorX", 0.025f)
    val uiAnchorY = addSetting<Float>("combatUiAnchorY", 0.8f)
    val combatGuiHotkey = addSetting<Int>("inCombatGuiHotkey", Keyboard.KEY_J)
    val guiHotkey = addSetting<Int>("GUIHotkey", Keyboard.KEY_J)
    val mergeHotkey = addSetting<Int>("mergeHotkey", Keyboard.KEY_K)
    val disableTagsHotkey = addSetting<Int>("disableTagsHotkey", Keyboard.KEY_L)
    val enablePersistentModes = addSetting<Boolean>("enablePersistentFireModes", true)
    val enableCombatChangePersistence = addSetting<Boolean>("persistChangesInCombat", true)
    val autoApplySavedTagsInCombat = addSetting<Boolean>("enableAutoSaveLoad", true)
    val maxLoadouts = addSetting<Int>("maxLoadouts", 3)
    val loadoutNames = addSetting<List<String>>("loadoutNames", listOf(), false)
    private var originalClassicTagList: MutableList<String> = mutableListOf()
    private var originalNoviceTagList: MutableList<String> = mutableListOf()
    private var originalCompleteTagList: MutableList<String> = mutableListOf()
    private var originalSimpleTagList: MutableList<String> = mutableListOf()
    val automaticallyReapplyPlayerShipModes = addSetting<Boolean>("automaticallyReapplyPlayerShipModes", true)
    val allowEnemyShipModeApplication = addSetting<Boolean>("allowEnemyShipModeApplication", true)
    val collisionRadiusMultiplier = addSetting<Float>("collisionRadiusMultiplier", 0.8f, true)
    val suppressHudWarning = addSetting("suppressHudWarning", false)


    // mode/suffix params
    val opportunistKineticThreshold = addSetting<Float>("opportunist_kineticThreshold", 0.5f, true)
    val opportunistHEThreshold = addSetting<Float>("opportunist_HEThreshold", 0.15f, true)
    val ventFluxThreshold = addSetting<Float>("vent_flux", 0.75f, true)
    val aggressiveVentFluxThreshold = addSetting<Float>("aggressiveVent_flux", 0.25f, true)
    val ventSafetyFactor = addSetting<Float>("vent_safetyFactor", 2f, true)
    val aggressiveVentSafetyFactor = addSetting<Float>("aggressiveVent_safetyFactor", 0.25f, true)
    val retreatHullThreshold = addSetting<Float>("retreat_hull", 0.5f, true)
    val shieldOffThreshold = addSetting<Float>("shieldsOff_flux", 0.5f, true)
    val conserveAmmo = addSetting<Float>("conserveAmmo_ammo", 0.5f, true)
    val conservePDAmmo = addSetting<Float>("conservePDAmmo_ammo", 0.8f, true)
    val noPDWasteCleanupDamageCap = addSetting<Float>("noPDWasteCleanupDamageCap", 100f, true)
    val showEnergyDamageTypeExclusionOption = addSetting<Boolean>("showEnergyDamageTypeExclusionOption", false, true)
    val showMissileDamageTypeExclusionOption = addSetting<Boolean>("showMissileDamageTypeExclusionOption", false, true)
    val showProjectileDamageTypeExclusionOption = addSetting<Boolean>("showProjectileDamageTypeExclusionOption", false, true)
    val directRetreat = addSetting<Boolean>("retreat_shouldDirectRetreat", false, tryLunar = true)
    val opportunistModifier = addSetting<Float>("opportunist_triggerHappinessModifier", 1.0f, true)
    val targetShieldThreshold = addSetting<Float>("targetShields_threshold", 0.1f, true)
    val avoidShieldThreshold = addSetting<Float>("avoidShields_threshold", 0.2f, true)
    val ignoreFighterShield = addSetting<Boolean>("ignoreFighterShields", true)
    val targetShieldAtTotalFlux = addSetting<Float>("targetShieldsAtFT_flux", 0.2f, true)
    val avoidShieldAtTotalFlux = addSetting<Float>("avoidShieldsAtFT_flux", 0.2f, true)
    val softFluxTotalFluxCap = addSetting<Float>("SFTUpperFluxLimit", 0.9f, true)
    val prioXModifier = addSetting<Float>("prioXModifier", 10f, true)
    val useExactBoundsForFiringDecision = addSetting<Boolean>("useExactBoundsForFiringDecision", true)
    val useConeFFForSpreadOver = addSetting("useConeFFAboveSpread", 4f, true)

    val enableWeaponHighlighting = addSetting<Boolean>("enableWeaponHighlighting", true, tryLunar = true)
    val enableTooltipsOnHover = addSetting<Boolean>("enableHoverTooltips", true, tryLunar = true)
    val enableTooltipBoxes = addSetting<Boolean>("enableHoverTooltipBoxes", true, tryLunar = true)
    val enableButtonHoverSound = addSetting<Boolean>("enableButtonHoverSound", true, tryLunar = true)
    val enableButtonHoverEffects = addSetting<Boolean>("enableButtonHoverEffects", true, tryLunar = true)
    val enableButtonOutlines = addSetting<Boolean>("enableButtonOutlines", true, tryLunar = true)
    val showSuggestedTagAdvancedInfo = addSetting<Boolean>("showSuggestedTagAdvancedInfo", false, tryLunar = true)
    val enableRefitScreenIntegration = addSetting<Boolean>("enableRefitScreenIntegration", true)
    val showRefitScreenButton = addSetting<Boolean>("showRefitScreenButton", true)
    val spamSystemPreventsDeactivation = addSetting<Boolean>("spamSystemPreventsDeactivation", false)

    var isAdvancedMode : Boolean by CampaignSettingDelegate("isAdvancedMode",
        defaultValue = false,
        getFromLunaSettingsIfPossible = false
    )
    var autoApplySuggestedTags : Boolean by CampaignSettingDelegate("autoApplySuggestedTags", false)
    var customSuggestedTags: Map<String, List<String>> by CampaignSettingDelegate("customSuggestedTags", mapOf(), false)
    var suggestedTagEditorListMode: String by CampaignSettingDelegate("suggestedTagEditorListMode", "custom_global", false)
    var suggestedTagEditorFilters: List<String> by CampaignSettingDelegate("suggestedTagEditorFilters", listOf(), false)
    private var renamedLoadoutNames: List<String> by CampaignSettingDelegate("renamedLoadoutNames", listOf(), false)

    var weaponBlacklist = listOf<String>()
        private set

    var defaultSuggestedTags = mapOf<String, List<String>>()
        private set

    private var cachedSuggestedTagSource: Map<String, List<String>>? = null
    private var cachedSuggestedTags: Map<String, List<String>>? = null
    private var cachedCurrentWeaponTagListAdvanced: Boolean? = null
    private var cachedCurrentWeaponTagListMode: WeaponTagListMode? = null
    private var cachedCurrentWeaponTagListSource: List<String>? = null
    private var cachedCurrentWeaponTagList: List<String>? = null
    private val cachedWeaponTagListsByMode = mutableMapOf<WeaponTagListMode, Pair<List<String>, List<String>>>()

    private const val MAX_LOADOUT_NAME_LENGTH = 24

    fun getCurrentSuggestedTags() : Map<String, List<String>>{
        val source = if(customSuggestedTags.isEmpty()) defaultSuggestedTags else customSuggestedTags
        cachedSuggestedTags?.let { cached ->
            if (cachedSuggestedTagSource === source) return cached
        }
        return source.mapValues { canonicalizeWeaponTagNames(it.value) }
            .also {
                cachedSuggestedTagSource = source
                cachedSuggestedTags = it
            }
    }

    private fun clearSuggestedTagCaches() {
        cachedSuggestedTagSource = null
        cachedSuggestedTags = null
    }

    private fun clearWeaponTagListCaches() {
        cachedCurrentWeaponTagListAdvanced = null
        cachedCurrentWeaponTagListMode = null
        cachedCurrentWeaponTagListSource = null
        cachedCurrentWeaponTagList = null
        cachedWeaponTagListsByMode.clear()
    }

    fun loadoutDisplayName(index: Int): String {
        return renamedLoadoutNames.getOrNull(index)
            ?.takeIf { it.isNotBlank() }
            ?: loadoutNames().getOrNull(index)
                ?.takeIf { it.isNotBlank() }
            ?: "Loadout ${index + 1}"
    }

    fun validateLoadoutName(index: Int, name: String): String? {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return "Loadout name cannot be empty."
        if (trimmed.length > MAX_LOADOUT_NAME_LENGTH) return "Loadout name is too long."
        val duplicateIndex = (0 until maxLoadouts().coerceAtLeast(1))
            .firstOrNull { otherIndex ->
                otherIndex != index && loadoutDisplayName(otherIndex).equals(trimmed, ignoreCase = true)
            }
        if (duplicateIndex != null) return "Loadout name already exists."
        return null
    }

    fun renameLoadout(index: Int, name: String): Boolean {
        if (index !in 0 until maxLoadouts().coerceAtLeast(1)) return false
        val trimmed = name.trim()
        if (validateLoadoutName(index, trimmed) != null) return false
        val names = MutableList(maxLoadouts().coerceAtLeast(index + 1)) { loadoutNames().getOrNull(it).orEmpty() }
        renamedLoadoutNames.forEachIndexed { existingIndex, existingName ->
            if (existingIndex < names.size) names[existingIndex] = existingName
        }
        names[index] = trimmed
        renamedLoadoutNames = names
        return true
    }

    fun getCurrentShipModes(): List<ShipModes>{
        return getCurrentShipModeNames().mapNotNull { parseShipMode(it)?.mode }
    }

    fun getCurrentShipModeNames(): List<String> {
        return configuredShipModeNames(if (isAdvancedMode) shipModeList() else noviceShipModeList())
    }

    fun getShipModeNamesForMode(mode: WeaponTagListMode): List<String> {
        return configuredShipModeNames(
            when (mode) {
                WeaponTagListMode.NOVICE -> noviceShipModeList()
                WeaponTagListMode.CLASSIC,
                WeaponTagListMode.COMPLETE,
                WeaponTagListMode.CUSTOM,
                WeaponTagListMode.CUSTOM_GLOBAL -> shipModeList()
            }
        )
    }

    private fun configuredShipModeNames(modes: List<String>): List<String> {
        return modes.mapNotNull { parseShipMode(it)?.canonicalName }
    }

    // Legacy storage shape: ship modes are persisted as an int-keyed map.
    // Only key 0 is used; other keys are ignored for existing-save compatibility.
    var shipModeStorage: List<StorageBaseIntKey<List<String>>> = listOf()
    var tagStorage: List<StorageBaseIntKey<List<String>>> = listOf()

    fun getCurrentWeaponTagList() : List<String>{
        val advanced = isAdvancedMode
        val mode = if (advanced) currentConfiguredWeaponTagListMode() else null
        val tags = if(advanced){
            when(mode){
                WeaponTagListMode.CLASSIC -> classicTagList()
                WeaponTagListMode.NOVICE -> noviceTagList()
                WeaponTagListMode.COMPLETE -> completeTagList()
                WeaponTagListMode.CUSTOM,
                WeaponTagListMode.CUSTOM_GLOBAL -> classicTagList()
                null -> classicTagList()
            }

        } else {
            simpleTagList()
        }
        cachedCurrentWeaponTagList?.let { cached ->
            if (cachedCurrentWeaponTagListAdvanced == advanced &&
                cachedCurrentWeaponTagListMode == mode &&
                cachedCurrentWeaponTagListSource === tags
            ) {
                return cached
            }
        }
        return canonicalizeWeaponTagNames(tags).also {
            cachedCurrentWeaponTagListAdvanced = advanced
            cachedCurrentWeaponTagListMode = mode
            cachedCurrentWeaponTagListSource = tags
            cachedCurrentWeaponTagList = it
        }
    }

    fun currentConfiguredWeaponTagListMode(): WeaponTagListMode {
        return when (listVariant()) {
            WeaponTagListMode.CLASSIC.storageId -> WeaponTagListMode.CLASSIC
            WeaponTagListMode.NOVICE.storageId -> WeaponTagListMode.NOVICE
            WeaponTagListMode.COMPLETE.storageId -> WeaponTagListMode.COMPLETE
            else -> WeaponTagListMode.NOVICE
        }
    }

    fun getWeaponTagListForMode(mode: WeaponTagListMode): List<String> {
        val tags = when (mode) {
            WeaponTagListMode.NOVICE -> noviceTagList()
            WeaponTagListMode.CLASSIC -> classicTagList()
            WeaponTagListMode.COMPLETE -> completeTagList()
            // Custom lists are ship-specific and live in CustomWeaponTagListStore.
            WeaponTagListMode.CUSTOM,
            WeaponTagListMode.CUSTOM_GLOBAL -> classicTagList()
        }
        cachedWeaponTagListsByMode[mode]?.let { (source, cached) ->
            if (source === tags) return cached
        }
        return canonicalizeWeaponTagNames(tags).also {
            cachedWeaponTagListsByMode[mode] = tags to it
        }
    }

    override fun readSettings() {
        super.readSettings()
        weaponBlacklist = MagicSettings.getList(Values.THIS_MOD_NAME, Values.WEAPON_BLACKLIST_KEY)
        defaultSuggestedTags = MagicSettings.getStringMap(Values.THIS_MOD_NAME, Values.SUGGESTED_TAGS_KEY)
            .mapValues { entry ->
                entry.value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }
        clearSuggestedTagCaches()
        clearWeaponTagListCaches()
    }

    override fun applySettings() {
        clearWeaponTagNameCaches()
        clearEditableWeaponTagDefinitionCaches()
        clearSuggestedTagCaches()
        clearWeaponTagListCaches()
        super.applySettings()
        maxLoadouts.set(maxLoadouts().coerceAtLeast(1))
        Values.storageIndex = Values.storageIndex.coerceIn(0, maxLoadouts() - 1)
        shipModeStorage = StorageBaseIntKey.assembleStorageArray("$" + Values.THIS_MOD_NAME + "shipModes")
        tagStorage = StorageBaseIntKey.assembleStorageArray("$" + Values.THIS_MOD_NAME + "tags")
        originalClassicTagList = classicTagList().toMutableList()
        originalNoviceTagList = noviceTagList().toMutableList()
        originalCompleteTagList = completeTagList().toMutableList()
        originalSimpleTagList = simpleTagList().toMutableList()
        forceCustomAI.set(forceCustomAI() && enableCustomAI())
        autoApplySavedTagsInCombat.set(autoApplySavedTagsInCombat() && enablePersistentModes())
        customAIFriendlyFireComplexity.set(max(0, min(2, customAIFriendlyFireComplexity())))
        MagicCombatButtonBase.enableHoverTooltips = enableTooltipsOnHover()
        MagicCombatButtonBase.enableHoverTooltipBoxes = enableTooltipBoxes()
        MagicCombatButtonBase.enableButtonHoverSound = enableButtonHoverSound()
        MagicCombatButtonBase.enableButtonHoverEffects = enableButtonHoverEffects()
        MagicCombatButtonBase.enableButtonOutlines = enableButtonOutlines()
        listOf(combatGuiHotkey, mergeHotkey, disableTagsHotkey, guiHotkey).filter { it() == 0 }.forEach { setting ->
            Global.getLogger(this.javaClass).error("Invalid hotkey was selected for ${setting.asString()}")
            setting.resetToDefault()
            setting.logError()
        }
        clearWeaponTagNameCaches()
        clearEditableWeaponTagDefinitionCaches()
        clearSuggestedTagCaches()
        clearWeaponTagListCaches()
    }

    fun hotAddTags(tags: List<String>, addForWholeSession: Boolean = false) {
        val hotTags = canonicalizeWeaponTagNames(tags.map { it.trim() }.filter { it.isNotEmpty() })
        if (hotTags.isEmpty()) return
        mapOf(
            classicTagList to originalClassicTagList,
            noviceTagList to originalNoviceTagList,
            completeTagList to originalCompleteTagList,
            simpleTagList to originalSimpleTagList
        ).forEach { (listSetting, originalList) ->
            val desiredTags = mergedHotTagList(originalList, hotTags)
            if (addForWholeSession && originalList != desiredTags) {
                originalList.clear()
                originalList.addAll(desiredTags)
                clearWeaponTagListCaches()
            }
            if (listSetting() != desiredTags) {
                listSetting.set(desiredTags)
                clearWeaponTagListCaches()
            }
        }
    }

    private fun mergedHotTagList(originalList: List<String>, hotTags: List<String>): List<String> {
        val merged = originalList.toMutableList()
        val seen = canonicalizeWeaponTagNames(originalList).toMutableSet()
        hotTags.forEach { tag ->
            if (seen.add(tag)) merged += tag
        }
        return merged
    }
}
