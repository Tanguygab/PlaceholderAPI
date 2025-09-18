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
package me.clip.placeholderapi.expansion

/**
 * Implementing this interface adds the [start][.start] and [stop][.stop] void
 * methods to your [PlaceholderExpansion][PlaceholderExpansion].
 *
 *
 * This can be used to execute methods and tasks whenever the PlaceholderExpansion has been
 * successfully (un)registered.
 */
interface Taskable {
    /**
     * Called when the implementing class has successfully been registered to the placeholder map.
     * <br></br>Tasks that need to be performed when this expansion is registered should go here
     */
    fun start()

    /**
     * Called when the implementing class has been unregistered from PlaceholderAPI.
     * <br></br>Tasks that need to be performed when this expansion has unregistered should go here
     */
    fun stop()
}
