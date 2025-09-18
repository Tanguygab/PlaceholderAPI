package me.clip.placeholderapi.scheduler.utils

object JavaUtil {
    fun classExists(className: String): Boolean {
        return try {
            Class.forName(className)
            true
        } catch (_: ClassNotFoundException) {
            false
        }
    }
}
