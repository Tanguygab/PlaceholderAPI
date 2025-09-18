/*
 * This file is part of PlaceholderAPI
 *
 * PlaceholderAPI
 * Copyright (c) 2015 - 2024 PlaceholderAPI Team
 *
 * PlaceholderAPI free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlaceholderAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package me.clip.placeholderapi.commands

import me.clip.placeholderapi.PlaceholderAPIPlugin
import me.clip.placeholderapi.commands.impl.cloud.CommandECloud
import me.clip.placeholderapi.commands.impl.local.*
import me.clip.placeholderapi.util.Msg
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class PlaceholderCommandRouter(private val plugin: PlaceholderAPIPlugin) : CommandExecutor, TabCompleter {
    private val commands = COMMANDS.map { it.labels.map { label -> label to it } }.flatMap { it }.toMap()


    override fun onCommand(sender: CommandSender, command: Command, alias: String, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            val fallback = commands["version"]
            fallback?.evaluate(plugin, sender, "", listOf())

            return true
        }

        val search = args[0].lowercase()
        val target = commands[search]

        if (target == null) {
            Msg.msg(sender, "&cUnknown command &7$search")
            return true
        }

        val permission = target.permission
        if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
            Msg.msg(sender, "&cYou do not have permission to do this!")
            return true
        }

        target.evaluate(plugin, sender, search, args.slice(1 ..< args.size))

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        val suggestions = mutableListOf()

        if (args.size > 1) {
            val target = commands[args[0].lowercase()]

            target?.complete(plugin, sender, args[0].lowercase(), args.slice(1 ..< args.size), suggestions)

            return suggestions
        }

        val targets = PlaceholderCommand
            .filterByPermission(sender, commands.values)
            .map { it.labels }
            .flatMap { it }
        PlaceholderCommand.suggestByParameter(targets, suggestions, if (args.isEmpty()) null else args[0])

        return suggestions
    }

    companion object {
        private val COMMANDS = listOf(
            CommandHelp(),
            CommandInfo(),
            CommandList(),
            CommandDump(),
            CommandECloud(),
            CommandParse(),
            CommandReload(),
            CommandVersion(),
            CommandExpansionRegister(),
            CommandExpansionUnregister()
        )
    }
}
