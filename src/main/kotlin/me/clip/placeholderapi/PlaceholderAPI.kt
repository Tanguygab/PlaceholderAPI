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
package me.clip.placeholderapi

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import me.clip.placeholderapi.expansion.Relational
import me.clip.placeholderapi.replacer.CharsReplacer
import me.clip.placeholderapi.replacer.Replacer
import me.clip.placeholderapi.util.Msg
import org.jetbrains.annotations.ApiStatus
import java.util.regex.Matcher
import java.util.regex.Pattern

object PlaceholderAPI {
    private val REPLACER_PERCENT: Replacer = CharsReplacer(Replacer.Closure.PERCENT)
    private val REPLACER_BRACKET: Replacer = CharsReplacer(Replacer.Closure.BRACKET)

    /**
     * Get the normal placeholder pattern.
     *
     * @return Regex Pattern of [%]([^%]+)[%]
     */
    @JvmStatic
    val placeholderPattern: Pattern = Pattern.compile("%([^%]+)%")

    /**
     * Get the bracket placeholder pattern.
     *
     * @return Regex Pattern of [{]([^{}]+)[}]
     */
    @JvmStatic
    val bracketPlaceholderPattern: Pattern = Pattern.compile("[{]([^{}]+)[}]")

    /**
     * Get the relational placeholder pattern.
     *
     * @return Regex Pattern of [%](rel_)([^%]+)[%]
     */
    @JvmStatic
    val relationalPlaceholderPattern: Pattern = Pattern.compile("%(rel_)([^%]+)%")


    // === Current API ===
    /**
     * Translates all placeholders into their corresponding values.
     * <br></br>The pattern of a valid placeholder is %&lt;identifier&gt;_&lt;params&gt;%.
     *
     * @param player Player to parse the placeholders against
     * @param text Text to set the placeholder values in
     * @return String containing all translated placeholders
     */
    @JvmStatic
    fun setPlaceholders(player: OfflinePlayer?, text: String): String {
        return REPLACER_PERCENT.apply(text, player, PlaceholderAPIPlugin.getInstance().localExpansionManager::getExpansion)
    }

    /**
     * Translates all placeholders into their corresponding values.
     * <br></br>The pattern of a valid placeholder is %&lt;identifier&gt;_&lt;params&gt;%.
     *
     * @param player Player to parse the placeholders against
     * @param text List of Strings to set the placeholder values in
     * @return String containing all translated placeholders
     */
    @JvmStatic
    fun setPlaceholders(player: OfflinePlayer?, text: List<String>) = text.map { setPlaceholders(player, it) }

    /**
     * Translates all placeholders into their corresponding values.
     * <br></br>The pattern of a valid placeholder is %&lt;identifier&gt;_&lt;params&gt;%.
     *
     * @param player Player to parse the placeholders against
     * @param text Text to set the placeholder values in
     * @return String containing all translated placeholders
     */
    @JvmStatic
    fun setPlaceholders(player: Player?, text: String) = setPlaceholders(player as OfflinePlayer?, text)

    /**
     * Translates all placeholders into their corresponding values.
     * <br></br>The pattern of a valid placeholder is %&lt;identifier&gt;_&lt;params&gt;%.
     *
     * @param player Player to parse the placeholders against
     * @param text List of Strings to set the placeholder values in
     * @return String containing all translated placeholders
     */
    @JvmStatic
    fun setPlaceholders(player: Player?, text: List<String>) = setPlaceholders(player as OfflinePlayer?, text)

    /**
     * Translates all placeholders into their corresponding values.
     * <br></br>The pattern of a valid placeholder is {&lt;identifier&gt;_&lt;params&gt;}.
     *
     * @param player Player to parse the placeholders against
     * @param text Text to set the placeholder values in
     * @return String containing all translated placeholders
     */
    @JvmStatic
    fun setBracketPlaceholders(player: OfflinePlayer?, text: String): String {
        return REPLACER_BRACKET.apply(text, player, PlaceholderAPIPlugin.getInstance().localExpansionManager::getExpansion)
    }

    /**
     * Translates all placeholders into their corresponding values.
     * <br></br>The pattern of a valid placeholder is {&lt;identifier&gt;_&lt;params&gt;}.
     *
     * @param player Player to parse the placeholders against
     * @param text List of Strings to set the placeholder values in
     * @return String containing all translated placeholders
     */
    @JvmStatic
    fun setBracketPlaceholders(player: OfflinePlayer?, text: List<String>) = text.map { setBracketPlaceholders(player, it) }

