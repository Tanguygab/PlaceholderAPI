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

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.clip.placeholderapi.replacer.CharsReplacer
import me.clip.placeholderapi.replacer.Replacer
import org.bukkit.OfflinePlayer

interface Values {
    class MockPlayerPlaceholderExpansion : PlaceholderExpansion() {
        override val identifier = "player"
        override val author = "Sxtanna"
        override val version = "1.0"

        override fun onRequest(player: OfflinePlayer?, params: String): String? {
            val parts = params.split("_")
            if (parts.isEmpty()) return null

            return when (parts[0]) {
                "name" -> PLAYER_NAME
                "x" -> PLAYER_X
                "y" -> PLAYER_Y
                "z" -> PLAYER_Z
                else -> null
            }
        }

        companion object {
            const val PLAYER_X = "10"
            const val PLAYER_Y = "20"
            const val PLAYER_Z = "30"
            const val PLAYER_NAME = "Sxtanna"
        }
    }

    companion object {
        const val SMALL_TEXT = "My name is %player_name%"
        const val LARGE_TEXT = "My name is %player_name% and my location is (%player_x%, %player_y%, %player_z%), this placeholder is invalid %server_name%"

        val PLACEHOLDERS = mapOf("player" to MockPlayerPlaceholderExpansion())
        val CHARS_REPLACER = CharsReplacer(Replacer.Closure.PERCENT)
    }
}
