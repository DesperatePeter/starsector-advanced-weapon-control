package com.dp.advancedgunnerycontrol.utils

import com.dp.advancedgunnerycontrol.settings.Settings
import org.lazywizard.console.BaseCommand

class AGCHotloadTags : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        val tagsToAdd = args.split(" ")
        if (tagsToAdd.isEmpty()) return BaseCommand.CommandResult.BAD_SYNTAX
        Settings.hotAddTags(tagsToAdd, true)
        return BaseCommand.CommandResult.SUCCESS
    }
}