    /**
     * Translates all placeholders into their corresponding values.
     * <br></br>The pattern of a valid placeholder is {&lt;identifier&gt;_&lt;params&gt;}.
     *
     * @param player Player to parse the placeholders against
     * @param text Text to set the placeholder values in
     * @return String containing all translated placeholders
     */
    @JvmStatic
    fun setBracketPlaceholders(player: Player?, text: String) = setBracketPlaceholders(player as OfflinePlayer?, text)

    /**
     * Translates all placeholders into their corresponding values.
     * <br></br>The pattern of a valid placeholder is {&lt;identifier&gt;_&lt;params&gt;}.
     *
     * @param player Player to parse the placeholders against
     * @param text List of Strings to set the placeholder values in
     * @return String containing all translated placeholders
     */
    @JvmStatic
    fun setBracketPlaceholders(player: Player?, text: List<String>) =
        setBracketPlaceholders(player as OfflinePlayer?, text)

    /**
     * set relational placeholders in the text specified placeholders are matched with the pattern
     * %&lt;rel_(identifier)_(params)&gt;% when set with this method
     *
     * @param one First player to compare
     * @param two Second player to compare
     * @param text Text to parse the placeholders in
     * @return The text containing the parsed relational placeholders
     */
    @JvmStatic
    fun setRelationalPlaceholders(one: Player?, two: Player?, text: String): String {
        var text = text
        val matcher = relationalPlaceholderPattern.matcher(text)

        while (matcher.find()) {
            val format = matcher.group(2)
            val index = format.indexOf("_")

            if (index <= 0 || index >= format.length) continue

            val identifier = format.take(index).lowercase()
            val params = format.substring(index + 1)
            val expansion = PlaceholderAPIPlugin.getInstance().localExpansionManager.getExpansion(identifier)

            if (expansion !is Relational) continue

            val value = expansion.onPlaceholderRequest(one, two, params) ?: continue
            text = text.replace(matcher.group(), Matcher.quoteReplacement(value))
        }

        return text
    }

    /**
     * Translate placeholders in the provided List based on the relation of the two provided players.
     * <br></br>The pattern of a valid placeholder is %rel_&lt;identifier&gt;_&lt;param&gt;%.
     *
     * @param one Player to compare
     * @param two Player to compare
     * @param text text to parse the placeholder values to
     * @return The text containing the parsed relational placeholders
     */
    @JvmStatic
    fun setRelationalPlaceholders(one: Player?, two: Player?, text: List<String>) = text.map { setRelationalPlaceholders(one, two, it) }

    /**
     * Check if a specific placeholder identifier is currently registered
     *
     * @param identifier The identifier to check
     * @return true if identifier is already registered
     */
    @JvmStatic
    fun isRegistered(identifier: String) = PlaceholderAPIPlugin.getInstance().localExpansionManager.findExpansionByIdentifier(identifier) != null

    /**
     * Get all registered placeholder identifiers
     *
     * @return A Set of type String containing the identifiers of all registered expansions.
     */
    @JvmStatic
    fun getRegisteredIdentifiers() = PlaceholderAPIPlugin.getInstance().localExpansionManager.identifiers.toSet()

    /**
     * Check if a String contains any PlaceholderAPI placeholders (%&lt;identifier&gt;_&lt;params&gt;%).
     *
     * @param text String to check
     * @return true if String contains any matches to the normal placeholder pattern, false otherwise
     */
    @JvmStatic
    fun containsPlaceholders(text: String?) = text != null && placeholderPattern.matcher(text).find()

    /**
     * Check if a String contains any PlaceholderAPI bracket placeholders ({&lt;identifier&gt;_&lt;params&gt;}).
     *
     * @param text String to check
     * @return true if String contains any matches to the bracket placeholder pattern, false otherwise
     */
    @JvmStatic
    fun containsBracketPlaceholders(text: String?) = text != null && bracketPlaceholderPattern.matcher(text).find()

    // === Deprecated API ===
    @Deprecated("")
    @ApiStatus.ScheduledForRemoval(inVersion = "2.13.0")
    @JvmStatic
    fun registerExpansion(expansion: PlaceholderExpansion) = expansion.register()

    @Deprecated("")
    @ApiStatus.ScheduledForRemoval(inVersion = "2.13.0")
    @JvmStatic
    fun unregisterExpansion(expansion: PlaceholderExpansion) = expansion.unregister()

