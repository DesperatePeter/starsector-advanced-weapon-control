package com.dp.advancedgunnerycontrol.settings

import com.dp.advancedgunnerycontrol.combatgui.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.dp.advancedgunnerycontrol.utils.StorageBase
import org.magiclib.util.MagicSettings
import kotlin.math.max
import kotlin.math.min

object Settings : SettingsDefinition() {
    private val tagList = addSetting<List<String>>("tagList", listOf())
    private val simpleTagList = addSetting<List<String>>("simpleTagList", listOf())
    val enableCustomAI = addSetting<Boolean>("enableCustomAI", true)
    val customAIRecursionLevel = addSetting<Int>("customAIRecursionLevel", 1)
    val forceCustomAI = addSetting<Boolean>("forceCustomAI", false)
    val customAITriggerHappiness = addSetting<Float>("customAITriggerHappiness", 1.2f)
    val customAIPerfectTargetLeading = addSetting<Boolean>("customAIAlwaysUsesBestTargetLeading", false)
    val customAIFriendlyFireCaution = addSetting<Float>("customAIFriendlyFireCaution", 1.0f)
    val customAIFriendlyFireComplexity = addSetting<Int>("customAIFriendlyFireAlgorithmComplexity", 1)
    val uiDisplayFrames = addSetting<Int>("messageDisplayDuration", 150)
    val uiMessagePositionX = addSetting<Float>("messagePositionX", 0.15f)
    val uiMessagePositionY = addSetting<Float>("messagePositionY", 0.25f)
    val uiAnchorX = addSetting<Float>("combatUiAnchorX", 0.1f)
    val uiAnchorY = addSetting<Float>("combatUiAnchorY", 0.7f)
    val infoHotkey = addSetting<Char>("inCombatGuiHotkey", 'j')
    val guiHotkey = addSetting<Char>("GUIHotkey", 'j')
    val mergeHotkey = addSetting<Char>("mergeHotkey", 'k')
    val enablePersistentModes = addSetting<Boolean>("enablePersistentFireModes", true)
    val enableCombatChangePersistance = addSetting<Boolean>("persistChangesInCombat", true)
    val enableAutoSaveLoad = addSetting<Boolean>("enableAutoSaveLoad", true)
    val maxLoadouts = addSetting<Int>("maxLoadouts", 3)
    val loadoutNames = addSetting<List<String>>("loadoutNames", listOf())
    val allowHotLoadingTags = addSetting<Boolean>("allowHotLoadingTags", true)
    private var originalTagList: List<String> = listOf()
    private var originalSimpleTagList: List<String> = listOf()
    val automaticallyReapplyPlayerShipModes = addSetting<Boolean>("automaticallyReapplyPlayerShipModes", true)
    val allowEnemyShipModeApplication = addSetting<Boolean>("allowEnemyShipModeApplication", true)
    val collisionRadiusMultiplier = addSetting<Float>("collisionRadiusMultiplier", 0.8f)


    // mode/suffix params
    val opportunistKineticThreshold = addSetting<Float>("opportunist_kineticThreshold", 0.5f)
    val opportunistHEThreshold = addSetting<Float>("opportunist_HEThreshold", 0.15f)
    val ventFluxThreshold = addSetting<Float>("vent_flux", 0.75f)
    val aggressiveVentFluxThreshold = addSetting<Float>("aggressiveVent_flux", 0.25f)
    val ventSafetyFactor = addSetting<Float>("vent_safetyFactor", 2f)
    val aggressiveVentSafetyFactor = addSetting<Float>("aggressiveVent_safetyFactor", 0.25f)
    val retreatHullThreshold = addSetting<Float>("retreat_hull", 0.5f)
    val shieldsOffThreshold = addSetting<Float>("shieldsOff_flux", 0.5f)
    val conserveAmmo = addSetting<Float>("conserveAmmo_ammo", 0.5f)
    val conservePDAmmo = addSetting<Float>("conservePDAmmo_ammo", 0.9f)
    val directRetreat = addSetting<Boolean>("retreat_shouldDirectRetreat", false)
    val opportunistModifier = addSetting<Float>("opportunist_triggerHappinessModifier", 1.0f)
    val strictBigSmall = addSetting<Boolean>("strictBigSmallShipMode", true)
    val targetShieldsThreshold = addSetting<Float>("targetShields_threshold", 0.2f)
    val avoidShieldsThreshold = addSetting<Float>("avoidShields_threshold", 0.5f)
    val ignoreFighterShields = addSetting<Boolean>("ignoreFighterShields", false)
    val targetShieldsAtFT = addSetting<Float>("targetShieldsAtFT_flux", 0.2f)
    val avoidShieldsAtFT = addSetting<Float>("avoidShieldsAtFT_flux", 0.2f)
    val prioXModifier = addSetting<Float>("prioXModifier", 10f)

