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

class CommandECloudStatus : PlaceholderCommand("status") {
    override fun evaluate(plugin: PlaceholderAPIPlugin, sender: CommandSender, alias: String, params: List<String>) {
        val manager = plugin.cloudExpansionManager

        val updateCount: Int = manager.getCloudUpdateCount()
        val authorCount: Int = manager.getCloudExpansionAuthorCount()
        val expansionCount: Int = manager.getCloudExpansions().size

        var builder = "&bThere are &a$expansionCount&b expansions available on the eCloud.\n" +
                "&7A total of &f$authorCount&7 authors have contributed to the eCloud.\n"

        if (updateCount > 0) {
            builder += "&eYou have &f$updateCount&e expansion${if (updateCount > 1) "s" else ""} installed that ${if (updateCount > 1) "have" else "has"} an update available."
        }

        Msg.msg(sender, builder)
    }
}
