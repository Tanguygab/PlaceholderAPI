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
import me.clip.placeholderapi.configuration.ExpansionSort
import me.clip.placeholderapi.expansion.cloud.CloudExpansion
import me.clip.placeholderapi.util.Format
import me.clip.placeholderapi.util.Msg
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.ceil
import kotlin.math.min

class CommandECloudExpansionList : PlaceholderCommand("list") {
    override fun evaluate(
        plugin: PlaceholderAPIPlugin,
        sender: CommandSender, alias: String,
        params: List<String>
    ) {
        if (params.isEmpty()) {
            Msg.msg(sender, "&cYou must specify an option. [all, {author}, installed]")
            return
        }

        val installed = params[0].equals("installed", ignoreCase = true)
        val expansions = getExpansions(params[0], plugin).toMutableList()

        if (expansions.isEmpty()) {
            Msg.msg(sender, "&cNo expansions available to list.")
            return
        }

        expansions.sortWith(plugin.placeholderAPIConfig.getExpansionSort() ?: ExpansionSort.LATEST)

        if (sender !is Player && params.size < 2) {
            val builder = StringBuilder()

            addExpansionTitle(builder, params[0], -1)
            addExpansionTable(
                expansions,
                builder,
                1,
                if (installed) "&9Version" else "&9Latest Version",
                if (installed) EXPANSION_CURRENT_VERSION else EXPANSION_LATEST_VERSION
            )

            Msg.msg(sender, builder.toString())
            return
        }

        val page: Int

        if (params.size < 2) {
            page = 1
        } else {
            val parsed = params[1].toIntOrNull()
            if (parsed == null) {
                Msg.msg(sender, "&cPage number must be an integer.")
                return
            }

            val limit = ceil(expansions.size.toDouble() / PAGE_SIZE).toInt()

            if (parsed !in 1..limit) {
                Msg.msg(sender, "&cPage number must be in the range &8[&a1&7..&a$limit&8]")
                return
            }

            page = parsed
        }

        val builder = StringBuilder()
        val values = getPage(expansions, page - 1)

        addExpansionTitle(builder, params[0], page)

        if (sender !is Player) {
            addExpansionTable(
                values,
                builder,
                (page - 1) * PAGE_SIZE + 1,
                if (installed) "&9Version" else "&9Latest Version",
                if (installed) EXPANSION_CURRENT_VERSION else EXPANSION_LATEST_VERSION
            )

            Msg.msg(sender, builder.toString())

            return
        }

        Msg.msg(sender, builder.toString())

        val limit = ceil(expansions.size.toDouble() / PAGE_SIZE).toInt()

        val message = getMessage(values, page, limit, params[0])
        plugin.adventure.player(sender).sendMessage(message)
    }

    override fun complete(
        plugin: PlaceholderAPIPlugin,
        sender: CommandSender, alias: String,
        params: List<String>, suggestions: MutableList<String>
    ) {
        if (params.size > 2) return

        if (params.size <= 1) {
            val authors = plugin.cloudExpansionManager.getCloudExpansionAuthors().toMutableSet()
            authors.addAll(OPTIONS)
            suggestByParameter(authors, suggestions, if (params.isEmpty()) null else params[0])
            return
        }

        suggestByParameter(
            (1 .. ceil(getExpansions(params[0], plugin).size.toDouble() / PAGE_SIZE).toInt()).map { it.toString() },
            suggestions, params[1]
        )
    }

