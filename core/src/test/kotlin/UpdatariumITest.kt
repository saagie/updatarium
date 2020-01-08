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
import com.github.codemonkeyfactory.test.logging.LoggingSpy
import com.github.codemonkeyfactory.test.logging.junit.LoggingTest
import com.github.codemonkeyfactory.test.logging.junit.LoggingTestSpyManager
import com.github.codemonkeyfactory.test.logging.log4j2.LoggingSpyManagerLog4j2Impl
import io.saagie.updatarium.Updatarium
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.LogEvent
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths

@DisplayName("Updatarium Integration Tests")
class UpdatariumITest {

    @Test
    @LoggingTest
    @LoggingTestSpyManager(LoggingSpyManagerLog4j2Impl::class)
    fun `should correctly execute a very simple changelog`(loggingSpy: LoggingSpy<Level, LogEvent>) {

        loggingSpy.enable()
        Updatarium()
            .executeChangelog(
                """
        import io.saagie.updatarium.dsl.action.BasicAction
        import io.saagie.updatarium.dsl.changeSet
        import io.saagie.updatarium.dsl.changelog

        changelog {
            changesets {
                +changeSet {
                    id = "ChangeSet-1"
                    author = "Hello World"
                    actions {
                        +BasicAction {basicAction ->
                            basicAction.logger.info {"Hello world"}
                        }
                    }
                }
            }
        }
    """.trimIndent()
            )
        loggingSpy.disable()
        val logs = loggingSpy.getLogs().filter { it.loggerName.endsWith("BasicAction") }
        Assertions.assertEquals(1, logs.size)
        Assertions.assertEquals(Level.INFO, logs.first().level)
        Assertions.assertEquals("Hello world", logs.first().message)
    }

    @Test
    @LoggingTest
    @LoggingTestSpyManager(LoggingSpyManagerLog4j2Impl::class)
    fun `should correctly execute a simple changelog with multiple actions`(loggingSpy: LoggingSpy<Level, LogEvent>) {

        loggingSpy.enable()
        Updatarium()
            .executeChangelog(
                """
        import io.saagie.updatarium.dsl.action.BasicAction
        import io.saagie.updatarium.dsl.changeSet
        import io.saagie.updatarium.dsl.changelog

        changelog {
            changesets {
                +changeSet {
                    id = "ChangeSet-1"
                    author = "Hello World"
                    actions {
                        +BasicAction {basicAction ->
                            basicAction.logger.info {"0"}
                        }
                        +BasicAction {basicAction ->
                            basicAction.logger.info {"1"}
                        }
                        +BasicAction {basicAction ->
                            basicAction.logger.info {"2"}
                        }
                    }
                }
            }
        }
    """.trimIndent()
            )
        loggingSpy.disable()
        val logs = loggingSpy.getLogs().filter { it.loggerName.endsWith("BasicAction") }
        Assertions.assertEquals(3, logs.size)
        logs.forEachIndexed { index, capturedLog ->
            Assertions.assertEquals(Level.INFO, capturedLog.level)
            Assertions.assertEquals("$index", capturedLog.message)
        }

    }

    @Test
    @LoggingTest
    @LoggingTestSpyManager(LoggingSpyManagerLog4j2Impl::class)
    fun `should correctly execute a changelog with multiple changesets&actions`(loggingSpy: LoggingSpy<Level, LogEvent>) {

        loggingSpy.enable()
        Updatarium()
            .executeChangelog(
                """
        import io.saagie.updatarium.dsl.action.BasicAction
        import io.saagie.updatarium.dsl.changeSet
        import io.saagie.updatarium.dsl.changelog

        changelog {
            changesets {
                +changeSet {
                    id = "ChangeSet-1"
                    author = "Hello 0"
                    actions {
                        +BasicAction {basicAction ->
                            basicAction.logger.info {"0"}
                        }
                    }
                }
                +changeSet {
                    id = "ChangeSet-2"
                    author = "Hello 1"
                    actions {
                        +BasicAction {basicAction ->
                            basicAction.logger.info {"1"}
                        }
                        +BasicAction {basicAction ->
                            basicAction.logger.info {"2"}
                        }
                    }
                }
            }
        }
    """.trimIndent()
            )
        loggingSpy.disable()
        val logs = loggingSpy.getLogs().filter { it.loggerName.endsWith("BasicAction") }
        Assertions.assertEquals(3, logs.size)
        logs.forEachIndexed { index, capturedLog ->
            Assertions.assertEquals(Level.INFO, capturedLog.level)
            Assertions.assertEquals("$index", capturedLog.message)
        }

    }

    @Test
    @LoggingTest
    @LoggingTestSpyManager(LoggingSpyManagerLog4j2Impl::class)
    fun `should correctly execute a simple changelog using Path`(loggingSpy: LoggingSpy<Level, LogEvent>) {

        loggingSpy.enable()
        Updatarium().executeChangelog(Paths.get(UpdatariumITest::class.java.getResource("changelog.kts").path))
        loggingSpy.disable()
        val logs = loggingSpy.getLogs().filter { it.loggerName.endsWith("BasicAction") }
        Assertions.assertEquals(1, logs.size)
        Assertions.assertEquals(Level.INFO, logs.first().level)
        Assertions.assertEquals("Hello world", logs.first().message)

    }

    @Test
    @LoggingTest
    @LoggingTestSpyManager(LoggingSpyManagerLog4j2Impl::class)
    fun `should correctly execute a very simple changelog using Reader`(loggingSpy: LoggingSpy<Level, LogEvent>) {

        loggingSpy.enable()
        Updatarium().executeChangelog(
            Files.newBufferedReader(
                Paths.get(
                    UpdatariumITest::class.java.getResource(
                        "changelog.kts"
                    ).path
                )
            )
        )
        loggingSpy.disable()
        val logs = loggingSpy.getLogs().filter { it.loggerName.endsWith("BasicAction") }
        Assertions.assertEquals(1, logs.size)
        Assertions.assertEquals(Level.INFO, logs.first().level)
        Assertions.assertEquals("Hello world", logs.first().message)

    }

}