    val enableWeaponHighlighting = addSetting<Boolean>("enableWeaponHighlighting", true)
    val enableTooltipsOnHover = addSetting<Boolean>("enableHoverTooltips", true)
    val enableTooltipBoxes = addSetting<Boolean>("enableHoverTooltipBoxes", true)
    val enableButtonHoverSound = addSetting<Boolean>("enableButtonHoverSound", true)
    val enableButtonHoverEffects = addSetting<Boolean>("enableButtonHoverEffects", true)
    val enableButtonOutlines = addSetting<Boolean>("enableButtonOutlines", true)

    var isAdvancedMode : Boolean by CampaignSettingDelegate("$" + Values.THIS_MOD_NAME + "isAdvancedMode", false)
    var autoApplySuggestedTags : Boolean by CampaignSettingDelegate("$" + Values.THIS_MOD_NAME + "autoApplySuggestedTags", false)
    var customSuggestedTags: Map<String, List<String>> by CampaignSettingDelegate("$" + Values.THIS_MOD_NAME + "customSuggestedTags", mapOf())

    var weaponBlacklist = listOf<String>()
        private set

    var defaultSuggestedTags = mapOf<String, List<String>>()
        private set

    fun getCurrentSuggestedTags() : Map<String, List<String>>{
        if(customSuggestedTags.isEmpty()) return defaultSuggestedTags
        return customSuggestedTags
    }

    var shipModeStorage: List<StorageBase<String>> = listOf()
    var tagStorage: List<StorageBase<List<String>>> = listOf()

    fun getCurrentWeaponTagList() : List<String>{
        if(isAdvancedMode){
            return tagList()
        }
        return simpleTagList()
    }

    override fun readSettings() {
        super.readSettings()
        weaponBlacklist = MagicSettings.getList(Values.THIS_MOD_NAME, Values.WEAPON_BLACKLIST_KEY)
        defaultSuggestedTags = MagicSettings.getStringMap(Values.THIS_MOD_NAME, Values.SUGGESTED_TAGS_KEY)
            .mapValues { it.value.split(",") }
    }

    override fun applySettings() {
        super.applySettings()
        shipModeStorage = StorageBase.assembleStorageArray<String>("$" + Values.THIS_MOD_NAME + "shipModes")
        tagStorage = StorageBase.assembleStorageArray("$" + Values.THIS_MOD_NAME + "tags")
        originalTagList = tagList()
        originalSimpleTagList = simpleTagList()
        forceCustomAI.set(forceCustomAI() && enableCustomAI())
        enableAutoSaveLoad.set(enableAutoSaveLoad() && enablePersistentModes())
        customAIFriendlyFireComplexity.set(max(0, min(2, customAIFriendlyFireComplexity())))
        ButtonBase.enableHoverTooltips = enableTooltipsOnHover()
        ButtonBase.enableHoverTooltipBoxes = enableTooltipBoxes()
        ButtonBase.enableButtonHoverSound = enableButtonHoverSound()
        ButtonBase.enableButtonHoverEffects = enableButtonHoverEffects()
        ButtonBase.enableButtonOutlines = enableButtonOutlines()
    }

    fun hotAddTags(tags: List<String>, addForWholeSession: Boolean = false) {
        if (!allowHotLoadingTags()) return
        tagList.set(originalTagList)
        simpleTagList.set(originalSimpleTagList)
        val combined = tagList().toMutableSet()
        combined.addAll(tags)
        if (addForWholeSession) originalTagList = combined.toList()
        tagList.set(combined.toList())
        val combinedSimple = simpleTagList().toMutableSet()
        combinedSimple.addAll(tags)
        simpleTagList.set(combinedSimple.toList())
    }
}