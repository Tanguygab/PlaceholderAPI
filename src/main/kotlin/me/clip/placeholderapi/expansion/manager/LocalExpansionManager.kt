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

import me.clip.placeholderapi.PlaceholderAPIPlugin
import me.clip.placeholderapi.events.ExpansionRegisterEvent
import me.clip.placeholderapi.events.ExpansionUnregisterEvent
import me.clip.placeholderapi.events.ExpansionsLoadedEvent
import me.clip.placeholderapi.expansion.Cacheable
import me.clip.placeholderapi.expansion.Cleanable
import me.clip.placeholderapi.expansion.Configurable
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.clip.placeholderapi.expansion.Taskable
import me.clip.placeholderapi.expansion.VersionSpecific
import me.clip.placeholderapi.util.FileUtil
import me.clip.placeholderapi.util.Futures
import me.clip.placeholderapi.util.Msg
import java.io.File
import java.lang.reflect.Modifier
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import java.util.logging.Level

import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.PluginDisableEvent
import java.util.stream.Stream

class LocalExpansionManager(private val plugin: PlaceholderAPIPlugin) : Listener {
    val expansionsFolder = File(plugin.dataFolder, EXPANSIONS_FOLDER_NAME)

    private val expansionsMap = ConcurrentHashMap<String, PlaceholderExpansion>()
    private val expansionsLock = ReentrantLock()

    init {
        if (!expansionsFolder.exists() && !expansionsFolder.mkdirs()) {
            Msg.warn("Failed to create expansions folder!")
        }
    }

    fun load(sender: CommandSender) = registerAll(sender)

    fun kill() =unregisterAll()


    val identifiers: Set<String>
        get() {
            expansionsLock.lock()
            try {
                return expansionsMap.keys.toSet()
            } finally {
                expansionsLock.unlock()
            }
        }

    val expansions: Set<PlaceholderExpansion>
        get() {
            expansionsLock.lock()
            try {
            return expansionsMap.values.toSet()
            } finally {
                expansionsLock.unlock()
            }
        }

    fun getExpansion(identifier: String): PlaceholderExpansion? {
        expansionsLock.lock()
        try {
            return expansionsMap[identifier.lowercase()]
        } finally {
            expansionsLock.unlock()
        }
    }

    fun findExpansionByName(name: String) = expansions.find { it.name.equals(name, ignoreCase = true) }

    fun findExpansionByIdentifier(identifier: String) = getExpansion(identifier)

    fun register(clazz: Class<out PlaceholderExpansion>): PlaceholderExpansion? {
        val expansion: PlaceholderExpansion

        try {
            expansion = createExpansionInstance(clazz) ?: return null
        } catch (e: LinkageError) {
            Msg.severe("Failed to load expansion class %s%s", e, clazz.getSimpleName(), " (Is a dependency missing?)")
            return null
        }

        if (!expansion.requiredPlugin.isNullOrEmpty()) {
            if (!plugin.server.pluginManager.isPluginEnabled(expansion.requiredPlugin!!)) {
                Msg.warn(
                    "Cannot load expansion %s due to a missing plugin: %s", expansion.identifier,
                    expansion.requiredPlugin
                )
                return null
            }
        }

        expansion.expansionType = PlaceholderExpansion.Type.EXTERNAL

        if (!expansion.register()) {
            Msg.warn("Cannot load expansion %s due to an unknown issue.", expansion.identifier)
            return null
        }

        return expansion
    }

    /**
     * Attempt to register a [PlaceholderExpansion]
     * @param expansion the expansion to register
     * @return if the expansion was registered
     */
    internal fun register(expansion: PlaceholderExpansion): Boolean {
        val identifier = expansion.identifier.lowercase()

        if (!expansion.canRegister()) return false

        // Avoid loading two external expansions with the same identifier
        if (expansion.expansionType === PlaceholderExpansion.Type.EXTERNAL && expansionsMap.containsKey(identifier)) {
            Msg.warn("Failed to load external expansion %s. Identifier is already in use.", expansion.identifier)
            return false
        }

        if (expansion is Configurable) {
            val defaults = expansion.defaults
            val pre = "expansions.$identifier."
            val cfg = plugin.config
            var save = false

            for (entries in defaults.entries) {
                if (entries.key.isEmpty()) continue

                if (entries.value == null) {
                    if (cfg.contains(pre + entries.key)) {
                        save = true
                        cfg.set(pre + entries.key, null)
                    }
                } else {
                    if (!cfg.contains(pre + entries.key)) {
                        save = true
                        cfg.set(pre + entries.key, entries.value)
                    }
                }
            }

            if (save) {
                plugin.saveConfig()
                plugin.reloadConfig()
            }
        }

        if (expansion is VersionSpecific) {
            if (!expansion.isCompatibleWith(PlaceholderAPIPlugin.serverVersion)) {
                Msg.warn(
                    "Your server version is incompatible with expansion %s %s",
                    expansion.identifier, expansion.version
                )
                return false
            }
        }

        val removed = getExpansion(identifier)
        if (removed != null && !removed.unregister()) return false

        val event = ExpansionRegisterEvent(expansion)
        plugin.server.pluginManager.callEvent(event)

        if (event.isCancelled()) return false

        expansionsLock.lock()
        try {
            expansionsMap[identifier] = expansion
        } finally {
            expansionsLock.unlock()
        }

        if (expansion is Listener) plugin.server.pluginManager.registerEvents(expansion, plugin)

        Msg.info(
            "Successfully registered %s expansion: %s [%s]",
            expansion.expansionType.name.lowercase(),
            expansion.identifier,
            expansion.version
        )

        if (expansion is Taskable) expansion.start()

        // Check eCloud for updates only if the expansion is external
        if (plugin.placeholderAPIConfig.isCloudEnabled && expansion.expansionType === PlaceholderExpansion.Type.EXTERNAL) {
            val cloudExpansion = plugin.cloudExpansionManager.findCloudExpansionByName(identifier)
            if (cloudExpansion != null) {
                cloudExpansion.hasExpansion = true
                cloudExpansion.shouldUpdate = cloudExpansion.latestVersion != expansion.version
            }
        }

        return true
    }

