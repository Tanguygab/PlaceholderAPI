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

import kotlin.math.max
import kotlin.math.min

/**
 * For the record, I am not sorry.
 */
object Format {
    fun tablify(align: Align, rows: List<List<String>>
    ): MutableList<String> {
        val format = buildFormat(align, findSpacing(rows))
        return rows
            .map { String.format(format, *it.toTypedArray()).substring(if (align == Align.RIGHT) 2 else 0) }
            .toMutableList()
    }

    private fun buildFormat(align: Align, spacing: List<Int>) = spacing.joinToString { "%" + (if (align == Align.LEFT) "-" else "") + (it + 2) + "s" }

    private fun findSpacing(rows: List<List<String>>) = rows
        .map { row -> row.map { it.length } }
        .reduce { l, r -> (0 ..< min(l.size, r.size)).map { max(l[it], r[it]) } }

    enum class Align {
        LEFT, RIGHT
    }
}
