package com.dp.advancedgunnerycontrol.settings

import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.dp.advancedgunnerycontrol.utils.StorageBase
import com.fs.starfarer.api.Global
import data.scripts.util.MagicSettings
import kotlin.math.max
import kotlin.math.min

object Settings : SettingsDefinition() {
    val tagList = addSetting<List<String>>("tagList",listOf("PD", "Fighter", "AvoidShields", "TargetShields", "NoFighters",
        "Hold(Flx>90%)", "Hold(Flx>75%)", "Hold(Flx>50%)", "ConserveAmmo", "Opportunist"))
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
    val enablePersistentModes = addSetting<Boolean>("enablePersistentFireModes", true)
    val enableCombatChangePersistance = addSetting<Boolean>("persistChangesInCombat", true)
    val enableAutoSaveLoad = addSetting<Boolean>("enableAutoSaveLoad", true)
    val maxLoadouts = addSetting<Int>("maxLoadouts", 3)
    val loadoutNames = addSetting<List<String>> ("loadoutNames", listOf())


    // mode/suffix params
    val opportunistKineticThreshold = addSetting<Float> ("opportunist_kineticThreshold", 0.5f)
    val opportunistHEThreshold = addSetting<Float> ("opportunist_HEThreshold", 0.15f)
    val ventFluxThreshold = addSetting<Float> ("vent_flux", 0.75f)
    val aggressiveVentFluxThreshold = addSetting<Float> ("aggressiveVent_flux", 0.25f)
    val ventSafetyFactor = addSetting<Float> ("vent_safetyFactor", 2f)
    val aggressiveVentSafetyFactor = addSetting<Float> ("aggressiveVent_safetyFactor", 0.25f)
    val retreatHullThreshold = addSetting<Float> ("retreat_hull", 0.5f)
    val shieldsOffThreshold = addSetting<Float> ("shieldsOff_flux", 0.5f)
    val conserveAmmo = addSetting<Float> ("conserveAmmo_ammo", 0.5f)
    val directRetreat = addSetting<Boolean> ("retreat_shouldDirectRetreat", false)
    val opportunistModifier = addSetting<Float> ("opportunist_triggerHappinessModifier", 1.0f)
    val strictBigSmall = addSetting<Boolean>("strictBigSmallShipMode", true)
    val targetShieldsThreshold = addSetting<Float> ("targetShields_threshold", 0.2f)
    val avoidShieldsThreshold = addSetting<Float> ("avoidShields_threshold", 0.5f)

    var weaponBlacklist = listOf<String>()
        private set

    var suggestedTags = mapOf<String, List<String>>()
        private set

    var shipModeStorage : List<StorageBase<String>> = listOf()
    var tagStorage : List<StorageBase<List<String>>> = listOf()

    override fun readSettings() {
        super.readSettings()
        weaponBlacklist = MagicSettings.getList(Values.THIS_MOD_NAME, Values.WEAPON_BLACKLIST_KEY)
        suggestedTags = MagicSettings.getStringMap(Values.THIS_MOD_NAME, Values.SUGGESTED_TAGS_KEY).mapValues { it.value.split(",") }
    }

    override fun applySettings() {
        super.applySettings()
        shipModeStorage =  StorageBase.assembleStorageArray<String>("$" + Values.THIS_MOD_NAME + "shipModes")
        tagStorage = StorageBase.assembleStorageArray("$" + Values.THIS_MOD_NAME + "tags")
        forceCustomAI.set(forceCustomAI() && enableCustomAI())
        enableAutoSaveLoad.set(enableAutoSaveLoad() && enablePersistentModes())
        customAIFriendlyFireComplexity.set ( max(0, min(2, customAIFriendlyFireComplexity())))
        uiAnchorX.set(uiAnchorX() / Global.getSettings().screenScaleMult)
        uiAnchorY.set(uiAnchorY() / Global.getSettings().screenScaleMult)
        uiMessagePositionX.set(uiMessagePositionX() / Global.getSettings().screenScaleMult)
        uiMessagePositionY.set(uiMessagePositionY() / Global.getSettings().screenScaleMult)
    }
}