    internal fun unregister(expansion: PlaceholderExpansion): Boolean {
        if (expansionsMap.remove(expansion.identifier.lowercase()) == null) {
            return false
        }

        plugin.server.pluginManager.callEvent(ExpansionUnregisterEvent(expansion))

        if (expansion is Listener) HandlerList.unregisterAll(expansion)
        if (expansion is Taskable) expansion.stop()
        if (expansion is Cacheable) expansion.clear()

        if (plugin.placeholderAPIConfig.isCloudEnabled) {
            plugin.cloudExpansionManager.findCloudExpansionByName(expansion.name)?.apply {
                hasExpansion = false
                shouldUpdate = false
            }
        }

        return true
    }

    private fun registerAll(sender: CommandSender) {
        Msg.info("Placeholder expansion registration initializing...")

        Futures.onMainThread(plugin, findExpansionsOnDisk()) { classes: List<Class<out PlaceholderExpansion>?>, exception: Throwable? ->
            if (exception != null) {
                Msg.severe("Failed to load class files of expansion.", exception)
                return@onMainThread
            }
            val registered = classes.filterNotNull().mapNotNull { register(it) }

            val needsUpdate = registered.mapNotNull { plugin.cloudExpansionManager.findCloudExpansionByName(it.name) }
                .count { it.shouldUpdate }

            val message = StringBuilder(if (registered.isEmpty()) "&6" else "&a")
                .append(registered.size)
                .append(" placeholder hook(s) registered!")

            if (needsUpdate > 0) {
                message.append(" &6")
                    .append(needsUpdate)
                    .append(" placeholder hook(s) have an update available.")
            }


            Msg.msg(sender, message.toString())
            plugin.server.pluginManager.callEvent(ExpansionsLoadedEvent(registered))
        }
    }

    private fun unregisterAll() = expansions.filterNot { it.persist }.forEach { it.unregister() }

    fun findExpansionsOnDisk(): CompletableFuture<List<Class<out PlaceholderExpansion>?>> {
        val files = expansionsFolder.listFiles { _: File, name: String -> name.endsWith(".jar") }
        if (files == null) return CompletableFuture.completedFuture(listOf())

        return Stream.of(*files)
            .map { findExpansionInFile(it) }
            .collect(Futures.collector())
    }

    fun findExpansionInFile(file: File) = CompletableFuture.supplyAsync {
        try {
            val expansionClass = FileUtil.findClass(file, PlaceholderExpansion::class.java)

            if (expansionClass == null) {
                Msg.severe(
                    "Failed to load expansion %s, as it does not have a class which"
                            + " extends PlaceholderExpansion", file.getName()
                )
                return@supplyAsync null
            }

            val expansionMethods = expansionClass.declaredMethods.map { MethodSignature(it.name, it.parameterTypes) }.toSet()
            if (!expansionMethods.containsAll(ABSTRACT_EXPANSION_METHODS)) {
                Msg.severe(
                    "Failed to load expansion %s, as it does not have the required"
                            + " methods declared for a PlaceholderExpansion.", file.getName()
                )
                return@supplyAsync null
            }

            return@supplyAsync expansionClass
        } catch (e: Error) {
            Msg.severe("Failed to load expansion %s (is a dependency missing?)", e, file.getName())
            return@supplyAsync null
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Failed to load expansion file: " + file.absolutePath, e)
            return@supplyAsync null
        }
    }!!


    @Throws(LinkageError::class)
    fun createExpansionInstance(clazz: Class<out PlaceholderExpansion>) = try {
        clazz.getDeclaredConstructor().newInstance()
    } catch (ex: Exception) {
        if (ex.cause is LinkageError) throw ex.cause!!

        Msg.warn("There was an issue with loading an expansion.")
        null
    }


    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        for (expansion in expansions) {
            if (expansion is Cleanable) {
                expansion.cleanup(event.player)
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onPluginDisable(event: PluginDisableEvent) {
        val name = event.plugin.name
        if (name == plugin.name) return

        for (expansion in expansions) {
            if (!name.equals(expansion.requiredPlugin, ignoreCase = true)) {
                continue
            }

            expansion.unregister()
            Msg.info("Unregistered placeholder expansion %s", expansion.identifier)
            Msg.info("Reason: required plugin $name was disabled.")
        }
    }

    companion object {
        private const val EXPANSIONS_FOLDER_NAME = "expansions"

        private val ABSTRACT_EXPANSION_METHODS = PlaceholderExpansion::class.java.getDeclaredMethods()
            .filter { Modifier.isAbstract(it.modifiers) }
            .map { MethodSignature(it.name, it.parameterTypes) }
            .toSet()
    }
}
