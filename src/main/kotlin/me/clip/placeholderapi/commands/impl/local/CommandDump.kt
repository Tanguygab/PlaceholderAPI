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

import com.google.common.io.CharStreams
import com.google.gson.Gson
import com.google.gson.JsonObject
import me.clip.placeholderapi.PlaceholderAPIPlugin
import me.clip.placeholderapi.commands.PlaceholderCommand
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.clip.placeholderapi.util.Msg
import org.bukkit.command.CommandSender
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.logging.Level

class CommandDump : PlaceholderCommand("dump") {
    override fun evaluate(
        plugin: PlaceholderAPIPlugin,
        sender: CommandSender, alias: String,
        params: List<String>
    ) {
        postDump(makeDump(plugin)).whenComplete { key: String, exception: Throwable? ->
            if (exception != null) {
                plugin.logger.log(Level.WARNING, "failed to post dump details", exception)

                Msg.msg(
                    sender,
                    "&cFailed to post dump details, check console."
                )
                return@whenComplete
            }
            Msg.msg(
                sender,
                "&aSuccessfully posted dump: $URL$key"
            )
        }
    }

    private fun postDump(dump: String): CompletableFuture<String> {
        return CompletableFuture.supplyAsync {
            try {
                val connection = URL(URL + "documents").openConnection() as HttpURLConnection
                connection.apply {
                    setRequestMethod("POST")
                    setRequestProperty("Content-Type", "text/plain; charset=utf-8")
                    setDoOutput(true)

                    connect()

                    getOutputStream().use { it.write(dump.toByteArray(StandardCharsets.UTF_8)) }
                }
                connection.getInputStream().use { stream ->
                    val json = CharStreams.toString(InputStreamReader(stream, StandardCharsets.UTF_8))
                    return@supplyAsync gson.fromJson(json, JsonObject::class.java).get("key").asString
                }
            } catch (ex: IOException) {
                throw CompletionException(ex)
            }
        }
    }

    private fun makeDump(plugin: PlaceholderAPIPlugin): String {
        val builder = StringBuilder()

        builder.append("Generated: ")
            .append(DATE_FORMAT.format(Instant.now()))
            .append("\n\n")
            .append("PlaceholderAPI: ")
            .append(plugin.description.version)
            .append("\n\n")
            .append("Expansions Registered:\n")

        val expansions = plugin.localExpansionManager
            .expansions
            .sortedWith(
                Comparator.comparing(PlaceholderExpansion::identifier)
                .thenComparing(PlaceholderExpansion::author)
            )

        var size = expansions.maxOfOrNull { it.identifier.length } ?: 0

        for (expansion in expansions) {
            builder.append("  ")
                .append(String.format("%-" + size + "s", expansion.identifier))
                .append(" [Author: ")
                .append(expansion.author)
                .append(", Version: ")
                .append(expansion.version)
                .append("]\n")
        }

        builder.append("\nExpansions Directory:\n")

        val jars = plugin.localExpansionManager.expansionsFolder.list { _: File, name: String -> name.lowercase().endsWith(".jar") }

        builder.append(jars?.joinToString { " $it\n" } ?: "  ¨[Warning]: Could not load jar files from expansions folder.")

        builder.append('\n')

        builder.append("Server Info: ")
            .append(plugin.server.bukkitVersion)
            .append('/')
            .append(plugin.server.version)
            .append("\n")

        builder.append("Java Version: ")
            .append(System.getProperty("java.version"))
            .append("\n\n")

        builder.append("Plugin Info:\n")

        val plugins = plugin.server.pluginManager.plugins.sortedBy { it.name }

        size = plugins.maxOfOrNull { it.name.length } ?: 0

        for (other in plugins) {
            builder.append("  ")
                .append(String.format("%-" + size + "s", other.name))
                .append(" [Authors: [")
                .append(other.description.authors.joinToString(", "))
                .append("], Version: ")
                .append(other.description.version)
                .append("]\n")
        }

        return builder.toString()
    }

    companion object {
        private const val URL = "https://paste.helpch.at/"

        private val gson = Gson()

        private val DATE_FORMAT = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.LONG)
            .withLocale(Locale.US)
            .withZone(ZoneId.of("UTC"))
    }
}
