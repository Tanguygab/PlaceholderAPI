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

import me.clip.placeholderapi.PlaceholderAPIPlugin
import me.clip.placeholderapi.commands.PlaceholderCommand
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.clip.placeholderapi.util.Futures
import me.clip.placeholderapi.util.Msg
import org.bukkit.command.CommandSender
import java.io.File
import java.util.logging.Level

class CommandExpansionRegister : PlaceholderCommand("register") {
    override fun evaluate(
        plugin: PlaceholderAPIPlugin,
        sender: CommandSender, alias: String,
        params: List<String>
    ) {
        if (params.isEmpty()) {
            Msg.msg(sender, "&cYou must specify the name of an expansion file.")
            return
        }

        val manager = plugin.localExpansionManager

        val file = File(manager.expansionsFolder, params[0])
        if (!file.exists() || file.getParentFile() != manager.expansionsFolder) {
            Msg.msg(sender, "&cThe file &f" + file.getName() + "&c doesn't exist!")
            return
        }

        Futures.onMainThread(plugin, manager.findExpansionInFile(file)) { clazz: Class<out PlaceholderExpansion>?, exception: Throwable? ->
            if (exception != null) {
                Msg.msg(sender, "&cFailed to find expansion in file: &f$file")

                plugin.logger.log(Level.WARNING, "failed to find expansion in file: $file", exception)
                return@onMainThread
            }
            if (clazz == null) {
                Msg.msg(sender, "&cNo expansion class found in file: &f$file")
                return@onMainThread
            }

            val expansion = manager.register(clazz)
            if (expansion == null) {
                Msg.msg(sender, "&cFailed to register expansion from &f" + params[0])
                return@onMainThread
            }
            Msg.msg(sender, "&aSuccessfully registered expansion: &f" + expansion.name)
        }
    }

    override fun complete(
        plugin: PlaceholderAPIPlugin,
        sender: CommandSender, alias: String,
        params: List<String>, suggestions: MutableList<String>
    ) {
        if (params.size > 1) return

        val fileNames = plugin.localExpansionManager.expansionsFolder.list { _: File, name: String -> name.endsWith(".jar") }
        if (fileNames == null || fileNames.size == 0) return

        suggestByParameter(
            fileNames.toList(), suggestions,
            if (params.isEmpty()) null else params[0]
        )
    }
}
