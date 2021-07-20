package tigase.halcyon.core.logger.internal

import tigase.halcyon.core.logger.Level
import tigase.halcyon.core.logger.LoggerSPI

actual class DefaultLoggerSPI actual constructor(name: String, enabled: Boolean) :
    LoggerSPI {
    actual override fun isLoggable(level: Level): Boolean {
        TODO("Not yet implemented")
    }

    actual override fun log(
        level: Level,
        msg: String,
        caught: Throwable?
    ) {
    }

}