    /**
     * Get map of registered placeholders
     *
     * @return Map of registered placeholders
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.13.0")
    @Deprecated("Use {@link LocalExpansionManager#getExpansions()} instead.")
    @JvmStatic
    fun getPlaceholders() = PlaceholderAPIPlugin.getInstance().localExpansionManager.expansions.associateBy { it.identifier }

    /**
     * @param plugin The Plugin to register with this [PlaceholderHook]
     * @param placeholderHook The [PlaceholderHook] to register
     * @return always false
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.13.0")
    @Deprecated("Please use {@link PlaceholderExpansion} to register placeholders instead")
    @JvmStatic
    fun registerPlaceholderHook(plugin: Plugin, placeholderHook: PlaceholderHook?): Boolean {
        Msg.warn(
            "Nag author(s) %s of plugin %s about their usage of the deprecated PlaceholderHook"
                    + " class! This class will be removed in v2.13.0!",
            plugin.description.authors,
            plugin.name
        )
        return false
    }

    /**
     * @param identifier The identifier to use for the [PlaceholderHook]
     * @param placeholderHook The [PlaceholderHook] to register
     * @return always false
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.13.0")
    @Deprecated("Please use {@link PlaceholderExpansion} to register placeholders instead")
    @JvmStatic
    fun registerPlaceholderHook(identifier: String, placeholderHook: PlaceholderHook): Boolean {
        Msg.warn(
            "$identifier is attempting to register placeholders via deprecated PlaceholderHook class."
                    + " This class is no longer supported and will be removed in v2.13.0!"
        )
        return false
    }

    /**
     * @param plugin The plugin to unregister
     * @return always false
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.13.0")
    @Deprecated("Please use {@link PlaceholderExpansion} to unregister placeholders instead")
    @JvmStatic
    fun unregisterPlaceholderHook(plugin: Plugin): Boolean {
        Msg.warn(
            ("Nag author(s) %s of plugin %s about their usage of the PlaceholderAPI class."
                    + " This way of unregistering placeholders is no longer supported and will be removed"
                    + " in v2.13.0!"), plugin.description.authors, plugin.name
        )
        return false
    }

    /**
     * @param identifier The identifier to unregister
     * @return always false
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.13.0")
    @Deprecated("Please use {@link PlaceholderExpansion} to unregister placeholders instead")
    @JvmStatic
    fun unregisterPlaceholderHook(identifier: String?): Boolean {
        Msg.warn(
            "$identifier is attempting to unregister placeholders via PlaceholderAPI class."
                    + " This way of unregistering placeholders is no longer supported and will be removed"
                    + " in v2.13.0!"
        )
        return false
    }

    /**
     * @return Set of registered identifiers
     */
    @Deprecated("Will be removed in a future release.")
    @ApiStatus.ScheduledForRemoval(inVersion = "2.13.0")
    @JvmStatic
    fun registeredPlaceholderPlugins() = getRegisteredIdentifiers()

    /**
     * @return always null
     */
    @Deprecated("Will be removed in a future release.")
    @ApiStatus.ScheduledForRemoval(inVersion = "2.13.0")
    @JvmStatic
    fun externalPlaceholderPlugins(): Set<String>? = null

