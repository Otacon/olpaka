package org.cyanotic.olpaka.core

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.LogLevel

class OlpakaAntilog : Antilog() {

    private val logger = DebugAntilog()
    var currentLevel = LogLevel.INFO

    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?
    ) {
        if(priority.ordinal >= currentLevel.ordinal){
            logger.log(priority, tag, throwable, message)
        }
    }

}