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
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.clip.placeholderapi.expansion.cloud.CloudExpansion
import me.clip.placeholderapi.util.Futures
import me.clip.placeholderapi.util.Msg
import org.bukkit.command.CommandSender

/**
 * please don't flame me for this code, I will fix this shit later.
 */
class CommandECloudUpdate : PlaceholderCommand("update") {
    override fun evaluate(
        plugin: PlaceholderAPIPlugin,
        sender: CommandSender, alias: String,
        params: List<String>
    ) {
        if (params.isEmpty()) {
            Msg.msg(
                sender,
                "&cYou must define 'all' or the name of an expansion to update."
            )
            return
        }

        val multiple = params[0].equals("all", ignoreCase = true)
        val expansions = mutableListOf<CloudExpansion>()

        // gather target expansions
        if (multiple) expansions.addAll(plugin.cloudExpansionManager.getCloudExpansionsInstalled().values)
        else plugin.cloudExpansionManager.findCloudExpansionByName(params[0])?.let { expansions.add(it) }

        // remove the ones that are the latest version
        expansions.removeIf { !it.shouldUpdate }

        if (expansions.isEmpty()) {
            Msg.msg(sender, "&cNo updates available for ${if (!multiple) "this expansion" else "your active expansions"}.")
            return
        }

        Msg.msg(sender, "&aUpdating expansions: " + expansions.joinToString("&7, &6", "&8[&6", "&8]&r") { it.name })

        Futures.onMainThread(plugin, downloadAndDiscover(expansions, plugin)!!) { classes: List<Class<out PlaceholderExpansion>?>, exception: Throwable? ->
            if (exception != null) {
                Msg.msg(sender, "&cFailed to update expansions: &e" + exception.message)
                return@onMainThread
            }
            Msg.msg(sender, "&aSuccessfully downloaded updates, registering new versions.")

            val message = classes
                .filterNotNull()
                .mapNotNull { plugin.localExpansionManager.register(it) }
                .joinToString("\n") { "  &a" + it.name + " &f" + it.version }
            Msg.msg(sender, "&7Registered expansions:", message)
        }
    }

    override fun complete(
        plugin: PlaceholderAPIPlugin,
        sender: CommandSender, alias: String,
        params: List<String>, suggestions: MutableList<String>
    ) {
        if (params.size > 1) return

        val installed = plugin.cloudExpansionManager.getCloudExpansionsInstalled().values.toMutableList()
        installed.removeIf { !it.shouldUpdate }

        if (!installed.isEmpty() && (params.isEmpty() || "all".startsWith(params[0].lowercase()))) {
            suggestions.add("all")
        }

        suggestByParameter(
            installed.map { it.name.replace(" ", "_") },
            suggestions, if (params.isEmpty()) null else params[0]
        )
    }

    companion object {
        private fun downloadAndDiscover(expansions: List<CloudExpansion>, plugin: PlaceholderAPIPlugin) = expansions
            .map { plugin.cloudExpansionManager.downloadExpansion(it, it.getVersion()!!) }
            .map { it.thenCompose{ file -> plugin.localExpansionManager.findExpansionInFile(file) } }
            .stream()
            .collect(Futures.collector())
    }
}
