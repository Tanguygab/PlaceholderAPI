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
import me.clip.placeholderapi.Values.MockPlayerPlaceholderExpansion
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ReplacerUnitTester {
    @Test
    fun testCharsReplacerProducesExpectedSingleValue() {
        Assertions.assertEquals(
            MockPlayerPlaceholderExpansion.PLAYER_NAME,
            Values.CHARS_REPLACER.apply("%player_name%", null) { Values.PLACEHOLDERS[it] }
        )
    }

    @Test
    fun testCharsReplacerProducesExpectedSentence() {
        Assertions.assertEquals(
            String.format(
                "My name is %s and my location is (%s, %s, %s), this placeholder is invalid %%server_name%%",
                MockPlayerPlaceholderExpansion.PLAYER_NAME,
                MockPlayerPlaceholderExpansion.PLAYER_X,
                MockPlayerPlaceholderExpansion.PLAYER_Y,
                MockPlayerPlaceholderExpansion.PLAYER_Z
            ),
            Values.CHARS_REPLACER.apply(Values.LARGE_TEXT, null) { Values.PLACEHOLDERS[it] }
        )
    }

    @Test
    fun testResultsAreTheSameAsReplacement() {
        val resultChars = Values.CHARS_REPLACER.apply("%player_name%", null) { Values.PLACEHOLDERS[it] }

        Assertions.assertEquals(MockPlayerPlaceholderExpansion.PLAYER_NAME, resultChars)
    }

    @Test
    fun testCharsReplacerIgnoresMalformed() {
        val text = "10% and %hello world 15%"

        Assertions.assertEquals(text, Values.CHARS_REPLACER.apply(text, null) { Values.PLACEHOLDERS[it] })
    }
}
