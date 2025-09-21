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
package me.clip.placeholderapi.commands.impl.local

import me.clip.placeholderapi.PlaceholderAPI.getRegisteredIdentifiers
import me.clip.placeholderapi.PlaceholderAPIPlugin
import me.clip.placeholderapi.commands.PlaceholderCommand
import me.clip.placeholderapi.util.Msg
import org.bukkit.command.CommandSender

class CommandInfo : PlaceholderCommand("info") {
    override fun evaluate(
        plugin: PlaceholderAPIPlugin,
        sender: CommandSender, alias: String,
        params: List<String>
    ) {
        if (params.isEmpty()) {
            Msg.msg(sender, "&cYou must specify the name of the expansion.")
            return
        }

        val expansion = plugin.localExpansionManager.findExpansionByIdentifier(params[0])
        if (expansion == null) {
            Msg.msg(sender, "&cThere is no expansion loaded with the identifier: &f" + params[0])
            return
        }

        val builder = StringBuilder()

        builder.append("&7Placeholder expansion info for: &r")
            .append(expansion.name)
            .append('\n')
            .append("&7Status: &r")
            .append(if (expansion.isRegistered()) "&aRegistered" else "7cNotRegistered")
            .append('\n')
            .append("&7Author: &r${expansion.author}\n")
            .append("&7Version: &r${expansion.version}\n")

        val requiredPlugin = expansion.requiredPlugin
        if (requiredPlugin != null) builder.append("&7Requires plugin: &r$requiredPlugin\n")

        val placeholders = expansion.placeholders
        if (placeholders.isNotEmpty()) {
            builder.append("&8&m-- &7Placeholders &8&m--&r\n")

            for (placeholder in placeholders) {
                builder.append(placeholder).append('\n')
            }
        }

        Msg.msg(sender, builder.toString())
    }

    override fun complete(
        plugin: PlaceholderAPIPlugin,
        sender: CommandSender, alias: String,
        params: List<String>, suggestions: MutableList<String>
    ) {
        if (params.size > 1) return

        suggestByParameter(
            getRegisteredIdentifiers(), suggestions,
            if (params.isEmpty()) null else params[0]
        )
    }
}
