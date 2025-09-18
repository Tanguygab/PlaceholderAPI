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
package me.clip.placeholderapi.configuration

import me.clip.placeholderapi.PlaceholderAPIPlugin

class PlaceholderAPIConfig(private val plugin: PlaceholderAPIPlugin) {
    val checkUpdates = plugin.config.getBoolean("check_updates")

    var isCloudEnabled: Boolean
        get() = plugin.config.getBoolean("cloud_enabled")
        set(state) {
            plugin.config.set("cloud_enabled", state)
            plugin.saveConfig()
        }


    val isDebugMode = plugin.config.getBoolean("debug", false)

    fun getExpansionSort(): ExpansionSort? {
        val option: String = plugin.config.getString("cloud_sorting", ExpansionSort.LATEST.name)

        return try {
            ExpansionSort.valueOf(option.uppercase())
        } catch (_: IllegalArgumentException) {
            null
        }
    }


    val dateFormat = plugin.config.getString("date_format", "MM/dd/yy HH:mm:ss")

    val booleanTrue = plugin.config.getString("boolean.true", "true")
    val booleanFalse = plugin.config.getString("boolean.false", "false")
}
