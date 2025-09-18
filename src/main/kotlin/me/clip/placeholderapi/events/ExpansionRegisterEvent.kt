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
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * This event indicates that a **single** [PlaceholderExpansion] has
 * been registered in PlaceholderAPI.
 *
 *
 * To know when **all** Expansions have been registered, use the
 * [ExpansionsLoadedEvent][ExpansionsLoadedEvent] instead.
 */
class ExpansionRegisterEvent(
    /**
      * The [PlaceholderExpansion] that was registered in PlaceholderAPI.
      * <br></br>The PlaceholderExpansion will be available for use when the event
      * [was not cancelled][.isCancelled]!
      *
      * @return Current instance of the registered [PlaceholderExpansion]
      */
    val expansion: PlaceholderExpansion
) : Event(), Cancellable {

    private var cancelled = false

    /**
     * Indicates if this event was cancelled or not.
     * <br></br>A cancelled Event will result in the [PlaceholderExpansion][.getExpansion] NOT
     * being added to PlaceholderAPI's internal list and will therefore be considered not registered
     * anymore.
     *
     * @return Whether the event has been cancelled or not.
     */
    fun isCancelled() = cancelled

    fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }

    fun getHandlers() = HANDLERS

    companion object {
        private val HANDLERS = HandlerList()
    }
}
