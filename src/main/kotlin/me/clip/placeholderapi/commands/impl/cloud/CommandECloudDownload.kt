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
import me.clip.placeholderapi.expansion.cloud.CloudExpansion
import me.clip.placeholderapi.util.Msg
import org.bukkit.command.CommandSender
import java.io.File

class CommandECloudDownload : PlaceholderCommand("download") {
    private fun isBlockedExpansion(name: String?): Boolean {
        val env = System.getenv("PAPI_BLOCKED_EXPANSIONS") ?: return false

        return env.split(",").any { it.equals(name, ignoreCase = true) }
    }

    override fun evaluate(
        plugin: PlaceholderAPIPlugin,
        sender: CommandSender, alias: String,
        params: List<String>
    ) {
        if (params.isEmpty()) {
            Msg.msg(sender, "&cYou must supply the name of an expansion.")
            return
        }

        if (isBlockedExpansion(params[0])) {
            Msg.msg(sender, "&cThis expansion can't be downloaded.")
            return
        }

        val expansion = plugin.cloudExpansionManager.findCloudExpansionByName(params[0])
        if (expansion == null) {
            Msg.msg(sender, "&cFailed to find an expansion named: &f" + params[0])
            return
        }

        if (!expansion.isVerified) {
            Msg.msg(sender, "&cThe expansion '&f" + params[0] + "&c' is not verified and can only be downloaded manually from &fhttps://placeholderapi.com/ecloud")
            return
        }

        val version: CloudExpansion.Version?
        if (params.size < 2) {
            version = expansion.getVersion(expansion.latestVersion!!)
            if (version == null) {
                Msg.msg(sender, "&cCould not find latest version for expansion.")
                return
            }
        } else {
            version = expansion.getVersion(params[1])
            if (version == null) {
                Msg.msg(sender, "&cCould not find specified version: &f" + params[1], "&7Available versions: &f" + expansion.getAvailableVersions())
                return
            }
        }

        plugin.cloudExpansionManager.downloadExpansion(expansion, version).whenComplete { file: File, exception: Throwable? ->
            if (exception != null) {
                Msg.msg(sender, "&cFailed to download expansion: &f" + exception.message)
                return@whenComplete
            }
            Msg.msg(
                sender,
                "&aSuccessfully downloaded expansion &f" + expansion.name + " [" + version.version + "] &ato file: &f" + file.getName(),
                "&aMake sure to type &f/papi reload &ato enable your new expansion!")
            plugin.cloudExpansionManager.load()
        }
    }

    override fun complete(
        plugin: PlaceholderAPIPlugin,
        sender: CommandSender, alias: String,
        params: List<String>, suggestions: MutableList<String>
    ) {
        if (params.size > 2) return

        if (params.size <= 1) {
            val names = plugin.cloudExpansionManager.getCloudExpansions().values
                .map(CloudExpansion::name)
                .map { it.replace(' ', '_') }
            suggestByParameter(names, suggestions, if (params.isEmpty()) null else params[0])
            return
        }

        val expansion = plugin.cloudExpansionManager.findCloudExpansionByName(params[0]) ?: return
        suggestByParameter(expansion.getAvailableVersions(), suggestions, params[1])
    }
}