    companion object {
        private const val PAGE_SIZE = 10

        private val EXPANSION_NAME = { expansion: CloudExpansion ->
            (if (expansion.shouldUpdate) "&6" else if (expansion.hasExpansion) "&a" else "&7") + expansion.name
        }
        private val EXPANSION_AUTHOR = { expansion: CloudExpansion -> "&f" + expansion.author }
        private val EXPANSION_VERIFIED = { expansion: CloudExpansion -> if (expansion.isVerified) "&aY" else "&cN" }
        private val EXPANSION_LATEST_VERSION = { expansion: CloudExpansion -> "&f" + expansion.latestVersion }
        private val EXPANSION_CURRENT_VERSION = { expansion: CloudExpansion ->
                "&f" + (PlaceholderAPIPlugin.getInstance().localExpansionManager
                    .findExpansionByName(expansion.name)
                    ?.version ?: "Unknown")
        }

        private val OPTIONS: Set<String> = setOf("all", "installed")

        private fun getExpansions(target: String, plugin: PlaceholderAPIPlugin) = when (target.lowercase()) {
            "all" -> plugin.cloudExpansionManager.getCloudExpansions()
            "installed" -> plugin.cloudExpansionManager.getCloudExpansionsInstalled()
            else -> plugin.cloudExpansionManager.getCloudExpansionsByAuthor(target)
        }.values

        private fun getPage(expansions: List<CloudExpansion>, page: Int): List<CloudExpansion> {
            val head = page * PAGE_SIZE
            val tail = min(expansions.size, head + PAGE_SIZE)

            if (expansions.size < head) return listOf()
            return expansions.subList(head, tail)
        }

        fun addExpansionTitle(builder: StringBuilder, target: String, page: Int) {
            when (target.lowercase()) {
                "all" -> builder.append("&bAll Expansions")
                "installed" -> builder.append("&bInstalled Expansions")
                else -> builder.append("&bExpansions by &f$target")
            }

            if (page == -1) {
                builder.append('\n')
                return
            }

            builder.append(" &bPage&7: &a$page&r")
        }

        private fun getMessage(expansions: List<CloudExpansion>, page: Int, limit: Int, target: String): Component {
            val format = PlaceholderAPIPlugin.getDateFormat()

            val message = Component.text()

            for (index in expansions.indices) {
                val expansion = expansions[index]
                val line = Component.text()

                val expansionNumber = index + (page - 1) * PAGE_SIZE + 1
                line.append(Component.text("$expansionNumber. ", NamedTextColor.DARK_GRAY))

                val expansionColour = when {
                    expansion.shouldUpdate -> NamedTextColor.GOLD
                    expansion.hasExpansion -> NamedTextColor.GREEN
                    else -> NamedTextColor.GRAY
                }

                line.append(Component.text(expansion.name, expansionColour))

                line.clickEvent(ClickEvent.suggestCommand("/papi ecloud download " + expansion.name))

                val hoverText = Component.text("Click to download this expansion!", NamedTextColor.AQUA)
                    .append(Component.newline())
                    .append(Component.newline())
                    .append(Component.text("Author: ", NamedTextColor.AQUA))
                    .append(Component.text(expansion.author, NamedTextColor.WHITE))
                    .append(Component.newline())
                    .append(Component.text("Verified: ", NamedTextColor.AQUA)).append(
                        Component.text(
                            if (expansion.isVerified) "✔" else "❌",
                            if (expansion.isVerified) NamedTextColor.GREEN else NamedTextColor.RED,
                            TextDecoration.BOLD
                        )
                    )
                    .append(Component.newline())
                    .append(Component.text("Released: ", NamedTextColor.AQUA))
                    .append(Component.text(format.format(expansion.lastUpdate), NamedTextColor.WHITE))
                    .toBuilder()


                if (!expansion.description.isNullOrEmpty()) {
                    hoverText
                        .append(Component.newline())
                        .append(Component.newline())
                        .append(
                            Component.text(
                                expansion.description.replace("\r", "").trim(),
                                NamedTextColor.WHITE
                            )
                        )
                }

                line.hoverEvent(HoverEvent.showText(hoverText.build()))

                if (index != expansions.size - 1) {
                    line.append(Component.newline())
                }

                message.append(line.build())
            }

            if (limit > 1) {
                message.append(Component.newline())

                val left = Component.text("◀", if (page > 1) NamedTextColor.GRAY else NamedTextColor.DARK_GRAY).toBuilder()
                if (page > 1) left.clickEvent(ClickEvent.runCommand("/papi ecloud list " + target + " " + (page - 1)))

                val right = Component.text("▶", if (page < limit) NamedTextColor.GRAY else NamedTextColor.DARK_GRAY).toBuilder()
                if (page < limit) right.clickEvent(ClickEvent.runCommand("/papi ecloud list " + target + " " + (page + 1)))

                message.append(left, Component.text(" $page ", NamedTextColor.GREEN), right)
            }

            return message.build()
        }

        private fun addExpansionTable(
            expansions: List<CloudExpansion>,
            message: StringBuilder, startIndex: Int,
            versionTitle: String,
            versionFunction: (CloudExpansion) -> String
        ) {
            val functions = mutableMapOf<String, (CloudExpansion) -> String>()

            val counter = AtomicInteger(startIndex)
            functions["&f"] = { "&8" + counter.getAndIncrement() + "." }

            functions["&9Name"] = EXPANSION_NAME
            functions["&9Author"] = EXPANSION_AUTHOR
            functions["&9Verified"] = EXPANSION_VERIFIED
            functions[versionTitle] = versionFunction

            val rows = mutableListOf<List<String>>()

            rows.add(ArrayList<String>(functions.keys))

            for (expansion in expansions) {
                rows.add(functions.values.map { it(expansion) })
            }

            val table = Format.tablify(Format.Align.LEFT, rows)
            if (table.isEmpty()) return


            table.add(1, "&8" + "-".repeat(table[0].length - rows[0].size * 2))

            message.append(table.joinToString("\n"))
        }
    }
}
