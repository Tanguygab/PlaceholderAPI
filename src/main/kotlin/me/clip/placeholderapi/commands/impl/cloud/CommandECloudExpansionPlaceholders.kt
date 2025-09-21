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

class CommandECloudExpansionPlaceholders : PlaceholderCommand("placeholders") {
    override fun evaluate(
        plugin: PlaceholderAPIPlugin,
        sender: CommandSender, alias: String,
        params: List<String>
    ) {
        if (params.isEmpty()) {
            Msg.msg(sender, "&cYou must specify the name of the expansion.")
            return
        }

        val expansion = plugin.cloudExpansionManager.findCloudExpansionByName(params[0])
        if (expansion == null) {
            Msg.msg(sender, "&cThere is no expansion with the name: &f" + params[0])
            return
        }

        val placeholders = expansion.placeholders
        if (placeholders.isNullOrEmpty()) {
            Msg.msg(sender, "&cThe expansion specified does not have placeholders listed.")
            return
        }

        val partitions = placeholders.sorted().chunked(10)

        Msg.msg(sender, "&6" + placeholders.size + "&7 placeholders: &a", partitions.joinToString("\n") { it.joinToString(", ") })
    }

    override fun complete(
        plugin: PlaceholderAPIPlugin,
        sender: CommandSender, alias: String,
        params: List<String>, suggestions: MutableList<String>
    ) {
        if (params.size > 1) return

        val names = plugin.cloudExpansionManager
            .getCloudExpansions()
            .values
            .map { it.name.replace(' ', '_') }

        suggestByParameter(names, suggestions, if (params.isEmpty()) null else params.get(0))
    }
}