    /**
     * @param player The offline player to parse the placeholders against
     * @param text The text to parse
     * @param pattern The Pattern to use
     * @param colorize If PlaceholderAPI should also parse color codes
     * @return String with the parsed placeholders
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.13.0")
    @Deprecated("Please use {@link #setPlaceholders(OfflinePlayer, String)} instead")
    @JvmStatic
    fun setPlaceholders(player: OfflinePlayer?, text: String, pattern: Pattern, colorize: Boolean) =
        setPlaceholders(player, text)

    /**
     * @param player The offline player to parse the placeholders against
     * @param text The List of text to parse
     * @param pattern The Pattern to use
     * @param colorize If PlaceholderAPI should also parse color codes
     * @return String with the parsed placeholders
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.13.0")
    @Deprecated("Please use {@link #setPlaceholders(OfflinePlayer, List)} instead")
    @JvmStatic
    fun setPlaceholders(player: OfflinePlayer?, text: List<String>, pattern: Pattern, colorize: Boolean) =
        setPlaceholders(player, text)

    /**
     * @param player The offline player to parse the placeholders against
     * @param text The List of text to parse
     * @param colorize If PlaceholderAPI should also parse color codes
     * @return String with the parsed placeholders
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.13.0")
    @Deprecated("Use {@link #setPlaceholders(OfflinePlayer, List)} instead.")
    @JvmStatic
    fun setPlaceholders(player: OfflinePlayer?, text: List<String>, colorize: Boolean) = setPlaceholders(player, text)

    /**
     * @param player The offline player to parse the placeholders against
     * @param text The List of text to parse
     * @param pattern The Pattern to use
     * @return String with the parsed placeholders
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.13.0")
    @Deprecated("Use {@link #setPlaceholders(OfflinePlayer, List)} instead.")
    @JvmStatic
    fun setPlaceholders(player: OfflinePlayer?, text: List<String>, pattern: Pattern) = setPlaceholders(player, text)

    /**
     * @param player The offline player to parse the placeholders against
     * @param text The text to parse
     * @param colorize If PlaceholderAPI should also parse color codes
     * @return String with the parsed placeholders
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.13.0")
    @Deprecated("Will be removed in a future release.")
    @JvmStatic
    fun setPlaceholders(player: Player?, text: String, colorize: Boolean) = setPlaceholders(player, text)

    /**
     * @param player The offline player to parse the placeholders against
     * @param text The List of text to parse
     * @param colorize If PlaceholderAPI should also parse color codes
     * @return String with the parsed placeholders
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.13.0")
    @Deprecated("Will be removed in a future release.")
    @JvmStatic
    fun setPlaceholders(player: Player?, text: List<String>, colorize: Boolean) = setPlaceholders(player, text)

    /**
     * @param player The offline player to parse the placeholders against
     * @param text The text to parse
     * @param colorize If PlaceholderAPI should also parse color codes
     * @return String with the parsed placeholders
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.13.0")
    @Deprecated("Use {@link #setPlaceholders(OfflinePlayer, String)} instead.")
    @JvmStatic
    fun setPlaceholders(player: OfflinePlayer?, text: String, colorize: Boolean) = setPlaceholders(player, text)

    /**
     * @param player The offline player to parse the placeholders against
     * @param text The text to parse
     * @param pattern The Pattern to use
     * @return String with the parsed placeholders
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.13.0")
    @Deprecated("Use {@link #setPlaceholders(OfflinePlayer, String)} instead.")
    @JvmStatic
    fun setPlaceholders(player: OfflinePlayer?, text: String, pattern: Pattern) = setPlaceholders(player, text)

    /**
     * @param player The offline player to parse the placeholders against
     * @param text The List of text to parse
     * @param colorize If PlaceholderAPI should also parse color codes
     * @return String with the parsed placeholders
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.13.0")
    @Deprecated("Use {@link #setPlaceholders(OfflinePlayer, List)} instead.")
    @JvmStatic
    fun setBracketPlaceholders(player: OfflinePlayer?, text: List<String>, colorize: Boolean) =
        setBracketPlaceholders(player, text)

    /**
     * @param player The offline player to parse the placeholders against
     * @param text The text to parse
     * @param colorize If PlaceholderAPI should also parse color codes
     * @return String with the parsed placeholders
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.13.0")
    @Deprecated("Use {@link #setPlaceholders(OfflinePlayer, String)} instead.")
    @JvmStatic
    fun setBracketPlaceholders(player: OfflinePlayer?, text: String, colorize: Boolean) =
        setBracketPlaceholders(player, text)

    /**
     * @param player The offline player to parse the placeholders against
     * @param text The text to parse
     * @param colorize If PlaceholderAPI should also parse color codes
     * @return String with the parsed placeholders
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.13.0")
    @Deprecated("Will be removed in a future release.")
    @JvmStatic
    fun setBracketPlaceholders(player: Player?, text: String, colorize: Boolean) = setBracketPlaceholders(player, text)

    /**
     * @param player The offline player to parse the placeholders against
     * @param text The List of text to parse
     * @param colorize If PlaceholderAPI should also parse color codes
     * @return String with the parsed placeholders
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.13.0")
    @Deprecated("Will be removed in a future release.")
    @JvmStatic
    fun setBracketPlaceholders(player: Player?, text: List<String>, colorize: Boolean) = setBracketPlaceholders(player, text)

    /**
     * set relational placeholders in the text specified placeholders are matched with the pattern
     * %&lt;rel_(identifier)_(params)&gt;% when set with this method
     *
     * @param one Player to compare
     * @param two Player to compare
     * @param text Text to parse the placeholders in
     * @param colorize If color codes (&amp;[0-1a-fk-o]) should be translated
     * @return The text containing the parsed relational placeholders
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.13.0")
    @Deprecated("Use {@link #setPlaceholders(OfflinePlayer, String)} instead.")
    @JvmStatic
    fun setRelationalPlaceholders(one: Player?, two: Player?, text: String, colorize: Boolean) = setRelationalPlaceholders(one, two, text)

    /**
     * Translate placeholders in the provided list based on the relation of the two provided players.
     * <br></br>The pattern of a valid placeholder is %rel_&lt;identifier&gt;_&lt;params&gt;%.
     *
     * @param one First player to compare
     * @param two Second player to compare
     * @param text Text to parse the placeholders in
     * @param colorize If color codes (&amp;[0-1a-fk-o]) should be translated
     * @return The text containing the parsed relational placeholders
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.13.0")
    @Deprecated("Use {@link #setRelationalPlaceholders(Player, Player, List)} instead.")
    @JvmStatic
    fun setRelationalPlaceholders(one: Player?, two: Player?, text: List<String>, colorize: Boolean) = setRelationalPlaceholders(one, two, text)
}
