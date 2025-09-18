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
package me.clip.placeholderapi.updatechecker

import me.clip.placeholderapi.PlaceholderAPIPlugin
import me.clip.placeholderapi.util.Msg
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class UpdateChecker(private val plugin: PlaceholderAPIPlugin) : Listener {
    private val scheduler = plugin.scheduler
    private val pluginVersion = plugin.description.version
    private var spigotVersion: String? = null
    private var updateAvailable = false

    fun fetch() {
        scheduler.runTaskAsynchronously {
            try {
                val con = URL("https://api.spigotmc.org/legacy/update.php?resource=$RESOURCE_ID").openConnection() as HttpsURLConnection
                con.setRequestMethod("GET")
                spigotVersion = BufferedReader(InputStreamReader(con.getInputStream())).readLine()
            } catch (_: Exception) {
                plugin.logger.info("Failed to check for updates on spigot.")
                return@runTaskAsynchronously
            }
            if (spigotVersion.isNullOrEmpty()) return@runTaskAsynchronously

            updateAvailable = spigotIsNewer()

            if (!updateAvailable) return@runTaskAsynchronously
            scheduler.runTask {
                plugin.logger.info("An update for PlaceholderAPI (v" + this.spigotVersion + ") is available at:")
                plugin.logger.info("https://www.spigotmc.org/resources/placeholderapi.$RESOURCE_ID/")
                plugin.server.pluginManager.registerEvents(this, plugin)
            }
        }
    }

    private fun spigotIsNewer(): Boolean {
        if (spigotVersion.isNullOrEmpty()) return false

        val plV = toReadable(pluginVersion)
        val spV = toReadable(spigotVersion!!)

        return plV[0] < spV[0] || plV[1] < spV[1] || plV[2] < spV[2]
    }

    private fun toReadable(version: String): List<Int> {
        var version = version
        if (version.contains("-DEV")) {
            version = version.split("-DEV")[0]
        }

        return version.split(".").map { it.toInt() }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onJoin(e: PlayerJoinEvent) {
        if (e.player.hasPermission("placeholderapi.updatenotify")) {
            Msg.msg(
                e.player,
                "&bAn update for &fPlaceholder&7API &e(&fPlaceholder&7API &fv$spigotVersion&e)",
                "&bis available at &ehttps://www.spigotmc.org/resources/placeholderapi.$RESOURCE_ID/"
            )
        }
    }

    companion object {
        private const val RESOURCE_ID = 6245
    }
}
