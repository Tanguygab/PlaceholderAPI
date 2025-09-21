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

import me.clip.placeholderapi.PlaceholderAPIPlugin
import me.clip.placeholderapi.PlaceholderHook
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.Contract
import java.util.logging.Level

/**
 * Any class extending this will be able to get registered as a PlaceholderExpansion.
 * <br></br>The registration either happens automatically when the jar file containing a
 * class extending this one is located under the `PlaceholderAPI/expansions`
 * directory or when the [.register] method is called by said class.
 */
abstract class PlaceholderExpansion : PlaceholderHook() {
    /**
     * Get the type of the expansion
     *
     * @return the type of the expansion
     * @since 2.11.4
     */
    /**
     * Set the type of the expansion
     * @param expansionType the new type
     * @since 2.11.4
     */
    /**
     * The type is [Type.INTERNAL] by default.
     * For external expansions, the type is updated on [register][me.clip.placeholderapi.expansion.manager.LocalExpansionManager.register].
     * @since 2.11.4
     */
    internal var expansionType = Type.INTERNAL

    /**
     * The placeholder identifier of this expansion. May not contain %,
     * {} or _
     *
     * @return placeholder identifier that is associated with this expansion
     */
    abstract val identifier: String

    /**
     * The author of this expansion
     *
     * @return name of the author for this expansion
     */
    abstract val author: String

    /**
     * The version of this expansion
     *
     * @return current version of this expansion
     */
    abstract val version: String

    /**
     * The name of this expansion
     *
     * @return [.getIdentifier] by default, name of this expansion if specified
     */
    open val name
        get() = identifier

    // === Deprecated API ===
    /**
     * @return The plugin name.
     */
    @Deprecated("As of versions greater than 2.8.7, use {@link #requiredPlugin}")
    @ApiStatus.ScheduledForRemoval(inVersion = "2.13.0")
    open val plugin: String? = null

    /**
     * The name of the plugin that this expansion hooks into. by default will null
     *
     * @return plugin name that this expansion requires to function
     */
    open val requiredPlugin
        get() = plugin

    /**
     * The placeholders associated with this expansion
     *
     * @return placeholder list that this expansion provides
     */
    open val placeholders = listOf<String>()


    /**
     * Expansions that do not use the ecloud and instead register from the dependency should set this
     * to true to ensure that your placeholder expansion is not unregistered when the papi reload
     * command is used
     *
     * @return if this expansion should persist through placeholder reloads
     */
    open val persist = false
    open fun persist() = persist

    /**
     * Check if this placeholder identifier has already been registered
     *
     * @return true if the identifier for this expansion is already registered
     */
    fun isRegistered() = placeholderAPI.localExpansionManager.findExpansionByIdentifier(identifier) == this

    /**
     * If any requirements need to be checked before this expansion should register, you can check
     * them here
     *
     * @return true if this hook meets all the requirements to register
     */
    open fun canRegister() = requiredPlugin == null || placeholderAPI.server.pluginManager.getPlugin(requiredPlugin!!) != null

    /**
     * Attempt to register this PlaceholderExpansion
     *
     * @return true if this expansion is now registered with PlaceholderAPI
     */
    open fun register() = placeholderAPI.localExpansionManager.register(this)

    /**
     * Attempt to unregister this PlaceholderExpansion
     *
     * @return true if this expansion is now unregistered with PlaceholderAPI
     */
    open fun unregister() = placeholderAPI.localExpansionManager.unregister(this)


    /**
     * Quick getter for the [PlaceholderAPIPlugin] instance
     *
     * @return [PlaceholderAPIPlugin] instance
     */
    val placeholderAPI = PlaceholderAPIPlugin.getInstance()

    // === Configuration ===
    /**
     * Gets the ConfigurationSection of the expansion located in the config.yml of PlaceholderAPI or
     * null when not specified.
     * <br></br>You may use the [Configurable] interface to define default values set
     *
     * @return ConfigurationSection that this expansion has.
     */
    val configSection
        get() = placeholderAPI.config.getConfigurationSection("expansions.$identifier")

