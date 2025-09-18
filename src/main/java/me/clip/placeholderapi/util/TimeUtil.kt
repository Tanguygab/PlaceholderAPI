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
package me.clip.placeholderapi.util

import java.time.Duration

object TimeUtil {
    fun getRemaining(seconds: Int, type: TimeFormat?) = getRemaining(seconds.toLong(), type)

    fun getRemaining(seconds: Long, type: TimeFormat?) = when (type) {
        TimeFormat.DAYS -> (seconds / 86400)
        TimeFormat.HOURS -> ((seconds / 3600) % 24)
        TimeFormat.MINUTES -> ((seconds / 60) % 60)
        TimeFormat.SECONDS -> (seconds % 60)
        else -> seconds
    }.toString()

    /**
     * Format the given value with s, m, h and d (seconds, minutes, hours and days)
     *
     * @param duration [Duration] (eg, Duration.of(20, [ChronoUnit.SECONDS]) for 20
     * seconds)
     * @return formatted time
     */
    fun getTime(duration: Duration) = getTime(duration.seconds)

    fun getTime(seconds: Int) = getTime(seconds.toLong())

    fun getTime(seconds: Long): String {
        val joiner = mutableListOf<String>()

        var seconds = seconds
        var minutes = seconds / 60
        var hours = minutes / 60
        val days = hours / 24

        seconds %= 60
        minutes %= 60
        hours %= 24

        if (days > 0) joiner.add(days.toString() + "d")
        if (hours > 0) joiner.add(hours.toString() + "h")
        if (minutes > 0) joiner.add(minutes.toString() + "m")
        if (seconds > 0) joiner.add(seconds.toString() + "s")

        return joiner.joinToString(" ")
    }
}
