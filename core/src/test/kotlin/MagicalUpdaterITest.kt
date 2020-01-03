import com.github.codemonkeyfactory.test.logging.LoggingSpy
import com.github.codemonkeyfactory.test.logging.junit.LoggingTest
import com.github.codemonkeyfactory.test.logging.junit.LoggingTestSpyManager
import com.github.codemonkeyfactory.test.logging.log4j2.LoggingSpyManagerLog4j2Impl
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.LogEvent
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths

@DisplayName("MagicalUpdater Integration Tests")
class MagicalUpdaterITest {

    @Test
    @LoggingTest
    @LoggingTestSpyManager(LoggingSpyManagerLog4j2Impl::class)
    fun `should correctly execute a very simple changelog`(loggingSpy: LoggingSpy<Level, LogEvent>) {

        loggingSpy.enable()
        MagicalUpdater()
            .executeChangelog(
                """
        import dsl.action.BasicAction
        import dsl.changeSet
        import dsl.changelog

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
    fun `should correctly execute a very simple changelog with multiple actions`(loggingSpy: LoggingSpy<Level, LogEvent>) {

        loggingSpy.enable()
        MagicalUpdater()
            .executeChangelog(
                """
        import dsl.action.BasicAction
        import dsl.changeSet
        import dsl.changelog

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
    fun `should correctly execute a changelog with multiple changesets and actions`(loggingSpy: LoggingSpy<Level, LogEvent>) {

        loggingSpy.enable()
        MagicalUpdater()
            .executeChangelog(
                """
        import dsl.action.BasicAction
        import dsl.changeSet
        import dsl.changelog

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
    fun `should correctly execute a very simple changelog using Path`(loggingSpy: LoggingSpy<Level, LogEvent>) {

        loggingSpy.enable()
        MagicalUpdater().executeChangelog(Paths.get(MagicalUpdaterITest::class.java.getResource("changelog.kts").path))
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
        MagicalUpdater().executeChangelog(
            Files.newBufferedReader(
                Paths.get(
                    MagicalUpdaterITest::class.java.getResource(
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
