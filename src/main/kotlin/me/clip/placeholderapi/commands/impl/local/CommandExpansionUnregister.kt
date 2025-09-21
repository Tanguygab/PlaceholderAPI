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

class CommandExpansionUnregister : PlaceholderCommand("unregister") {
    override fun evaluate(
        plugin: PlaceholderAPIPlugin,
        sender: CommandSender, alias: String,
        params: List<String>
    ) {
        if (params.isEmpty()) {
            Msg.msg(sender, "&cYou must specify the name of the expansion.")
            return
        }

        val expansion = plugin.localExpansionManager.findExpansionByName(params[0])
        if (expansion == null) {
            Msg.msg(sender, "&cThere is no expansion loaded with the identifier: &f" + params[0])
            return
        }

        val message = if (!expansion.unregister()) "&cFailed to unregister expansion: &f" else "&aSuccessfully unregistered expansion: &f"

        Msg.msg(sender, message + expansion.name)
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
