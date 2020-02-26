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

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import io.saagie.updatarium.persist.PersistConfig
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.time.MutableInstant
import org.apache.logging.log4j.message.FormattedMessage
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class InMemoryAppenderTest {
    val inMemoryAppender = InMemoryAppender("testAppender")

    @Nested
    inner class RecordingAndStoreEventTest {

        @Test
        fun should_enable_recording_change_the_enabled_boolean() {
            // Given
            assertThat(inMemoryAppender.getEnableValue()).isEqualTo(false)
            // When
            inMemoryAppender.enableRecording()
            // Then
            assertThat(inMemoryAppender.getEnableValue()).isEqualTo(true)
        }

        @Test
        fun should_disable_recording_change_the_enabled_boolean() {
            // Given
            should_enable_recording_change_the_enabled_boolean()
            // When
            inMemoryAppender.disableRecording()
            // Then
            assertThat(inMemoryAppender.getEnableValue()).isEqualTo(false)
        }

        @Test
        fun should_not_append_LogEvent_in_the_queue_when_enabled_is_false() {
            // Given
            val logEvent = LogEventStub(
                level = Level.INFO,
                loggerName = "testLogger",
                instant = MutableInstant(),
                message = FormattedMessage("my message"),
                thrown = IllegalStateException("No error")
            )
            // When
            inMemoryAppender.append(logEvent)
            // Then
            val queue = EventsQueueInstance.instance
            assertThat(inMemoryAppender.getEnableValue()).isEqualTo(false)
            assertThat(queue.isEmpty()).isEqualTo(true)
        }

        @Test
        fun should_append_LogEvent_in_the_queue_when_enabled_is_true() {
            // Given
            val logEvent = LogEventStub(
                level = Level.INFO,
                loggerName = "testLogger",
                instant = MutableInstant(),
                message = FormattedMessage("my message"),
                thrown = IllegalStateException("No error")
            )
            // When
            inMemoryAppender.enableRecording()
            inMemoryAppender.append(logEvent)
            // Then
            val queue = EventsQueueInstance.instance
            assertThat(inMemoryAppender.getEnableValue()).isEqualTo(true)
            assertThat(queue.isEmpty()).isEqualTo(false)
            assertThat(queue.element().message).isEqualTo(logEvent.message.formattedMessage)
        }
    }

    @Nested
    @DisplayName("Persist Config tests")
    inner class PersistConfig {

        val config = PersistConfig(
            level = Level.INFO,
            onSuccessStoreLogs = true,
            onErrorStoreLogs = true
        ) { event -> event.message ?: throw IllegalStateException() }

        @Test
        fun should_store_all_logs() {
            // Given
            val logEvents = appendLogs()
            // Then
            val logs = InMemoryAppenderAccess.getEvents()
            assertThat(logs).hasSize(2)
            assertThat(logs.mapNotNull { it.message }).isEqualTo(
                logEvents.map { it.message.formattedMessage }
            )

            val logsError = InMemoryAppenderAccess.getEvents(config, false)
            assertThat(logsError).hasSize(2)
            assertThat(logsError).isEqualTo(
                logEvents.map { it.message.formattedMessage }
            )
            val logsSuccess = InMemoryAppenderAccess.getEvents(config, true)
            assertThat(logsSuccess).hasSize(2)
            assertThat(logsSuccess).isEqualTo(
                logEvents.map { it.message.formattedMessage }
            )
        }

        @Test
        fun should_not_store_error_logs() {
            val logEvents = appendLogs()
            val overridenConfig = config.copy(onErrorStoreLogs = false)
            val logsError = InMemoryAppenderAccess.getEvents(overridenConfig, false)
            assertThat(logsError).isEmpty()
            val logsSuccess = InMemoryAppenderAccess.getEvents(overridenConfig, true)
            assertThat(logsSuccess).hasSize(2)
            assertThat(logsSuccess).isEqualTo(
                logEvents.map { it.message.formattedMessage }
            )
        }

        @Test
        fun should_not_store_success_logs() {
            val logEvents = appendLogs()
            val overridenConfig = config.copy(onSuccessStoreLogs = false)
            val logsError = InMemoryAppenderAccess.getEvents(overridenConfig, false)
            assertThat(logsError).hasSize(2)
            assertThat(logsError).isEqualTo(
                logEvents.map { it.message.formattedMessage }
            )
            val logsSuccess = InMemoryAppenderAccess.getEvents(overridenConfig, true)
            assertThat(logsSuccess).isEmpty()
        }

        @Test
        fun should_not_store_both_logs() {
            appendLogs()
            val overridenConfig = config.copy(onSuccessStoreLogs = false, onErrorStoreLogs = false)
            val logsError = InMemoryAppenderAccess.getEvents(overridenConfig, false)
            assertThat(logsError).isEmpty()
            val logsSuccess = InMemoryAppenderAccess.getEvents(overridenConfig, true)
            assertThat(logsSuccess).isEmpty()
        }

        fun appendLogs(): List<LogEventStub> {
            val logEvent1 = LogEventStub(
                level = Level.INFO,
                loggerName = "testLogger",
                instant = MutableInstant(),
                message = FormattedMessage("INFO message"),
                thrown = IllegalStateException("No error")
            )
            val logEvent2 = LogEventStub(
                level = Level.ERROR,
                loggerName = "testLogger",
                instant = MutableInstant(),
                message = FormattedMessage("ERROR  message"),
                thrown = IllegalStateException("No error")
            )

            inMemoryAppender.enableRecording()
            inMemoryAppender.setConfig(config)
            // When
            inMemoryAppender.append(logEvent1)
            inMemoryAppender.append(logEvent2)

            return listOf(logEvent1, logEvent2)
        }
    }
}

fun InMemoryAppender.getEnableValue(): Boolean = this::class.declaredMemberProperties
    .first { it.name == "enabled" }
    .let {
        it.isAccessible = true
        return (it.getter.call(this)) as Boolean
    }
