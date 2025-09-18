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
package me.clip.placeholderapi.expansion.manager

import com.google.common.io.Resources
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import me.clip.placeholderapi.PlaceholderAPIPlugin
import me.clip.placeholderapi.expansion.cloud.CloudExpansion
import me.clip.placeholderapi.util.Msg
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.nio.channels.Channels
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.logging.Level

class CloudExpansionManager(val plugin: PlaceholderAPIPlugin) {
    private val cache = mutableMapOf<String, CloudExpansion>()
    private val await = ConcurrentHashMap<String, CompletableFuture<File>>()

    private val ASYNC_EXECUTOR = Executors.newCachedThreadPool(ThreadFactoryBuilder().setNameFormat("placeholderapi-io-#%1\$d").build())

    fun load() {
        clean()
        fetch()
    }

    fun kill() = clean()

    fun getCloudExpansions() = cache.toMap()

    fun getCloudExpansionsInstalled(): Map<String, CloudExpansion> {
        return if (cache.isEmpty()) mapOf()
        else INDEXED_NAME_COLLECTOR(cache.values.filter(CloudExpansion::hasExpansion))
    }

    fun getCloudExpansionsByAuthor(author: String): Map<String, CloudExpansion> {
        return if (cache.isEmpty()) mapOf()
        else INDEXED_NAME_COLLECTOR(cache.values.filter { author.equals(it.author, ignoreCase = true) })
    }

    fun getCloudExpansionAuthors() = cache.values.map(CloudExpansion::author).toSet()
    fun getCloudExpansionAuthorCount() = getCloudExpansionAuthors().size

    fun getCloudUpdateCount() = plugin.localExpansionManager.expansions.count { findCloudExpansionByName(it.name)?.shouldUpdate == true }
    fun findCloudExpansionByName(name: String) = cache[toIndexName(name)]

    fun clean() {
        cache.clear()

        await.values.forEach { it.cancel(true) }
        await.clear()
    }

    fun fetch() {
        plugin.logger.info("Fetching available expansion information...")

        ASYNC_EXECUTOR.submit {
            // a defence tactic! use ConcurrentHashMap instead of normal HashMap
            val values = ConcurrentHashMap<String, CloudExpansion>()
            try {
                val json = Resources.toString(URL(API_URL), StandardCharsets.UTF_8)
                values.putAll(GSON.fromJson(json, TYPE))

                val toRemove = mutableListOf<String>()

                for (entry in values.entries) {
                    val expansion = entry.value
                    if (expansion.latestVersion == null || expansion.getVersion(expansion.latestVersion) == null) {
                        toRemove.add(entry.key)
                    }
                }

                for (name in toRemove) values.remove(name)
            } catch (e: Throwable) {
                // ugly swallowing of every throwable, but we have to be defensive
                plugin.logger.log(Level.WARNING, "Failed to download expansion information", e)
            }

            // loop through what's left on the main thread
            plugin.scheduler.runTask{
                try {
                    for (entry in values.entries) {
                        val name = entry.key
                        val expansion = entry.value

                        expansion.name = name

                        val local = plugin.localExpansionManager.findExpansionByName(name)
                        if (local != null && local.registered) {
                            expansion.hasExpansion = true
                            expansion.shouldUpdate = !local.version.equals(expansion.latestVersion, ignoreCase = true)
                        }

                        cache.put(toIndexName(expansion), expansion)
                    }
                } catch (e: Throwable) {
                    // ugly swallowing of every throwable, but we have to be defensive
                    plugin.logger.log(Level.WARNING, "Failed to download expansion information", e)
                }
            }
        }
    }

    fun isDownloading(expansion: CloudExpansion) = await.containsKey(toIndexName(expansion))

    fun downloadExpansion(
        expansion: CloudExpansion,
        version: CloudExpansion.Version
    ): CompletableFuture<File> {
        val previous = await[toIndexName(expansion)]
        if (previous != null) return previous

        val file = File(plugin.localExpansionManager.expansionsFolder, "Expansion-" + toIndexName(expansion) + ".jar")

        val download = CompletableFuture.supplyAsync({
            try {
                Channels.newChannel(URL(version.url).openStream()).use { source ->
                    FileOutputStream(file).use { target ->
                        target.channel.transferFrom(source, 0, Long.Companion.MAX_VALUE)
                    }
                }
            } catch (ex: IOException) {
                throw CompletionException(ex)
            }
            file
        }, ASYNC_EXECUTOR)

        download.whenCompleteAsync({ value: File?, exception: Throwable? ->
            await.remove(toIndexName(expansion))
            if (exception != null) {
                Msg.severe("Failed to download %s:%s", exception, expansion.name, expansion.getVersion())
            }
        }, ASYNC_EXECUTOR)

        await.put(toIndexName(expansion), download)

        return download
    }

    companion object {
        private const val API_URL = "http://api.extendedclip.com/v2/"
        private val INDEXED_NAME_COLLECTOR = { expansions: List<CloudExpansion> -> expansions.associateBy { toIndexName(it) }}

        private val GSON = Gson()
        private val TYPE = object : TypeToken<Map<String, CloudExpansion>>() {}.type

        private fun toIndexName(name: String) = name.lowercase().replace(' ', '_')

        private fun toIndexName(expansion: CloudExpansion) = toIndexName(expansion.name)
    }
}
