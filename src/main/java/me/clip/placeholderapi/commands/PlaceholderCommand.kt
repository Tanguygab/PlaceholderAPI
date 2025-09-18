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
import org.bukkit.command.CommandSender

abstract class PlaceholderCommand protected constructor(val label: String, vararg alias: String) {
    val alias = setOf(alias)
    var permission = "placeholderapi.$label"

    val labels = alias.toMutableList().also { it.add(label) }.toList()

    open fun evaluate(plugin: PlaceholderAPIPlugin, sender: CommandSender, alias: String, params: List<String>) {}
    open fun complete(plugin: PlaceholderAPIPlugin, sender: CommandSender, alias: String, params: List<String>, suggestions: List<String>) {}

    companion object {
        fun filterByPermission(
            sender: CommandSender,
            commands: Collection<PlaceholderCommand>
        ) = commands.filter { sender.hasPermission(it.permission) }

        fun suggestByParameter(possible: List<String>, suggestions: MutableList<String>, parameter: String?) {
            suggestions.addAll(possible.filter { parameter == null || it.startsWith(parameter, ignoreCase = true) })
        }
    }
}
