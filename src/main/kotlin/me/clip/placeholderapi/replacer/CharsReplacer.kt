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

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

class CharsReplacer(private val closure: Replacer.Closure) : Replacer {
    override fun apply(text: String, player: OfflinePlayer?, lookup: (String) -> PlaceholderExpansion?): String {
        val builder = StringBuilder(text.length)

        val identifier = StringBuilder()
        val parameters = StringBuilder()

        var i = 0
        while (i < text.length) {
            val l = text[i]

            if (l != closure.head || i + 1 >= text.length) {
                builder.append(l)
                i++
                continue
            }

            var identified = false
            var invalid = true
            var hadSpace = false

            while (++i < text.length) {
                val p = text[i]

                if (p == ' ' && !identified) {
                    hadSpace = true
                    break
                }
                if (p == closure.tail) {
                    invalid = false
                    break
                }

                if (p == '_' && !identified) {
                    identified = true
                    continue
                }

                (if (identified) parameters else identifier).append(p)
            }

            val identifierString = identifier.toString()
            val lowercaseIdentifierString = identifierString.lowercase()
            val parametersString = parameters.toString()

            identifier.setLength(0)
            parameters.setLength(0)

            if (invalid) {
                builder.append(closure.head).append(identifierString)

                if (identified) builder.append('_').append(parametersString)

                if (hadSpace) builder.append(' ')
                i++
                continue
            }

            val placeholder = lookup(lowercaseIdentifierString)
            if (placeholder == null) {
                builder.append(closure.head).append(identifierString)

                if (identified) builder.append('_')

                builder.append(parametersString).append(closure.tail)
                i++
                continue
            }

            val replacement: String? = placeholder.onRequest(player, parametersString)
            if (replacement == null) {
                builder.append(closure.head).append(identifierString)

                if (identified) builder.append('_')

                builder.append(parametersString).append(closure.tail)
                i++
                continue
            }

            builder.append(replacement)
            i++
        }

        return builder.toString()
    }
}
