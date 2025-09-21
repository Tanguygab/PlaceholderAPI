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

import java.io.File
import java.io.IOException
import java.net.URLClassLoader
import java.util.jar.JarEntry
import java.util.jar.JarInputStream

object FileUtil {
    @Throws(IOException::class, ClassNotFoundException::class)
    fun <T> findClass(
        file: File,
        clazz: Class<T>
    ): Class<out T>? {
        if (!file.exists()) return null

        val jar = file.toURI().toURL()
        val loader = URLClassLoader(arrayOf(jar), clazz.getClassLoader())
        val matches = mutableListOf<String>()
        val classes = ArrayList<Class<out T?>?>()

        JarInputStream(jar.openStream()).use {
            var entry: JarEntry?
            while ((it.nextJarEntry.also { entry = it }) != null) {
                val name = entry!!.name
                if (name.isEmpty() || !name.endsWith(".class")) continue

                matches.add(name.substring(0, name.lastIndexOf('.')).replace('/', '.'))
            }
            for (match in matches) {
                try {
                    val loaded = loader.loadClass(match)
                    if (clazz.isAssignableFrom(loaded)) {
                        classes.add(loaded.asSubclass(clazz))
                    }
                } catch (_: NoClassDefFoundError) {}
            }
        }
        if (classes.isEmpty()) {
            loader.close()
            return null
        }
        return classes[0]
    }
}
