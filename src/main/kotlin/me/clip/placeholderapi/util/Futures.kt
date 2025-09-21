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

import me.clip.placeholderapi.PlaceholderAPIPlugin
import org.bukkit.Bukkit
import java.util.concurrent.CompletableFuture
import java.util.stream.Collector
import java.util.stream.Collectors

object Futures {
    fun <T> onMainThread(
        plugin: PlaceholderAPIPlugin,
        future: CompletableFuture<T>,
        consumer: (T, Throwable?) -> Unit
    ) {
        future.whenComplete { value: T, exception: Throwable? ->
            if (Bukkit.isPrimaryThread()) consumer(value, exception)
            else plugin.scheduler.runTask { consumer(value, exception) }
        }
    }

    fun <T> collector(): Collector<CompletableFuture<T>, *, CompletableFuture<List<T>>> {
        return Collectors.collectingAndThen(Collectors.toList()) { of(it) }
    }

    fun <T> of(futures: Collection<CompletableFuture<T>>): CompletableFuture<List<T>> {
        return CompletableFuture.allOf(*futures.toTypedArray()).thenApplyAsync { awaitCompletion(futures) }
    }

    private fun <T> awaitCompletion(futures: Collection<CompletableFuture<T>>) = futures.map { it.join() }
}
