package com.dp.advancedgunnerycontrol.settings


import com.dp.advancedgunnerycontrol.typesandvalues.FMValues
import com.dp.advancedgunnerycontrol.typesandvalues.FireMode
import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.dp.advancedgunnerycontrol.utils.StorageBase
import com.fs.starfarer.api.Global
import data.scripts.util.MagicSettings
import kotlin.math.max
import kotlin.math.min

object Settings : SettingsDefinition() {
    private val cycleOrderStrings = addSetting<List<String>>("cycleOrder", listOf("PD", "Fighters", "Missiles", "NoFighters"))
    private var cycleOrderInternal = listOf(FireMode.DEFAULT)
    fun cycleOrder() : List<FireMode> = cycleOrderInternal
    val enableCustomAI = addSetting<Boolean>("enableCustomAI", true)
    val customAIRecursionLevel = addSetting<Int>("customAIRecursionLevel", 1)
    val forceCustomAI = addSetting<Boolean>("forceCustomAI", false)
    val customAITriggerHappiness = addSetting<Float>("customAITriggerHappiness", 1.2f)
    val customAIPerfectTargetLeading = addSetting<Boolean>("customAIAlwaysUsesBestTargetLeading", false)
    val customAIFriendlyFireCaution = addSetting<Float>("customAIFriendlyFireCaution", 1.0f)
    val customAIFriendlyFireComplexity = addSetting<Int>("customAIFriendlyFireAlgorithmComplexity", 1)
    val uiDisplayFrames = addSetting<Int>("messageDisplayDuration", 150)
    val uiPositionX = addSetting<Int>("messagePositionX", 900)
    val uiPositionY = addSetting<Int>("messagePositionY", 150)
    val uiForceFullInfo = addSetting<Boolean>("alwaysShowFullInfo", false)
    val infoHotkey = addSetting<Char>("saveLoadInfoHotkey", 'j')
    val resetHotkey = addSetting<Char>("resetHotkey", '/')
    val loadHotkey = addSetting<Char>("loadAllShipsHotkey", '*')
    val suffixHotkey = addSetting<Char>("suffixHotkey", '-')
    val guiHoteky = addSetting<Char>("GUIHotkey", 'j')
    val enablePersistentModes = addSetting<Boolean>("enablePersistentFireModes", true)
    val enableAutoSaveLoad = addSetting<Boolean>("enableAutoSaveLoad", true)
    val skipInvalidModes = addSetting<Boolean>("skipInvalidModes", true)
    val enableTextInterface = addSetting<Boolean>("enableGUI", true)
    val cycleLoadout = addSetting<Char>("cycleLoadoutHotkey", '+')
    val maxLoadouts = addSetting<Int>("maxLoadouts", 3)
    val loadoutNames = addSetting<List<String>> ("loadoutNames", listOf())


    var weaponBlacklist = listOf<String>()
        private set

    var shipModeStorage : List<StorageBase<String>> = listOf()
    var suffixStorage : List<StorageBase<String>> = listOf()
    var fireModeStorage : List<StorageBase<String>> = listOf()

    override fun readSettings() {
        super.readSettings()
        weaponBlacklist = MagicSettings.getList(Values.THIS_MOD_NAME, Values.WEAPON_BLACKLIST_KEY)
    }

    override fun applySettings() {
        super.applySettings()
        shipModeStorage =  StorageBase.assembleStorageArray<String>("$" + Values.THIS_MOD_NAME + "shipModes")
        suffixStorage = StorageBase.assembleStorageArray<String>("$" + Values.THIS_MOD_NAME + "suffixes")
        fireModeStorage = StorageBase.assembleStorageArray<String>("$" + Values.THIS_MOD_NAME + "weaponModes")
        forceCustomAI.set(forceCustomAI() && enableCustomAI())
        enableAutoSaveLoad.set(enableAutoSaveLoad() && enablePersistentModes())
        customAIFriendlyFireComplexity.set ( max(0, min(2, customAIFriendlyFireComplexity())))
        cycleOrderStrings.set(listOf("Default") + cycleOrderStrings())
        cycleOrderInternal = cycleOrderStrings().mapNotNull { FMValues.FIRE_MODE_TRANSLATIONS[it] }
        if(cycleOrderInternal.size != cycleOrderStrings().size){
            // update lest we print the original values on printSettings call
            cycleOrderStrings.set(cycleOrderInternal.mapNotNull { FMValues.FIRE_MODE_DESCRIPTIONS[it] })
            Global.getLogger(this.javaClass).warn(
                "Error when loading cycle order. Double-check that only permitted strings are used. Fallback to default")
        }
    }
}