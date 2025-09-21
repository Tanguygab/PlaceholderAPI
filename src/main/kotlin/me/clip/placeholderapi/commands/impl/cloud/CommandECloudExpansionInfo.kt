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

class CommandECloudExpansionInfo : PlaceholderCommand("info") {
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

        val builder = StringBuilder()

        builder.append("&bExpansion: &f")
            .append(if (expansion.shouldUpdate) "&e" else "&a")
            .append(expansion.name)
            .append('\n')
            .append("&bAuthor: &f")
            .append(expansion.author)
            .append('\n')
            .append("&bVerified: ")
            .append(if (expansion.isVerified) "&a&l✔" else "&c&l❌")
            .append('\n')

        if (params.size < 2) {
            builder.append("&bLatest Version: &f")
                .append(expansion.latestVersion)
                .append('\n')
                .append("&bReleased: &f")
                .append(expansion.getTimeSinceLastUpdate())
                .append(" ago")
                .append('\n')
                .append("&bRelease Notes: &f")
                .append(expansion.getVersion()!!.releaseNotes)
                .append('\n')
        } else {
            val version = expansion.getVersion(params[1])
            if (version == null) {
                Msg.msg(sender, "&cCould not find specified version: &f" + params[1], "&aVersions: &f" + expansion.getAvailableVersions())
                return
            }

            builder.append("&bVersion: &f")
                .append(version.version)
                .append('\n')
                .append("&bRelease Notes: &f")
                .append(version.releaseNotes)
                .append('\n')
                .append("&bDownload URL: &f")
                .append(version.url)
                .append('\n')
        }

        Msg.msg(sender, builder.toString())
    }

    override fun complete(
        plugin: PlaceholderAPIPlugin,
        sender: CommandSender, alias: String,
        params: List<String>, suggestions: MutableList<String>
    ) {
        if (params.size > 2) return

        if (params.size <= 1) {
            val names = plugin.cloudExpansionManager.getCloudExpansions().values.map{ it.name.replace(' ', '_') }
            suggestByParameter(names, suggestions, if (params.isEmpty()) null else params[0])
            return
        }

        val expansion = plugin.cloudExpansionManager.findCloudExpansionByName(params[0]) ?: return
        suggestByParameter(expansion.getAvailableVersions(), suggestions, params[1])
    }
}
