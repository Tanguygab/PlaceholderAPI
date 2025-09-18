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
package me.clip.placeholderapi.expansion.cloud

import me.clip.placeholderapi.util.TimeUtil
import java.util.concurrent.TimeUnit

data class CloudExpansion(
    var name: String,
    val author: String,
    val latestVersion: String? = null,
    val description: String? = null,
    val sourceUrl: String? = null,
    val dependencyUrl: String? = null,

    var hasExpansion: Boolean = false,
    var shouldUpdate: Boolean = false,
    val isVerified: Boolean = false,

    val lastUpdate: Long = 0,
    val ratingsCount: Long = 0,

    val averageRating: Double = 0.0,

    val placeholders: List<String>? = null,

    val versions: List<Version>? = null
) {
    fun getTimeSinceLastUpdate(): String {
        val time = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - lastUpdate).toInt()
        return TimeUtil.getTime(time)
    }

    fun getVersion() = if (latestVersion == null) null else getVersion(latestVersion)

    fun getVersion(version: String) = versions?.find { it.version == version }

    fun getAvailableVersions() = versions!!.map { it.version }

    data class Version(
        val url: String,
        val version: String,
        val releaseNotes: String
    )
}
