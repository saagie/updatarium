/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2019-2020 Pierre Leresteux.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.saagie.updatarium.log


import io.saagie.updatarium.persist.PersistConfig
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.Appender
import org.apache.logging.log4j.core.Core
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.layout.PatternLayout
import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Logs event in a shared queue.
 */
@Plugin(
    name = "InMemoryAppender",
    category = Core.CATEGORY_NAME,
    elementType = Appender.ELEMENT_TYPE
)
class InMemoryAppender(
    name: String
) : AbstractAppender(name, null, PatternLayout.createDefaultLayout(), false, null) {

    private var enabled = false

    private var persistConfig = PersistConfig()

    private val logEvents = ConcurrentLinkedQueue<InMemoryEvent<Level, LogEvent>>()

    init {
        EventsQueueInstance.instance = this.logEvents
    }

    fun setConfig(persistConfig: PersistConfig) {
        this.persistConfig = persistConfig
    }

    override fun append(event: LogEvent?) {
        if (enabled && event != null) {
            logEvents.add(
                InMemoryEvent(
                    event = event,
                    level = event.level,
                    loggerName = event.loggerName,
                    time = Instant.ofEpochSecond(event.instant.epochSecond, event.instant.nanoOfSecond.toLong()),
                    message = event.message.formattedMessage,
                    exception = event.thrown
                )
            )
        }
    }

    fun enableRecording() {
        enabled = true
    }

    fun disableRecording() {
        enabled = false
    }
}
