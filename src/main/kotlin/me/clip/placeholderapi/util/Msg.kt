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
package me.clip.placeholderapi.util

import me.clip.placeholderapi.PlaceholderAPIPlugin
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import java.util.logging.Level

object Msg {
    fun log(level: Level, msg: String, vararg args: Any?) {
        PlaceholderAPIPlugin.getInstance().logger.log(level, String.format(msg, *args))
    }

    fun info(msg: String, vararg args: Any?) = log(Level.INFO, msg, *args)

    fun warn(msg: String, vararg args: Any?) = log(Level.WARNING, msg, *args)

    fun warn(msg: String, throwable: Throwable, vararg args: Any?) {
        PlaceholderAPIPlugin.getInstance().logger.log(Level.WARNING, String.format(msg, *args), throwable)
    }

    fun severe(msg: String, vararg args: Any?) = log(Level.SEVERE, msg, *args)

    fun severe(msg: String, throwable: Throwable, vararg args: Any?) {
        PlaceholderAPIPlugin.getInstance().logger.log(Level.SEVERE, String.format(msg, *args), throwable)
    }

    fun msg(sender: CommandSender, vararg messages: String) {
        if (messages.isEmpty()) return
        sender.sendMessage(messages.joinToString("\n") { color(it) })
    }

    fun broadcast(vararg messages: String) {
        if (messages.isEmpty()) return
        Bukkit.broadcastMessage(messages.joinToString("\n") { color(it) })
    }

    fun color(text: String) = ChatColor.translateAlternateColorCodes('&', text)
}