    /**
     * Gets the ConfigurationSection relative to the [default one][.getConfigSection] set
     * by the expansion or null when the default ConfigurationSection is null
     *
     * @param path The path to get the ConfigurationSection from. This is relative to the default section
     * @return ConfigurationSection relative to the default section
     */
    fun getConfigSection(path: String) = configSection?.getConfigurationSection(path)

    /**
     * Gets the Object relative to the [default ConfigurationSection][.getConfigSection] set
     * by the expansion or the provided Default Object, when the default ConfigurationSection is null
     *
     * @param path The path to get the Object from. This is relative to the default section
     * @param def The default Object to return when the ConfigurationSection returns null
     * @return Object from the provided path or the default one provided
     */
    @Contract("_, !null -> !null")
    fun get(path: String, def: Any?) = configSection?.get(path, def) ?: def

    /**
     * Gets the int relative to the [default ConfigurationSection][.getConfigSection] set
     * by the expansion or the provided Default int, when the default ConfigurationSection is null
     *
     * @param path The path to get the int from. This is relative to the default section
     * @param def The default int to return when the ConfigurationSection returns null
     * @return int from the provided path or the default one provided
     */
    fun getInt(path: String, def: Int) = configSection?.getInt(path, def) ?: def

    /**
     * Gets the long relative to the [default ConfigurationSection][.getConfigSection] set
     * by the expansion or the provided Default long, when the default ConfigurationSection is null
     *
     * @param path The path to get the long from. This is relative to the default section
     * @param def The default long to return when the ConfigurationSection returns null
     * @return long from the provided path or the default one provided
     */
    fun getLong(path: String, def: Long) = configSection?.getLong(path, def) ?: def

    /**
     * Gets the double relative to the [default ConfigurationSection][.getConfigSection] set
     * by the expansion or the provided Default double, when the default ConfigurationSection is null
     *
     * @param path The path to get the double from. This is relative to the default section
     * @param def The default double to return when the ConfigurationSection returns null
     * @return double from the provided path or the default one provided
     */
    fun getDouble(path: String, def: Double) = configSection?.getDouble(path, def) ?: def

    /**
     * Gets the String relative to the [default ConfigurationSection][.getConfigSection] set
     * by the expansion or the provided Default String, when the default ConfigurationSection is null
     *
     * @param path The path to get the String from. This is relative to the default section
     * @param def The default String to return when the ConfigurationSection returns null. Can be null
     * @return String from the provided path or the default one provided
     */
    @Contract("_, !null -> !null")
    fun getString(path: String, def: String?) = configSection?.getString(path, def) ?: def

    /**
     * Gets a String List relative to the [default ConfigurationSection][.getConfigSection] set
     * by the expansion or an empty List, when the default ConfigurationSection is null
     *
     * @param path The path to get the String list from. This is relative to the default section
     * @return String list from the provided path or an empty list
     */
    fun getStringList(path: String) = configSection?.getStringList(path) ?: listOf<String>()

    /**
     * Gets the boolean relative to the [default ConfigurationSection][.getConfigSection] set
     * by the expansion or the default boolean, when the default ConfigurationSection is null
     *
     * @param path The path to get the boolean from. This is relative to the default section
     * @param def The default boolean to return when the ConfigurationSection is null
     * @return boolean from the provided path or the default one provided
     */
    fun getBoolean(path: String, def: Boolean) = configSection?.getBoolean(path, def) ?: def

    /**
     * Whether the [default ConfigurationSection][.getConfigSection] contains the provided path
     * or not. This will return `false` when either the default section is null, or doesn't
     * contain the provided path
     *
     * @param path The path to check
     * @return true when the default ConfigurationSection is not null and contains the path, false otherwise
     */
    fun configurationContains(path: String) = configSection?.contains(path) == true

