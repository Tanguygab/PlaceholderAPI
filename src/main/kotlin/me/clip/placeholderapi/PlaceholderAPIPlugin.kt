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
package me.clip.placeholderapi

import me.clip.placeholderapi.commands.PlaceholderCommandRouter
import me.clip.placeholderapi.configuration.PlaceholderAPIConfig
import me.clip.placeholderapi.expansion.Version
import me.clip.placeholderapi.expansion.manager.CloudExpansionManager
import me.clip.placeholderapi.expansion.manager.LocalExpansionManager
import me.clip.placeholderapi.listeners.ServerLoadEventListener
import me.clip.placeholderapi.scheduler.UniversalScheduler
import me.clip.placeholderapi.updatechecker.UpdateChecker
import me.clip.placeholderapi.util.Msg
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bstats.bukkit.Metrics
import org.bstats.charts.AdvancedPie
import org.bstats.charts.SimplePie
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin
import java.text.SimpleDateFormat

/**
 * Yes I have a shitload of work to do...
 *
 * @author Ryan McCarthy
 */
class PlaceholderAPIPlugin : JavaPlugin() {

    /**
     * Obtain the configuration class for PlaceholderAPI.
     *
     * @return PlaceholderAPIConfig instance
     */
    val placeholderAPIConfig = PlaceholderAPIConfig(this)

    val localExpansionManager = LocalExpansionManager(this)
    val cloudExpansionManager = CloudExpansionManager(this)
    val scheduler = UniversalScheduler.getScheduler(this)

    lateinit var adventure: BukkitAudiences


    override fun onLoad() {
        INSTANCE = this

        saveDefaultConfig()
    }

    override fun onEnable() {
        setupCommand()
        setupMetrics()
        setupExpansions()

        adventure = BukkitAudiences.create(this)

        if (placeholderAPIConfig.isCloudEnabled) cloudExpansionManager.load()
        if (placeholderAPIConfig.checkUpdates) UpdateChecker(this).fetch()
    }

    override fun onDisable() {
        cloudExpansionManager.kill()
        localExpansionManager.kill()

        HandlerList.unregisterAll(this)

        scheduler.cancelTasks(this)

        adventure.close()
    }

    fun reloadConf(sender: CommandSender) {
        localExpansionManager.kill()

        reloadConfig()

        localExpansionManager.load(sender)

        if (placeholderAPIConfig.isCloudEnabled) cloudExpansionManager.load()
        else cloudExpansionManager.kill()
    }

    private fun setupCommand() {
        val pluginCommand = getCommand("placeholderapi") ?: return

        PlaceholderCommandRouter(this).let {
            pluginCommand.setExecutor(it)
            pluginCommand.tabCompleter = it
        }
    }

    private fun setupMetrics() {
        val metrics = Metrics(this, 438)
        metrics.addCustomChart(SimplePie("using_expansion_cloud") { if (placeholderAPIConfig.isCloudEnabled) "yes" else "no" })

        metrics.addCustomChart(SimplePie("using_spigot") { if (serverVersion.isSpigot) "yes" else "no" })

        metrics.addCustomChart(AdvancedPie("expansions_used") {
            val values = mutableMapOf<String, Int>()
            for (expansion in localExpansionManager.expansions) {
                values[expansion.requiredPlugin ?: expansion.identifier] = 1
            }
            values
        })
    }

    private fun setupExpansions() {
        server.pluginManager.registerEvents(localExpansionManager, this)

        try {
            Class.forName("org.bukkit.event.server.ServerLoadEvent")
            ServerLoadEventListener(this)
        } catch (_: ClassNotFoundException) {
            scheduler.runTaskLater( { localExpansionManager.load(server.consoleSender) }, 1)
        }
    }

    companion object {
        @Deprecated("")
        val serverVersion: Version
        private lateinit var INSTANCE: PlaceholderAPIPlugin

        init {
            var version = Bukkit.getServer().bukkitVersion.split("-")[0]
            val suffix: String?
            if (version.toCharArray().count { it == '.' } == 1) {
                suffix = "R1"
                version = 'v' + version.replace('.', '_') + "_$suffix"
            } else {
                val minor = version.split(".")[2][0].digitToInt()
                version = 'v' + version.replace('.', '_').replace("_$minor", "") + "_R" + (minor - 1)
            }

            var isSpigot: Boolean
            try {
                Class.forName("org.spigotmc.SpigotConfig")
                isSpigot = true
            } catch (_: ExceptionInInitializerError) {
                isSpigot = false
            } catch (_: ClassNotFoundException) {
                isSpigot = false
            }

            @Suppress("DEPRECATION")
            serverVersion = Version(version, isSpigot)
        }

        /**
         * Gets the static instance of the main class for PlaceholderAPI. This class is not the actual API
         * class, this is the main class that extends JavaPlugin. For most API methods, use static methods
         * available from the class: [PlaceholderAPI]
         *
         * @return PlaceholderAPIPlugin instance
         */
        @JvmStatic
        fun getInstance() = INSTANCE

        /**
         * Get the configurable [String] value that should be returned when a boolean is true
         *
         * @return string value of true
         */
        @JvmStatic
        fun booleanTrue() = INSTANCE.placeholderAPIConfig.booleanTrue

        /**
         * Get the configurable [String] value that should be returned when a boolean is false
         *
         * @return string value of false
         */
        @JvmStatic
        fun booleanFalse() = INSTANCE.placeholderAPIConfig.booleanFalse

        /**
         * Get the configurable [SimpleDateFormat] object that is used to parse time for
         * generic time based placeholders
         *
         * @return date format
         */
        @JvmStatic
        fun getDateFormat(): SimpleDateFormat {
            return try {
                SimpleDateFormat(INSTANCE.placeholderAPIConfig.dateFormat)
            } catch (e: IllegalArgumentException) {
                Msg.warn(
                    "Configured date format ('%s') is invalid! Defaulting to 'MM/dd/yy HH:mm:ss'",
                    e, INSTANCE.placeholderAPIConfig.dateFormat
                )
                SimpleDateFormat("MM/dd/yy HH:mm:ss")
            }
        }
    }
}
