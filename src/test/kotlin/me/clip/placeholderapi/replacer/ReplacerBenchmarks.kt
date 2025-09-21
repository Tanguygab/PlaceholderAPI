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
package me.clip.placeholderapi.replacer

import me.clip.placeholderapi.Values
import org.openjdk.jmh.annotations.Benchmark

class ReplacerBenchmarks {
    @Benchmark
    fun measureCharsReplacerSmallText() {
        Values.CHARS_REPLACER.apply(Values.SMALL_TEXT, null) { o: Any? -> Values.PLACEHOLDERS.get(o) }
    }

    @Benchmark
    fun measureCharsReplacerLargeText() {
        Values.CHARS_REPLACER.apply(Values.LARGE_TEXT, null) { o: Any? -> Values.PLACEHOLDERS.get(o) }
    }
}