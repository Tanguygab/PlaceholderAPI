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
 * Implementing this interface allows [PlaceholderExpansions][PlaceholderExpansion]
 * to set a list of default configuration values through the [getDefaults method][.getDefaults]
 * that should be added to the config.yml of PlaceholderAPI.
 *
 *
 * The entries will be added under `expansions` as their own section.
 * <h2>Example:</h2>
 * returning a Map with key `foo` and value `bar` will result in the following config entry:
 *
 * <pre>`
 * expansions:
 * myexpansion:
 * foo: "bar"
`</pre> *
 *
 *
 * **The configuration is set before the PlaceholderExpansion is registered!**
 *
 * @author Ryan McCarthy
 */
interface Configurable {
    /**
     * The map returned by this method will be used to set config options in PlaceholderAPI's config.yml.
     *
     *
     * The key and value pairs are set under a section named after your
     * [PlaceholderExpansion][PlaceholderExpansion] in the
     * `expansions` section of the config.
     *
     * @return Map of config path / values which need to be added / removed from the PlaceholderAPI
     * config.yml file
     */
    val defaults: Map<String, Any?>
}
