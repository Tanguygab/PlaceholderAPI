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
package me.clip.placeholderapi.commands.impl.cloud

import me.clip.placeholderapi.PlaceholderAPIPlugin
import me.clip.placeholderapi.commands.PlaceholderCommand
import me.clip.placeholderapi.util.Msg
import org.bukkit.command.CommandSender

class CommandECloud : PlaceholderCommand("ecloud") {
    private val commands = COMMANDS.map { it.labels.map { label -> label to it } }.flatMap { it }.toMap()

    override fun evaluate(
        plugin: PlaceholderAPIPlugin,
        sender: CommandSender, alias: String,
        params: List<String>
    ) {
        if (params.isEmpty()) {
            Msg.msg(
                sender,
                "&b&lPlaceholderAPI &8- &7eCloud Help Menu &8- ",
                " ",
                "&b/papi &fecloud status",
                "  &7&oView status of the eCloud",
                "&b/papi &fecloud list <all/{author}/installed> {page}",
                "  &7&oList all/author specific available expansions",
                "&b/papi &fecloud info <expansion name> {version}",
                "  &7&oView information about a specific expansion available on the eCloud",
                "&b/papi &fecloud placeholders <expansion name>",
                "  &7&oView placeholders for an expansion",
                "&b/papi &fecloud download <expansion name> {version}",
                "  &7&oDownload an expansion from the eCloud",
                "&b/papi &fecloud update <expansion name/all>",
                "  &7&oUpdate a specific/all installed expansions",
                "&b/papi &fecloud refresh",
                "  &7&oFetch the most up to date list of expansions available.",
                "&b/papi &fecloud clear",
                "  &7&oClear the expansion cloud cache."
            )

            return
        }

        val search = params[0].lowercase()
        val target = commands[search]

        if (target == null) {
            Msg.msg(sender, "&cUnknown command &7ecloud $search")
            return
        }

        val permission = target.permission
        if (!permission.isEmpty() && !sender.hasPermission(permission)) {
            Msg.msg(sender, "&cYou do not have permission to do this!")
            return
        }

        if (!plugin.placeholderAPIConfig.isCloudEnabled) {
            Msg.msg(sender, "&cThe eCloud Manager is not enabled! To enable it, set 'cloud_enabled' to true and reload the plugin.")
            return
        }

        target.evaluate(plugin, sender, search, params.subList(1, params.size))
    }

    override fun complete(
        plugin: PlaceholderAPIPlugin,
        sender: CommandSender, alias: String,
        params: List<String>, suggestions: MutableList<String>
    ) {
        if (params.size <= 1) {
            val targets = filterByPermission(sender, commands.values)
                .map { it.labels }
                .flatMap { it }
            suggestByParameter(targets, suggestions, if (params.isEmpty()) null else params[0])

            return  // send sub commands
        }

        val search = params[0].lowercase()
        val target = commands[search] ?: return

        target.complete(plugin, sender, search, params.subList(1, params.size), suggestions)
    }

    companion object {
        private val COMMANDS = listOf(
            CommandECloudClear(),
            CommandECloudStatus(),
            CommandECloudUpdate(),
            CommandECloudRefresh(),
            CommandECloudDownload(),
            CommandECloudExpansionInfo(),
            CommandECloudExpansionList(),
            CommandECloudExpansionPlaceholders()
        )

        init {
            COMMANDS.forEach { it.permission = "placeholderapi.ecloud." + it.label }
        }
    }
}
