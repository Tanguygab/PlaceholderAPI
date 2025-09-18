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
package me.clip.placeholderapi.scheduler.scheduling.schedulers

import me.clip.placeholderapi.scheduler.scheduling.tasks.MyScheduledTask
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin

import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

interface TaskScheduler {
    /**
     * **Folia**: Returns whether the current thread is ticking the global region <br></br>
     * **Paper & Bukkit**: Returns [org.bukkit.Server.isPrimaryThread]
     */
    fun isGlobalThread(): Boolean

    /**
     * @return [org.bukkit.Server.isPrimaryThread]
     */
    fun isTickThread() = Bukkit.getServer().isPrimaryThread

    /**
     * **Folia & Paper**: Returns whether the current thread is ticking a region and that the region
     * being ticked owns the specified entity. Note that this function is the only appropriate method of
     * checking for ownership of an entity, as retrieving the entity's location is undefined unless the
     * entity is owned by the current region
     *
     *
     * **Bukkit**: returns [org.bukkit.Server.isPrimaryThread]
     *
     * @param entity Specified entity
     */
    fun isEntityThread(entity: Entity): Boolean

    /**
     * **Folia & Paper**: Returns whether the current thread is ticking a region and that the region
     * being ticked owns the chunk at the specified world and block position as included in the specified location
     *
     *
     * **Bukkit**: returns [org.bukkit.Server.isPrimaryThread]
     *
     * @param location Specified location, must have a non-null world.
     */
    fun isRegionThread(location: Location): Boolean

    /**
     * Schedules a task to be executed on the next tick <br></br>
     * **Folia & Paper**: ...on the global region <br></br>
     * **Bukkit**: ...on the main thread
     *
     * @param runnable The task to execute
     */
    fun runTask(runnable: Runnable): MyScheduledTask

    /**
     * Schedules a task to be executed after the specified delay in ticks <br></br>
     * **Folia & Paper**: ...on the global region <br></br>
     * **Bukkit**: ...on the main thread
     *
     * @param runnable The task to execute
     * @param delay    The delay, in ticks
     */
    fun runTaskLater(runnable: Runnable, delay: Long): MyScheduledTask

    /**
     * Schedules a repeating task to be executed after the initial delay with the specified period <br></br>
     * **Folia & Paper**: ...on the global region <br></br>
     * **Bukkit**: ...on the main thread
     *
     * @param runnable The task to execute
     * @param delay    The initial delay, in ticks.
     * @param period   The period, in ticks.
     */
    fun runTaskTimer(runnable: Runnable, delay: Long, period: Long): MyScheduledTask

    /**
     * Deprecated: use [.runTask]
     */
    @Deprecated("")
    fun runTask(plugin: Plugin, runnable: Runnable) = runTask(runnable)

    /**
     * Deprecated: use [.runTaskLater]
     */
    @Deprecated("")
    fun runTaskLater(plugin: Plugin, runnable: Runnable, delay: Long) = runTaskLater(runnable, delay)

    /**
     * Deprecated: use [.runTaskTimer]
     */
    @Deprecated("")
    fun runTaskTimer(plugin: Plugin, runnable: Runnable, delay: Long, period: Long) = runTaskTimer(runnable, delay, period)

    /**
     * **Folia & Paper**: Schedules a task to be executed on the region which owns the location on the next tick
     *
     *
     * **Bukkit**: same as [.runTask]
     *
     * @param location The location which the region executing should own
     * @param runnable The task to execute
     */
    fun runTask(location: Location, runnable: Runnable) = runTask(runnable)

    /**
     * **Folia & Paper**: Schedules a task to be executed on the region which owns the location after the
     * specified delay in ticks
     *
     *
     * **Bukkit**: same as [.runTaskLater]
     *
     * @param location The location which the region executing should own
     * @param runnable The task to execute
     * @param delay    The delay, in ticks.
     */
    fun runTaskLater(location: Location, runnable: Runnable, delay: Long) = runTaskLater(runnable, delay)

    /**
     * **Folia & Paper**: Schedules a repeating task to be executed on the region which owns the location
     * after the initial delay with the specified period
     *
     *
     * **Bukkit**: same as [.runTaskTimer]
     *
     * @param location The location which the region executing should own
     * @param runnable The task to execute
     * @param delay    The initial delay, in ticks.
     * @param period   The period, in ticks.
     */
    fun runTaskTimer(location: Location, runnable: Runnable, delay: Long, period: Long) = runTaskTimer(runnable, delay, period)

    /**
     * Deprecated: use [.runTaskLater]
     */
    @Deprecated("")
    fun scheduleSyncDelayedTask(runnable: Runnable, delay: Long) = runTaskLater(runnable, delay)

    /**
     * Deprecated: use [.execute] or [.runTask]
     */
    @Deprecated("")
    fun scheduleSyncDelayedTask(runnable: Runnable) = runTask(runnable)

