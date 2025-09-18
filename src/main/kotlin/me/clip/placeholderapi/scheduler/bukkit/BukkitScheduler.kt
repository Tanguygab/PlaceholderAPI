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
package me.clip.placeholderapi.scheduler.bukkit

import me.clip.placeholderapi.scheduler.scheduling.schedulers.TaskScheduler
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin

class BukkitScheduler(val plugin: Plugin) : TaskScheduler {

    override fun isGlobalThread() = plugin.server.isPrimaryThread

    override fun isEntityThread(entity: Entity) = plugin.server.isPrimaryThread

    override fun isRegionThread(location: Location) = plugin.server.isPrimaryThread

    override fun runTask(runnable: Runnable) = BukkitScheduledTask(plugin.server.scheduler.runTask(plugin, runnable))

    override fun runTaskLater(runnable: Runnable, delay: Long) = BukkitScheduledTask(plugin.server.scheduler.runTaskLater(plugin, runnable, delay))

    override fun runTaskTimer(runnable: Runnable, delay: Long, period: Long) = BukkitScheduledTask(plugin.server.scheduler.runTaskTimer(plugin, runnable, delay, period))

    override fun runTaskAsynchronously(runnable: Runnable) = BukkitScheduledTask(plugin.server.scheduler.runTaskAsynchronously(plugin, runnable))

    override fun runTaskLaterAsynchronously(runnable: Runnable, delay: Long) = BukkitScheduledTask(plugin.server.scheduler.runTaskLaterAsynchronously(plugin, runnable, delay))

    override fun runTaskTimerAsynchronously(runnable: Runnable, delay: Long, period: Long) = BukkitScheduledTask(plugin.server.scheduler.runTaskTimerAsynchronously(plugin, runnable, delay, period))

    //Useless Or...
    @Deprecated("")
    override fun runTask(plugin: Plugin, runnable: Runnable) = BukkitScheduledTask(plugin.server.scheduler.runTask(plugin, runnable))

    @Deprecated("")
    override fun runTaskLater(plugin: Plugin, runnable: Runnable, delay: Long) = BukkitScheduledTask(plugin.server.scheduler.runTaskLater(plugin, runnable, delay))

    @Deprecated("")
    override fun runTaskTimer(plugin: Plugin, runnable: Runnable, delay: Long, period: Long) = BukkitScheduledTask(plugin.server.scheduler.runTaskTimer(plugin, runnable, delay, period))

    @Deprecated("")
    override fun runTaskAsynchronously(plugin: Plugin, runnable: Runnable) = BukkitScheduledTask(plugin.server.scheduler.runTaskAsynchronously(plugin, runnable))

    @Deprecated("")
    override fun runTaskLaterAsynchronously(plugin: Plugin, runnable: Runnable, delay: Long) = BukkitScheduledTask(plugin.server.scheduler.runTaskLaterAsynchronously(plugin, runnable, delay))

    @Deprecated("")
    override fun runTaskTimerAsynchronously(plugin: Plugin, runnable: Runnable, delay: Long, period: Long) = BukkitScheduledTask(plugin.server.scheduler.runTaskTimerAsynchronously(plugin, runnable, delay, period))

    override fun execute(runnable: Runnable) = plugin.server.scheduler.scheduleSyncDelayedTask(plugin, runnable)

    override fun cancelTasks() = plugin.server.scheduler.cancelTasks(plugin)

    override fun cancelTasks(plugin: Plugin) = plugin.server.scheduler.cancelTasks(plugin)
}
