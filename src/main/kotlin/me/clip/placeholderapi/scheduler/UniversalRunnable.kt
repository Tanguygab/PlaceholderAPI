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
package me.clip.placeholderapi.scheduler

import me.clip.placeholderapi.scheduler.scheduling.tasks.MyScheduledTask
import org.bukkit.plugin.Plugin

/** Just modified BukkitRunnable  */
abstract class UniversalRunnable : Runnable {
    var task: MyScheduledTask? = null

    @Synchronized
    @Throws(IllegalStateException::class)
    fun cancel() {
        checkScheduled()
        task?.cancel()
    }

    /**
     * Returns true if this task has been cancelled.
     *
     * @return true if the task has been cancelled
     * @throws IllegalStateException if task was not scheduled yet
     */
    @Throws(IllegalStateException::class)
    @Synchronized
    fun isCancelled(): Boolean {
        checkScheduled()
        return task?.isCancelled()
    }

    /**
     * Schedules this in the Bukkit scheduler to run on next tick.
     *
     * @param plugin the reference to the plugin scheduling task
     * @return [MyScheduledTask]
     * @throws IllegalArgumentException if plugin is null
     * @throws IllegalStateException    if this was already scheduled
     * @see TaskScheduler.runTask
     */
    @Synchronized
    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun runTask(plugin: Plugin): MyScheduledTask {
        checkNotYetScheduled()
        return setupTask(UniversalScheduler.getScheduler(plugin).runTask(this))
    }

    /**
     * **Asynchronous tasks should never access any API in Bukkit. Great care
     * should be taken to assure the thread-safety of asynchronous tasks.**
     *
     *
     * Schedules this in the Bukkit scheduler to run asynchronously.
     *
     * @param plugin the reference to the plugin scheduling task
     * @return [MyScheduledTask]
     * @throws IllegalArgumentException if plugin is null
     * @throws IllegalStateException    if this was already scheduled
     * @see TaskScheduler.runTaskAsynchronously
     */
    @Synchronized
    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun runTaskAsynchronously(plugin: Plugin): MyScheduledTask {
        checkNotYetScheduled()
        return setupTask(UniversalScheduler.getScheduler(plugin).runTaskAsynchronously(this))
    }

    /**
     * Schedules this to run after the specified number of server ticks.
     *
     * @param plugin the reference to the plugin scheduling task
     * @param delay  the ticks to wait before running the task
     * @return [MyScheduledTask]
     * @throws IllegalArgumentException if plugin is null
     * @throws IllegalStateException    if this was already scheduled
     * @see TaskScheduler.runTaskLater
     */
    @Synchronized
    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun runTaskLater(plugin: Plugin, delay: Long): MyScheduledTask {
        checkNotYetScheduled()
        return setupTask(UniversalScheduler.getScheduler(plugin).runTaskLater(this, delay))
    }

    /**
     * **Asynchronous tasks should never access any API in Bukkit. Great care
     * should be taken to assure the thread-safety of asynchronous tasks.**
     *
     *
     * Schedules this to run asynchronously after the specified number of
     * server ticks.
     *
     * @param plugin the reference to the plugin scheduling task
     * @param delay  the ticks to wait before running the task
     * @return [MyScheduledTask]
     * @throws IllegalArgumentException if plugin is null
     * @throws IllegalStateException    if this was already scheduled
     * @see TaskScheduler.runTaskLaterAsynchronously
     */
    @Synchronized
    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun runTaskLaterAsynchronously(plugin: Plugin, delay: Long): MyScheduledTask {
        checkNotYetScheduled()
        return setupTask(UniversalScheduler.getScheduler(plugin).runTaskLaterAsynchronously(this, delay))
    }

    /**
     * Schedules this to repeatedly run until cancelled, starting after the
     * specified number of server ticks.
     *
     * @param plugin the reference to the plugin scheduling task
     * @param delay  the ticks to wait before running the task
     * @param period the ticks to wait between runs
     * @return [MyScheduledTask]
     * @throws IllegalArgumentException if plugin is null
     * @throws IllegalStateException    if this was already scheduled
     * @see TaskScheduler.runTaskTimer
     */
    @Synchronized
    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun runTaskTimer(plugin: Plugin, delay: Long, period: Long): MyScheduledTask {
        checkNotYetScheduled()
        return setupTask(UniversalScheduler.getScheduler(plugin).runTaskTimer(this, delay, period))
    }

    /**
     * **Asynchronous tasks should never access any API in Bukkit. Great care
     * should be taken to assure the thread-safety of asynchronous tasks.**
     *
     *
     * Schedules this to repeatedly run asynchronously until cancelled,
     * starting after the specified number of server ticks.
     *
     * @param plugin the reference to the plugin scheduling task
     * @param delay  the ticks to wait before running the task for the first
     * time
     * @param period the ticks to wait between runs
     * @return [MyScheduledTask]
     * @throws IllegalArgumentException if plugin is null
     * @throws IllegalStateException    if this was already scheduled
     * @see TaskScheduler.runTaskTimerAsynchronously
     */
    @Synchronized
    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun runTaskTimerAsynchronously(plugin: Plugin, delay: Long, period: Long): MyScheduledTask {
        checkNotYetScheduled()
        return setupTask(UniversalScheduler.getScheduler(plugin).runTaskTimerAsynchronously(this, delay, period))
    }

    private fun checkScheduled() {
        checkNotNull(task) { "Not scheduled yet" }
    }

    private fun checkNotYetScheduled() {
        check(task == null) { "Already scheduled" }
    }


    private fun setupTask(task: MyScheduledTask): MyScheduledTask {
        this.task = task
        return task
    }
}
