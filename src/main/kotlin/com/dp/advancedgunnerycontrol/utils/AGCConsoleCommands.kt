package com.dp.advancedgunnerycontrol.utils

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.Values
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console

class AGCHotloadTags : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        val tagsToAdd = args.split(" ")
        if (tagsToAdd.isEmpty()) return BaseCommand.CommandResult.BAD_SYNTAX
        Settings.hotAddTags(tagsToAdd, true)
        return BaseCommand.CommandResult.SUCCESS
    }
}

fun parseArgs(args: String): Pair<String, Int>{
    val argsList = args.split(" ").filter { it != "" }
    var file = Values.WEAPON_COMP_GLOBAL_TAGS_JSON_FILE_NAME
    var loadout = Values.storageIndex
    if(argsList.isNotEmpty()){
        file = argsList[0]
        file = if(".json" in file) file else "$file.json"
    }
    if(argsList.size >= 2){
        try {
            loadout = argsList[1].toInt()
        }catch (e: NumberFormatException){
            Console.showMessage("Second argument must be an Integer. Assuming Loadout #$loadout")
        }
    }
    if(loadout < 0 || loadout >= Settings.maxLoadouts()){
        Console.showMessage("Second argument must be an Integer between 0 and ${Settings.maxLoadouts() - 1}. Using current loadout instead")
        loadout = Values.storageIndex
    }
    return Pair(file, loadout)
}

class AGCBackupWeaponCompGlobalTags: BaseCommand{
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if(Settings.tagStorageMode != TagStorageModes.WEAPON_COMPOSITION_GLOBAL){
            Console.showMessage("Operation only supported when storage mode is weapon composition global!")
            return BaseCommand.CommandResult.ERROR
        }
        val (file, loadout) = parseArgs(args)
        backupWeaponCompGlobalTagsToFile(file, loadout)
        Console.showMessage("Successfully saved loadout $loadout to $file")
        return BaseCommand.CommandResult.SUCCESS
    }
}

class AGCRestoreWeaponCompGlobalTags: BaseCommand{
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if(Settings.tagStorageMode != TagStorageModes.WEAPON_COMPOSITION_GLOBAL){
            Console.showMessage("Operation only supported when storage mode is weapon composition global!")
            return BaseCommand.CommandResult.ERROR
        }
        val (file, loadout) = parseArgs(args)
        val override = if(args.split(" ").size >= 3) args.split(" ")[2] == "override" else false
        restoreWeaponCompGlobalTagsFromFile(file, loadout, override)
        Console.showMessage("Successfully loaded loadout $loadout from $file")
        if(Settings.tagStorageByWeaponComposition[loadout].modesByShip["Global"]?.isEmpty() != false){
            Console.showMessage("Warning: Loaded empty tag set. Please make sure the given file exists.")
        }
        return BaseCommand.CommandResult.SUCCESS
    }
}