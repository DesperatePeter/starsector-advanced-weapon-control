package com.dp.advancedgunnerycontrol.presets

import com.dp.advancedgunnerycontrol.config.Values
import com.dp.advancedgunnerycontrol.persistence.agcPersistentDataOrNull
import com.dp.advancedgunnerycontrol.weapontags.canonicalizeWeaponTagNames
import com.fs.starfarer.api.Global
import org.json.JSONArray
import org.json.JSONObject
import org.lazywizard.lazylib.JSONUtils

private const val PRESET_SCHEMA_VERSION = 1
private data class WeaponCompositionPresetFileData(
    val schemaVersion: Int = PRESET_SCHEMA_VERSION,
    val presetsByLoadout: MutableMap<Int, MutableMap<String, List<String>>> = mutableMapOf()
)

internal interface WeaponPresetStore {
    fun get(loadoutIndex: Int, key: String): List<String>?
    fun put(loadoutIndex: Int, key: String, tags: List<String>)
}

private class ExternalWeaponPresetStore(private val file: String) : WeaponPresetStore {
    private val fileData: WeaponCompositionPresetFileData by lazy { loadPresetData(file) }

    override fun get(loadoutIndex: Int, key: String): List<String>? {
        return fileData.presetsByLoadout[loadoutIndex]?.get(key)
    }

    override fun put(loadoutIndex: Int, key: String, tags: List<String>) {
        val byLoadout = fileData.presetsByLoadout.getOrPut(loadoutIndex) { mutableMapOf() }
        byLoadout[key] = canonicalizeWeaponTagNames(tags)
        savePresetData(file, fileData)
    }
}

private class CampaignWeaponPresetStore : WeaponPresetStore {
    private val persistentKey = "$" + Values.THIS_MOD_NAME + Values.WEAPON_COMP_TAG_PRESETS_CAMPAIGN_KEY

    private fun data(): MutableMap<Int, MutableMap<String, List<String>>> {
        val persistentData = agcPersistentDataOrNull("campaign weapon preset store") ?: return mutableMapOf()
        val existing = validCampaignPresetDataOrNull(persistentData[persistentKey])
        if (existing != null) return existing
        if (persistentData.containsKey(persistentKey)) {
            logPresetWarn("Ignoring malformed campaign weapon preset data. Creating an empty preset store.")
        }
        val created = mutableMapOf<Int, MutableMap<String, List<String>>>()
        persistentData[persistentKey] = created
        return created
    }

    @Suppress("UNCHECKED_CAST")
    private fun validCampaignPresetDataOrNull(raw: Any?): MutableMap<Int, MutableMap<String, List<String>>>? {
        val map = raw as? MutableMap<*, *> ?: return null
        val valid = map.all { (loadoutIndex, presets) ->
            loadoutIndex is Int &&
                presets is MutableMap<*, *> &&
                presets.all { (weaponKey, tags) ->
                    weaponKey is String &&
                        tags is List<*> &&
                        tags.all { it is String }
                }
        }
        return if (valid) map as MutableMap<Int, MutableMap<String, List<String>>> else null
    }

    override fun get(loadoutIndex: Int, key: String): List<String>? {
        return data()[loadoutIndex]?.get(key)
    }

    override fun put(loadoutIndex: Int, key: String, tags: List<String>) {
        data().getOrPut(loadoutIndex) { mutableMapOf() }[key] = canonicalizeWeaponTagNames(tags)
    }
}

internal fun presetStore(backend: WeaponPresetBackend, file: String): WeaponPresetStore {
    return when (backend) {
        WeaponPresetBackend.CAMPAIGN -> CampaignWeaponPresetStore()
        WeaponPresetBackend.EXTERNAL -> ExternalWeaponPresetStore(file)
    }
}

private fun loadPresetData(file: String): WeaponCompositionPresetFileData {
    return try {
        val root = JSONUtils.loadCommonJSON(file)
        val schemaVersion = root.optInt("schemaVersion", PRESET_SCHEMA_VERSION)
        val loadoutsObject = root.optJSONObject("loadouts")
        val presetsByLoadout = mutableMapOf<Int, MutableMap<String, List<String>>>()
        if (loadoutsObject != null) {
            loadoutsObject.keys().forEach { loadoutKeyAny ->
                val loadoutKey = loadoutKeyAny as? String ?: return@forEach
                val loadoutIndex = loadoutKey.toIntOrNull() ?: return@forEach
                val perLoadoutObject = loadoutsObject.optJSONObject(loadoutKey) ?: return@forEach
                val perLoadout = mutableMapOf<String, List<String>>()
                perLoadoutObject.keys().forEach { weaponKeyAny ->
                    val weaponKey = weaponKeyAny as? String ?: return@forEach
                    val tagsJson = perLoadoutObject.optJSONArray(weaponKey)
                    if (tagsJson == null) {
                        logPresetWarn("Ignoring malformed preset entry for key=$weaponKey in loadout=$loadoutIndex (not an array).")
                        return@forEach
                    }
                    perLoadout[weaponKey] = jsonArrayToStringList(tagsJson)
                }
                if (perLoadout.isNotEmpty()) {
                    presetsByLoadout[loadoutIndex] = perLoadout
                }
            }
        }
        WeaponCompositionPresetFileData(schemaVersion = schemaVersion, presetsByLoadout = presetsByLoadout)
    } catch (ex: Exception) {
        logPresetWarn("Failed reading preset file '$file'. Falling back to empty presets.", ex)
        WeaponCompositionPresetFileData()
    }
}

private fun savePresetData(file: String, fileData: WeaponCompositionPresetFileData) {
    val root = JSONUtils.loadCommonJSON(file)
    clearJsonObject(root)
    root.put("schemaVersion", fileData.schemaVersion)
    val loadoutsObject = JSONObject()
    fileData.presetsByLoadout.forEach { (loadoutIndex, presets) ->
        val loadoutObject = JSONObject()
        presets.forEach { (weaponKey, tags) ->
            loadoutObject.put(weaponKey, JSONArray(canonicalizeWeaponTagNames(tags)))
        }
        loadoutsObject.put(loadoutIndex.toString(), loadoutObject)
    }
    root.put("loadouts", loadoutsObject)
    root.save()
}

private fun clearJsonObject(jsonObject: JSONObject) {
    val keys = mutableListOf<String>()
    jsonObject.keys().forEach { key ->
        (key as? String)?.let { keys.add(it) }
    }
    keys.forEach { jsonObject.remove(it) }
}

private fun jsonArrayToStringList(jsonArray: JSONArray): List<String> {
    val output = mutableListOf<String>()
    for (i in 0 until jsonArray.length()) {
        val value = jsonArray.opt(i) as? String ?: continue
        val trimmed = value.trim()
        if (trimmed.isNotEmpty()) output.add(trimmed)
    }
    return output
}

internal fun logPresetWarn(message: String, throwable: Throwable? = null) {
    val logger = Global.getLogger(WeaponCompositionPresetFileData::class.java)
    if (throwable == null) logger.warn(message) else logger.warn(message, throwable)
}
