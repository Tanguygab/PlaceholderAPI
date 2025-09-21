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

package me.clip.placeholderapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class MockPlayerPlaceholderExpansion extends PlaceholderExpansion {

    public static final String PLAYER_X = "10";
    public static final String PLAYER_Y = "20";
    public static final String PLAYER_Z = "30";
    public static final String PLAYER_NAME = "Sxtanna";


    @NotNull
    @Override
    public String getIdentifier() {
        return "player";
    }

    @NotNull
    @Override
    public String getAuthor() {
        return "Sxtanna";
    }

    @NotNull
    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(@Nullable final OfflinePlayer player, @NotNull final String params) {
        final String[] parts = params.split("_");
        if (parts.length == 0) {
            return null;
        }

        switch (parts[0]) {
            case "name":
                return PLAYER_NAME;
            case "x":
                return PLAYER_X;
            case "y":
                return PLAYER_Y;
            case "z":
                return PLAYER_Z;
        }

        return null;
    }

}