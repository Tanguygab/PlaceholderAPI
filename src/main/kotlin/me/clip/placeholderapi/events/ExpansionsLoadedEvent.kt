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
package me.clip.placeholderapi.events

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * This event indicated that **all** [PlaceholderExpansions][PlaceholderExpansion] have
 * been registered in PlaceholderAPI and can now be used.
 * <br></br>This even will also be triggered whenever PlaceholderAPI gets reloaded.
 *
 *
 * All PlaceholderExpansions, except for those loaded by plugins, are loaded
 * after Spigot triggered its ServerLoadEvent (1.13+), or after PlaceholderAPI has been enabled.
 */
class ExpansionsLoadedEvent(expansions: List<PlaceholderExpansion>) : Event() {

    /**
     * Returns a unmodifiable list of [PlaceholderExpansions][PlaceholderExpansion] that
     * have been registered by PlaceholderAPI.
     *
     *
     * **This list does not include manually registered PlaceholderExpansions.**
     *
     * @return List of [registered PlaceholderExpansions][PlaceholderExpansion].
     */
    val expansions = expansions.toList()

    override fun getHandlers() = HANDLERS

    companion object {
        private val HANDLERS = HandlerList()
    }
}
