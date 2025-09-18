/*
 * MIT License
 *
 * Copyright (c) 2023 Sevastjan
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package me.clip.placeholderapi.scheduler.folia

import me.clip.placeholderapi.scheduler.scheduling.schedulers.TaskScheduler
import me.clip.placeholderapi.scheduler.scheduling.tasks.MyScheduledTask
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import java.util.concurrent.TimeUnit

open class FoliaScheduler(val plugin: Plugin) : TaskScheduler {

    private val regionScheduler = plugin.server.regionScheduler
    private val globalRegionScheduler = plugin.server.globalRegionScheduler
    private val asyncScheduler = plugin.server.asyncScheduler

    override fun isGlobalThread() = plugin.server.isGlobalTickThread

    override fun isTickThread() = plugin.server.isPrimaryThread // The Paper implementation checks whether this is a tick thread, this method exists to avoid confusion.

    override fun isEntityThread(entity: Entity) = plugin.server.isOwnedByCurrentRegion(entity)

    override fun isRegionThread(location: Location) = plugin.server.isOwnedByCurrentRegion(location)

    override fun runTask(runnable: Runnable): = FoliaScheduledTask(globalRegionScheduler.run(plugin) { runnable.run() })

    override fun runTaskLater(runnable: Runnable, delay: Long): MyScheduledTask {
        //Folia exception: Delay ticks may not be <= 0
        return if (delay <= 0) runTask(runnable)
        else FoliaScheduledTask(globalRegionScheduler.runDelayed(plugin, { runnable.run() }, delay))
    }

    override fun runTaskTimer(runnable: Runnable, delay: Long, period: Long): MyScheduledTask {
        //Folia exception: Delay ticks may not be <= 0
        var delay = getOneIfNotPositive(delay)
        return FoliaScheduledTask(
            globalRegionScheduler.runAtFixedRate(
                plugin,
                { runnable.run() },
                delay,
                period
            )
        )
    }

    override fun runTask(plugin: Plugin, runnable: Runnable) = FoliaScheduledTask(globalRegionScheduler.run(plugin, { runnable.run() }))

    override fun runTaskLater(plugin: Plugin, runnable: Runnable, delay: Long): MyScheduledTask {
        //Folia exception: Delay ticks may not be <= 0
        return if (delay <= 0) runTask(plugin, runnable)
        else FoliaScheduledTask(globalRegionScheduler.runDelayed(plugin, { runnable.run() }, delay))
    }

    override fun runTaskTimer(plugin: Plugin, runnable: Runnable, delay: Long, period: Long): MyScheduledTask {
        //Folia exception: Delay ticks may not be <= 0
        var delay = getOneIfNotPositive(delay)
        return FoliaScheduledTask(
            globalRegionScheduler.runAtFixedRate(
                plugin,
                { runnable.run() },
                delay,
                period
            )
        )
    }

    override fun runTask(location: Location, runnable: Runnable) = FoliaScheduledTask(regionScheduler.run(plugin, location, { runnable.run() }))

    override fun runTaskLater(location: Location, runnable: Runnable, delay: Long): MyScheduledTask {
        //Folia exception: Delay ticks may not be <= 0
        return if (delay <= 0) runTask(runnable)
        else FoliaScheduledTask(regionScheduler.runDelayed(plugin, location, { runnable.run() }, delay))
    }

    override fun runTaskTimer(location: Location, runnable: Runnable, delay: Long, period: Long): MyScheduledTask {
        //Folia exception: Delay ticks may not be <= 0
        var delay = getOneIfNotPositive(delay)
        return FoliaScheduledTask(
            regionScheduler.runAtFixedRate(
                plugin,
                location,
                { runnable.run() },
                delay,
                period
            )
        )
    }

    override fun runTask(entity: Entity, runnable: Runnable) = FoliaScheduledTask(entity.scheduler.run(plugin, { runnable.run() }, null))

    override fun runTaskLater(entity: Entity, runnable: Runnable, delay: Long): MyScheduledTask {
        //Folia exception: Delay ticks may not be <= 0
        return if (delay <= 0) runTask(entity, runnable)
        else FoliaScheduledTask(entity.scheduler.runDelayed(plugin, { runnable.run() }, null, delay))
    }

    override fun runTaskTimer(entity: Entity, runnable: Runnable, delay: Long, period: Long): MyScheduledTask {
        //Folia exception: Delay ticks may not be <= 0
        var delay = getOneIfNotPositive(delay)
        return FoliaScheduledTask(entity.scheduler.runAtFixedRate(plugin, { runnable.run() }, null, delay, period))
    }

    override fun runTaskAsynchronously(runnable: Runnable) = FoliaScheduledTask(asyncScheduler.runNow(plugin, { runnable.run() }))

    override fun runTaskLaterAsynchronously(runnable: Runnable, delay: Long): MyScheduledTask {
        //Folia exception: Delay ticks may not be <= 0
        var delay = getOneIfNotPositive(delay)
        return FoliaScheduledTask(
            asyncScheduler.runDelayed(
                plugin,
                { runnable.run() },
                delay * 50L,
                TimeUnit.MILLISECONDS
            )
        )
    }

    override fun runTaskTimerAsynchronously(runnable: Runnable, delay: Long, period: Long) = FoliaScheduledTask(
        asyncScheduler.runAtFixedRate(
            plugin,
            { runnable.run() },
            delay * 50,
            period * 50,
            TimeUnit.MILLISECONDS
        )
    )

    @Deprecated("")
    override fun runTaskAsynchronously(plugin: Plugin, runnable: Runnable) = FoliaScheduledTask(asyncScheduler.runNow(plugin, { runnable.run() }))

    @Deprecated("")
    override fun runTaskLaterAsynchronously(plugin: Plugin, runnable: Runnable, delay: Long): MyScheduledTask {
        //Folia exception: Delay ticks may not be <= 0
        var delay = delay
        delay = getOneIfNotPositive(delay)
        return FoliaScheduledTask(
            asyncScheduler.runDelayed(
                plugin,
                { runnable.run() },
                delay * 50L,
                TimeUnit.MILLISECONDS
            )
        )
    }

    @Deprecated("")
    override fun runTaskTimerAsynchronously(
        plugin: Plugin,
        runnable: Runnable,
        delay: Long,
        period: Long
    ): MyScheduledTask {
        //Folia exception: Delay ticks may not be <= 0
        var delay = delay
        delay = getOneIfNotPositive(delay)
        return FoliaScheduledTask(
            asyncScheduler.runAtFixedRate(
                plugin,
                { runnable.run() },
                delay * 50,
                period * 50,
                TimeUnit.MILLISECONDS
            )
        )
    }

    override fun execute(runnable: Runnable) = globalRegionScheduler.execute(plugin, runnable)

    override fun execute(location: Location, runnable: Runnable) = regionScheduler.execute(plugin, location, runnable)

    override fun execute(entity: Entity, runnable: Runnable) = entity.scheduler.execute(plugin, runnable, null, 1L)

    override fun cancelTasks() {
        globalRegionScheduler.cancelTasks(plugin)
        asyncScheduler.cancelTasks(plugin)
    }

    override fun cancelTasks(plugin: Plugin) {
        globalRegionScheduler.cancelTasks(plugin)
        asyncScheduler.cancelTasks(plugin)
    }

    private fun getOneIfNotPositive(x: Long) = if (x <= 0) 1L else x
}
