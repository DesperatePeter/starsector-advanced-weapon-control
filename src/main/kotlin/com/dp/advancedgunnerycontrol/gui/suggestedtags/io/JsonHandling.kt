package com.dp.advancedgunnerycontrol.gui.suggestedtags.io

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*


import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.io.clearJsonMapFile
import com.dp.advancedgunnerycontrol.io.readJsonMapFromFile
import com.dp.advancedgunnerycontrol.io.saveJsonMapAsFile

fun backupSuggestedTagsToJson(){
    saveJsonMapAsFile(Values.CUSTOM_SUGGESTED_TAG_JSON_FILE_NAME, Settings.getCurrentSuggestedTags())
}

fun restoreSuggestedTagsFromJson(){
    Settings.customSuggestedTags = emptyMap()
    Settings.customSuggestedTags = readJsonMapFromFile(Values.CUSTOM_SUGGESTED_TAG_JSON_FILE_NAME)
}

fun clearSuggestedTagsToJson(){
    clearJsonMapFile(Values.CUSTOM_SUGGESTED_TAG_JSON_FILE_NAME)
}
