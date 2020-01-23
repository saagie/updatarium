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

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager.getContext
import org.apache.logging.log4j.core.LoggerContext

/**
 * Manage the lifecycle of the [InMemoryAppender]: setup and teardown
 */
object InMemoryAppenderManager {

    private val inMemoryAppender = InMemoryAppender("memory")

    fun setup() {
        // Start the InMemory Appender
        val context = getContext(false) as LoggerContext
        val configuration = context.configuration
        inMemoryAppender.start()
        configuration.addAppender(inMemoryAppender)

        // Add it to the Root logger
        val rootLoggerConfig = configuration.rootLogger
        rootLoggerConfig.addAppender(inMemoryAppender, Level.ALL, null)
        // rootLoggerConfig.level = Level.ALL
        context.updateLoggers()
    }

    fun tearDown() {
        // Call reconfigure to reset the configuration
        // The drawback to this method is that any programmatic changes done after loading from source are lost
        inMemoryAppender.stop()
        (getContext(false) as LoggerContext).reconfigure()
    }

}

