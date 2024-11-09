package com.dp.advancedgunnerycontrol.settings

import com.dp.advancedgunnerycontrol.typesandvalues.ShipModes
import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.dp.advancedgunnerycontrol.typesandvalues.shipModeFromString
import com.dp.advancedgunnerycontrol.utils.StorageBase
import com.dp.advancedgunnerycontrol.utils.StorageBaseIntKey
import com.dp.advancedgunnerycontrol.utils.TagStorageModes
import com.dp.advancedgunnerycontrol.utils.tagStorageModeFromStr
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
    private val listVariant = addSetting("listVariant", "novice")
    private val tagStorageModeInternal = addSetting("tagStorageMode", "Index")
    val tagStorageMode: TagStorageModes
        get() = tagStorageModeFromStr[tagStorageModeInternal()] ?: TagStorageModes.INDEX
    val shareTagsBetweenCampaigns = addSetting("shareGlobalTags", false)
    val enableCustomAI = addSetting<Boolean>("enableCustomAI", true)
    val customAIRecursionLevel = addSetting<Int>("customAIRecursionLevel", 1)
    val forceCustomAI = addSetting<Boolean>("forceCustomAI", false)
    val customAITriggerHappiness = addSetting<Float>("customAITriggerHappiness", 1.2f)
    val customAIPerfectTargetLeading = addSetting<Boolean>("customAIAlwaysUsesBestTargetLeading", false)
    val customAIFriendlyFireCaution = addSetting<Float>("customAIFriendlyFireCaution", 1.0f)
    val customAIFriendlyFireComplexity = addSetting<Int>("customAIFriendlyFireAlgorithmComplexity", 1, false)
    val uiDisplayFrames = addSetting<Int>("messageDisplayDuration", 150)
    val uiMessagePositionX = addSetting<Float>("messagePositionX", 0.15f)
    val uiMessagePositionY = addSetting<Float>("messagePositionY", 0.25f)
    val uiAnchorX = addSetting<Float>("combatUiAnchorX", 0.1f)
    val uiAnchorY = addSetting<Float>("combatUiAnchorY", 0.7f)
    val combatGuiHotkey = addSetting<Int>("inCombatGuiHotkey", Keyboard.KEY_J)
    val guiHotkey = addSetting<Int>("GUIHotkey", Keyboard.KEY_J)
    val mergeHotkey = addSetting<Int>("mergeHotkey", Keyboard.KEY_K)
    val enablePersistentModes = addSetting<Boolean>("enablePersistentFireModes", true)
    val enableCombatChangePersistence = addSetting<Boolean>("persistChangesInCombat", true)
    val enableAutoSaveLoad = addSetting<Boolean>("enableAutoSaveLoad", true)
    val maxLoadouts = addSetting<Int>("maxLoadouts", 3)
    val loadoutNames = addSetting<List<String>>("loadoutNames", listOf(), false)
    val allowHotLoadingTags = addSetting<Boolean>("allowHotLoadingTags", true)
    private var originalClassicTagList: MutableList<String> = mutableListOf()
    private var originalNoviceTagList: MutableList<String> = mutableListOf()
    private var originalCompleteTagList: MutableList<String> = mutableListOf()
    private var originalSimpleTagList: MutableList<String> = mutableListOf()
    val automaticallyReapplyPlayerShipModes = addSetting<Boolean>("automaticallyReapplyPlayerShipModes", true)
    val allowEnemyShipModeApplication = addSetting<Boolean>("allowEnemyShipModeApplication", true)
    val collisionRadiusMultiplier = addSetting<Float>("collisionRadiusMultiplier", 0.8f, false)
    val suppressHudWarning = addSetting("suppressHudWarning", false)


    // mode/suffix params
    val opportunistKineticThreshold = addSetting<Float>("opportunist_kineticThreshold", 0.5f, true)
    val opportunistHEThreshold = addSetting<Float>("opportunist_HEThreshold", 0.15f, true)
    val ventFluxThreshold = addSetting<Float>("vent_flux", 0.75f, true)
    val aggressiveVentFluxThreshold = addSetting<Float>("aggressiveVent_flux", 0.25f, true)
    val ventSafetyFactor = addSetting<Float>("vent_safetyFactor", 2f, true)
    val aggressiveVentSafetyFactor = addSetting<Float>("aggressiveVent_safetyFactor", 0.25f, true)
    val retreatHullThreshold = addSetting<Float>("retreat_hull", 0.5f, false)
    val shieldsOffThreshold = addSetting<Float>("shieldsOff_flux", 0.5f, false)
    val conserveAmmo = addSetting<Float>("conserveAmmo_ammo", 0.5f, false)
    val conservePDAmmo = addSetting<Float>("conservePDAmmo_ammo", 0.9f, false)
    val directRetreat = addSetting<Boolean>("retreat_shouldDirectRetreat", false, tryLunar = false)
    val opportunistModifier = addSetting<Float>("opportunist_triggerHappinessModifier", 1.0f, false)
    val strictBigSmall = addSetting<Boolean>("strictBigSmallShipMode", true, tryLunar = true)
    val targetShieldsThreshold = addSetting<Float>("targetShields_threshold", 0.2f, true)
    val avoidShieldsThreshold = addSetting<Float>("avoidShields_threshold", 0.5f, true)
    val ignoreFighterShields = addSetting<Boolean>("ignoreFighterShields", true)
    val targetShieldsAtFT = addSetting<Float>("targetShieldsAtFT_flux", 0.2f, false)
    val avoidShieldsAtFT = addSetting<Float>("avoidShieldsAtFT_flux", 0.2f, false)
    val prioXModifier = addSetting<Float>("prioXModifier", 10f, false)
    val useExactBoundsForFiringDecision = addSetting<Boolean>("useExactBoundsForFiringDecision", true)
    val useConeFFForSpreadOver = addSetting("useConeFFAboveSpread", 4f, true)

    val enableWeaponHighlighting = addSetting<Boolean>("enableWeaponHighlighting", true, tryLunar = false)
    val enableTooltipsOnHover = addSetting<Boolean>("enableHoverTooltips", true, tryLunar = false)
    val enableTooltipBoxes = addSetting<Boolean>("enableHoverTooltipBoxes", true, tryLunar = false)
    val enableButtonHoverSound = addSetting<Boolean>("enableButtonHoverSound", true, tryLunar = false)
    val enableButtonHoverEffects = addSetting<Boolean>("enableButtonHoverEffects", true, tryLunar = false)
    val enableButtonOutlines = addSetting<Boolean>("enableButtonOutlines", true, tryLunar = false)
    val enableRefitScreenIntegration = addSetting<Boolean>("enableRefitScreenIntegration", true)
    val showRefitScreenButton = addSetting<Boolean>("showRefitScreenButton", true)

    var isAdvancedMode : Boolean by CampaignSettingDelegate("isAdvancedMode",
        defaultValue = false,
        getFromLunaSettingsIfPossible = false
    )
    var autoApplySuggestedTags : Boolean by CampaignSettingDelegate("autoApplySuggestedTags", false)
    var customSuggestedTags: Map<String, List<String>> by CampaignSettingDelegate("customSuggestedTags", mapOf(), false)

    var weaponBlacklist = listOf<String>()
        private set

    var defaultSuggestedTags = mapOf<String, List<String>>()
        private set

    fun getCurrentSuggestedTags() : Map<String, List<String>>{
        if(customSuggestedTags.isEmpty()) return defaultSuggestedTags
        return customSuggestedTags
    }

    fun getCurrentShipModes(): List<ShipModes>{
        return shipModeList().mapNotNull { shipModeFromString[it] }
    }

    // why on earth did I decide that it was a good idea to use a map of int/string rather than a list of strings for ship modes?
    // All modes are stored in key 0, keys other than 0 are unused
    var shipModeStorage: List<StorageBaseIntKey<List<String>>> = listOf()
    var tagStorage: List<StorageBaseIntKey<List<String>>> = listOf()
    var tagStorageByWeaponComposition: List<StorageBase<String, List<String>>> = listOf()

    fun getCurrentWeaponTagList() : List<String>{
        if(isAdvancedMode){
            return when(listVariant()){
                "classic" -> classicTagList()
                "novice" -> noviceTagList()
                "complete" -> completeTagList()
                else -> noviceTagList()
            }

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
        shipModeStorage = StorageBaseIntKey.assembleStorageArray("$" + Values.THIS_MOD_NAME + "shipModes")
        tagStorage = StorageBaseIntKey.assembleStorageArray("$" + Values.THIS_MOD_NAME + "tags")
        tagStorageByWeaponComposition = StorageBase.assembleStorageArray("$" + Values.THIS_MOD_NAME + "tagsAlt")
        originalClassicTagList = classicTagList().toMutableList()
        originalNoviceTagList = noviceTagList().toMutableList()
        originalCompleteTagList = completeTagList().toMutableList()
        originalSimpleTagList = simpleTagList().toMutableList()
        forceCustomAI.set(forceCustomAI() && enableCustomAI())
        enableAutoSaveLoad.set(enableAutoSaveLoad() && enablePersistentModes())
        customAIFriendlyFireComplexity.set(max(0, min(2, customAIFriendlyFireComplexity())))
        MagicCombatButtonBase.enableHoverTooltips = enableTooltipsOnHover()
        MagicCombatButtonBase.enableHoverTooltipBoxes = enableTooltipBoxes()
        MagicCombatButtonBase.enableButtonHoverSound = enableButtonHoverSound()
        MagicCombatButtonBase.enableButtonHoverEffects = enableButtonHoverEffects()
        MagicCombatButtonBase.enableButtonOutlines = enableButtonOutlines()
        listOf(combatGuiHotkey, mergeHotkey, guiHotkey).filter { it() == 0 }.forEach { setting ->
            Global.getLogger(this.javaClass).error("Invalid hotkey was selected for ${setting.asString()}")
            setting.resetToDefault()
            setting.logError()
        }
    }

    fun hotAddTags(tags: List<String>, addForWholeSession: Boolean = false) {
        if (!allowHotLoadingTags()) return
        mapOf(
            classicTagList to originalClassicTagList,
            noviceTagList to originalNoviceTagList,
            completeTagList to originalCompleteTagList,
            simpleTagList to originalSimpleTagList
        ).forEach { (listSetting, originalList) ->
            listSetting.set(originalList)
            val combined = listSetting().toMutableSet()
            combined.addAll(tags)
            if(addForWholeSession){
                originalList.clear()
                originalList.addAll(combined)
            }
            listSetting.set(combined.toList())
        }
    }
}