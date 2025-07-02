package alcatraz.common

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class LogLevel(val priority: Int) {
    ERROR(0), WARN(1), INFO(2), DEBUG(3)
}

class Logger(private val moduleName: String = "APP") {

    companion object {
        private val LOG_LEVEL = System.getProperty("LOG_LEVEL")?.let {
            LogLevel.valueOf(it.uppercase())
        } ?: LogLevel.INFO

        private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

        // Default logger instance
        val default = Logger()

        // Factory method for creating module-specific loggers
        fun create(moduleName: String) = Logger(moduleName)
    }

    fun shouldLog(level: LogLevel): Boolean {
        return level.priority <= LOG_LEVEL.priority
    }

    private fun formatMessage(level: LogLevel, message: String, data: Any? = null): String {
        val timestamp = LocalDateTime.now().format(dateFormatter)
        val prefix = "[$timestamp] [${level.name}] [$moduleName]"

        return if (data != null) {
            "$prefix $message | Data: $data"
        } else {
            "$prefix $message"
        }
    }

    fun error(message: String, data: Any? = null) {
        if (shouldLog(LogLevel.ERROR)) {
            System.err.println(formatMessage(LogLevel.ERROR, message, data))
        }
    }

    fun warn(message: String, data: Any? = null) {
        if (shouldLog(LogLevel.WARN)) {
            println(formatMessage(LogLevel.WARN, message, data))
        }
    }

    fun info(message: String, data: Any? = null) {
        if (shouldLog(LogLevel.INFO)) {
            println(formatMessage(LogLevel.INFO, message, data))
        }
    }

    fun debug(message: String, data: Any? = null) {
        if (shouldLog(LogLevel.DEBUG)) {
            println(formatMessage(LogLevel.DEBUG, message, data))
        }
    }

    // Create a child logger with specific module name
    fun child(childModuleName: String): Logger {
        return Logger("$moduleName:$childModuleName")
    }

    // Inline functions for lazy evaluation (only evaluate message if logging)
    inline fun errorLazy(messageProvider: () -> String) {
        if (shouldLog(LogLevel.ERROR)) error(messageProvider())
    }

    inline fun warnLazy(messageProvider: () -> String) {
        if (shouldLog(LogLevel.WARN)) warn(messageProvider())
    }

    inline fun infoLazy(messageProvider: () -> String) {
        if (shouldLog(LogLevel.INFO)) info(messageProvider())
    }

    inline fun debugLazy(messageProvider: () -> String) {
        if (shouldLog(LogLevel.DEBUG)) debug(messageProvider())
    }
}

fun Any.logger(): Logger = Logger.create(this::class.simpleName ?: "UNKNOWN")