    /**
     * Logs the provided message with the provided Level in the console.
     * <br></br>The message will be prefixed with [&lt;code&gt;[&amp;lt;expansion name&amp;gt;]&lt;/code&gt;][.getName]
     *
     * @param level The Level at which the message should be logged with
     * @param msg The message to log
     */
    fun log(level: Level, msg: String) = placeholderAPI.logger.log(level, "[$name] $msg")

    /**
     * Logs the provided message and Throwable with the provided Level in the console.
     * <br></br>The message will be prefixed with [&lt;code&gt;[&amp;lt;expansion name&amp;gt;]&lt;/code&gt;][.getName]
     *
     * @param level The Level at which the message should be logged with
     * @param msg The message to log
     * @param throwable The Throwable to log
     */
    fun log(level: Level, msg: String, throwable: Throwable) = placeholderAPI.logger.log(level, "[$name] $msg", throwable)

    /**
     * Logs the provided message with Level "info".
     * <br></br>The message will be prefixed with [&lt;code&gt;[&amp;lt;expansion name&amp;gt;]&lt;/code&gt;][.getName]
     *
     * @param msg The message to log
     */
    fun info(msg: String) = log(Level.INFO, msg)

    /**
     * Logs the provided message with Level "warning".
     * <br></br>The message will be prefixed with [&lt;code&gt;[&amp;lt;expansion name&amp;gt;]&lt;/code&gt;][.getName]
     *
     * @param msg The message to log
     */
    fun warning(msg: String) = log(Level.WARNING, msg)

    /**
     * Logs the provided message with Level "severe" (error).
     * <br></br>The message will be prefixed with [&lt;code&gt;[&amp;lt;expansion name&amp;gt;]&lt;/code&gt;][.getName]
     *
     * @param msg The message to log
     */
    fun severe(msg: String) = log(Level.SEVERE, msg)

    /**
     * Logs the provided message and Throwable with Level "severe" (error).
     * <br></br>The message will be prefixed with [&lt;code&gt;[&amp;lt;expansion name&amp;gt;]&lt;/code&gt;][.getName]
     *
     * @param msg The message to log
     * @param throwable The Throwable to log
     */
    fun severe(msg: String, throwable: Throwable) = log(Level.SEVERE, msg, throwable)


    /**
     * Whether the provided Object is an instance of this PlaceholderExpansion.
     * <br></br>This method will perform the following checks in order:
     * <br></br>
     *  * Checks if Object equals the class. Returns true when equal and continues otherwise
     *  * Checks if the Object is an instance of a PlaceholderExpansion. Returns false if not
     *  * Checks if the Object's Identifier, Author and version equal the one of this class
     *
     *
     * @param other The Object to check
     * @return true or false depending on the above-mentioned checks
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlaceholderExpansion) return false
        return identifier == other.identifier
                && author == other.author
                && version == other.version
    }

    /**
     * Returns a String containing the Expansion's name, author and version
     *
     * @return String containing name, author and version of the expansion
     */
    override fun toString() = "PlaceholderExpansion[name: '$name', author: '$author', version: '$version', type: '$expansionType']"

    // === Deprecated API ===

    /**
     * @return The description of the expansion.
     */
    @Deprecated("As of versions greater than 2.8.7, use the expansion cloud to show a description")
    @ApiStatus.ScheduledForRemoval(inVersion = "2.13.0")
    open val description: String? = null

    /**
     * @return The link for the expansion.
     */
    @Deprecated("As of versions greater than 2.8.7, use the expansion cloud to display a link")
    @ApiStatus.ScheduledForRemoval(inVersion = "2.13.0")
    open val link: String? = null

    enum class Type {
        /**
         * An expansion provided by a plugin is considered internal
         */
        INTERNAL,

        /**
         * An expansion loaded from the expansions folder is considered external
         */
        EXTERNAL
    }
}
