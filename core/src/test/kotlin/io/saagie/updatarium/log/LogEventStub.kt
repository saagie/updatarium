/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2019-2022 Creative Data.
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
import org.apache.logging.log4j.Marker
import org.apache.logging.log4j.ThreadContext
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.impl.ThrowableProxy
import org.apache.logging.log4j.core.time.Instant
import org.apache.logging.log4j.message.Message
import org.apache.logging.log4j.util.ReadOnlyStringMap

class LogEventStub(
    private val level: Level,
    private val loggerName: String,
    private val instant: Instant,
    private val message: Message,
    private val thrown: Throwable
) : LogEvent {
    override fun getMessage(): Message = message

    override fun getThreadName(): String {
        TODO("not implemented")
    }

    override fun getMarker(): Marker {
        TODO("not implemented")
    }

    override fun getInstant(): Instant = instant

    override fun getSource(): StackTraceElement {
        TODO("not implemented")
    }

    override fun getNanoTime(): Long {
        TODO("not implemented")
    }

    override fun isIncludeLocation(): Boolean {
        TODO("not implemented")
    }

    override fun getContextMap(): MutableMap<String, String> {
        TODO("not implemented")
    }

    override fun getLoggerName(): String = loggerName

    override fun getThrown(): Throwable = thrown

    override fun setEndOfBatch(endOfBatch: Boolean) {
        TODO("not implemented")
    }

    override fun toImmutable(): LogEvent {
        TODO("not implemented")
    }

    override fun getTimeMillis(): Long {
        TODO("not implemented")
    }

    override fun getLevel(): Level = level

    override fun getThreadPriority(): Int {
        TODO("not implemented")
    }

    override fun getLoggerFqcn(): String {
        TODO("not implemented")
    }

    override fun getContextData(): ReadOnlyStringMap {
        TODO("not implemented")
    }

    override fun getContextStack(): ThreadContext.ContextStack {
        TODO("not implemented")
    }

    override fun getThrownProxy(): ThrowableProxy {
        TODO("not implemented")
    }

    override fun getThreadId(): Long {
        TODO("not implemented")
    }

    override fun isEndOfBatch(): Boolean {
        TODO("not implemented")
    }

    override fun setIncludeLocation(locationRequired: Boolean) {
        TODO("not implemented")
    }
}
