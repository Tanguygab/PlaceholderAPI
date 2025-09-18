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
 * This event indicates that a [PlaceholderExpansion] has been
 * unregistered by PlaceholderAPI.
 *
 *
 * Note that this event is triggered **before** the PlaceholderExpansion is completely
 * removed.
 * <br></br>This includes removing any Listeners, stopping active tasks and clearing the cache of
 * the PlaceholderExpansion.
 */
class ExpansionUnregisterEvent(
    /**
     * The [PlaceholderExpansion] that was unregistered.
     *
     * @return The [PlaceholderExpansion] instance.
     */
    val expansion: PlaceholderExpansion
) : Event() {

    fun getHandlers()= HANDLERS

    companion object {
        private val HANDLERS = HandlerList()
    }
}