    /**
     * Deprecated: use [.runTaskTimer]
     */
    @Deprecated("")
    fun scheduleSyncRepeatingTask(runnable: Runnable, delay: Long, period: Long) = runTaskTimer(runnable, delay, period)

    /**
     * **Folia & Paper**: Schedules a task to be executed on the region which owns the location
     * of given entity on the next tick
     *
     *
     * **Bukkit**: same as [.runTask]
     *
     * @param entity   The entity whose location the region executing should own
     * @param runnable The task to execute
     */
    fun runTask(entity: Entity, runnable: Runnable) = runTask(runnable)

    /**
     * **Folia & Paper**: Schedules a task to be executed on the region which owns the location
     * of given entity after the specified delay in ticks
     *
     *
     * **Bukkit**: same as [.runTaskLater]
     *
     * @param entity   The entity whose location the region executing should own
     * @param runnable The task to execute
     * @param delay    The delay, in ticks.
     */
    fun runTaskLater(entity: Entity, runnable: Runnable, delay: Long) = runTaskLater(runnable, delay)

    /**
     * **Folia & Paper**: Schedules a repeating task to be executed on the region which owns the
     * location of given entity after the initial delay with the specified period
     *
     *
     * **Bukkit**: same as [.runTaskTimer]
     *
     * @param entity   The entity whose location the region executing should own
     * @param runnable The task to execute
     * @param delay    The initial delay, in ticks.
     * @param period   The period, in ticks.
     */
    fun runTaskTimer(entity: Entity, runnable: Runnable, delay: Long, period: Long) = runTaskTimer(runnable, delay, period)

    /**
     * Schedules the specified task to be executed asynchronously immediately
     *
     * @param runnable The task to execute
     * @return The [MyScheduledTask] that represents the scheduled task
     */
    fun runTaskAsynchronously(runnable: Runnable): MyScheduledTask

    /**
     * Schedules the specified task to be executed asynchronously after the time delay has passed
     *
     * @param runnable The task to execute
     * @param delay    The time delay to pass before the task should be executed
     * @return The [MyScheduledTask] that represents the scheduled task
     */
    fun runTaskLaterAsynchronously(runnable: Runnable, delay: Long): MyScheduledTask

    /**
     * Schedules the specified task to be executed asynchronously after the initial delay has passed,
     * and then periodically executed with the specified period
     *
     * @param runnable The task to execute
     * @param delay    The time delay to pass before the first execution of the task, in ticks
     * @param period   The time between task executions after the first execution of the task, in ticks
     * @return The [MyScheduledTask] that represents the scheduled task
     */
    fun runTaskTimerAsynchronously(runnable: Runnable, delay: Long, period: Long): MyScheduledTask

    /**
     * Deprecated: use [.runTaskAsynchronously]
     */
    @Deprecated("")
    fun runTaskAsynchronously(plugin: Plugin, runnable: Runnable) = runTaskAsynchronously(runnable)

    /**
     * Deprecated: use [.runTaskLaterAsynchronously]
     */
    @Deprecated("")
    fun runTaskLaterAsynchronously(plugin: Plugin, runnable: Runnable, delay: Long) = runTaskLaterAsynchronously(runnable, delay)

    /**
     * Deprecated: use [.runTaskTimerAsynchronously]
     */
    @Deprecated("")
    fun runTaskTimerAsynchronously(plugin: Plugin, runnable: Runnable, delay: Long, period: Long) = runTaskTimerAsynchronously(runnable, delay, period)

    /**
     * Calls a method on the main thread and returns a Future object. This task will be executed
     * by the main(Bukkit)/global(Folia&Paper) server thread.
     *
     *
     * Note: The Future.get() methods must NOT be called from the main thread.
     *
     *
     * Note2: There is at least an average of 10ms latency until the isDone() method returns true.
     *
     * @param task Task to be executed
     */
    fun <T> callSyncMethod(task: Callable<T>): Future<T> {
        val completableFuture = CompletableFuture<T>()
        execute { completableFuture.complete(task.call()) }
        return completableFuture
    }

    /**
     * Schedules a task to be executed on the global region
     *
     * @param runnable The task to execute
     */
    fun execute(runnable: Runnable)

    /**
     * Schedules a task to be executed on the region which owns the location
     *
     * @param location The location which the region executing should own
     * @param runnable The task to execute
     */
    fun execute(location: Location, runnable: Runnable) = execute(runnable)

    /**
     * Schedules a task to be executed on the region which owns the location of given entity
     *
     * @param entity   The entity which location the region executing should own
     * @param runnable The task to execute
     */
    fun execute(entity: Entity, runnable: Runnable) = execute(runnable)

    /**
     * Attempts to cancel all tasks scheduled by this plugin
     */
    fun cancelTasks()

    /**
     * Attempts to cancel all tasks scheduled by the specified plugin
     *
     * @param plugin specified plugin
     */
    fun cancelTasks(plugin: Plugin)
}
