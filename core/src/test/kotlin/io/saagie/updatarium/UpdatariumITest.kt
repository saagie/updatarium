package io.saagie.updatarium

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
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import io.saagie.updatarium.config.UpdatariumConfiguration
import io.saagie.updatarium.model.UpdatariumError
import io.saagie.updatarium.model.changeLog
import io.saagie.updatarium.persist.TestPersistEngine
import java.nio.file.Files
import java.nio.file.Paths
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Updatarium Integration Tests")
class UpdatariumITest {

    val resourcesPath = Paths.get(UpdatariumITest::class.java.getResource("/changelogs").path)
    val changelogPath = Paths.get(UpdatariumITest::class.java.getResource("/changelogs/changelog.kts").path)
    val changelogWithTagPath =
        Paths.get(UpdatariumITest::class.java.getResource("/changelogs/changelog_with_tags.kts").path)
    val failedChangelogPath =
        Paths.get(UpdatariumITest::class.java.getResource("/changelogs/failed_changelog.kts").path)

    fun getConfig() = UpdatariumConfiguration(
        dryRun = false,
        persistEngine = TestPersistEngine(),
        listFilesRecursively = true
    )

    @Test
    fun `should correctly execute a very simple changelog`() {
        with(getConfig()) {
            Updatarium(this)
                .executeChangeLog(
                    """
        import io.saagie.updatarium.model.changeLog

        changeLog {
            changeSet(id = "ChangeSet-1", author = "Hello World") {
                action { logger.info {"Hello world"} }
            }
        }
    """.trimIndent()
                )
            assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(1)
            assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                .extracting { it.executionId }
                .containsExactly("ChangeSet-1")
        }
    }

    @Test
    fun `should correctly execute a simple changelog with multiple actions`() {

        with(getConfig()) {
            Updatarium(this)
                .executeChangeLog(
                    """
        import io.saagie.updatarium.model.changeLog

        changeLog {
            changeSet(id = "ChangeSet-1", author = "Hello World") {
                action { logger.info { "0" } }
                action { logger.info { "1" } }
                action { logger.info { "2" } }
            }
        }
    """.trimIndent()
                )
            assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(1)
            assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                .extracting { it.executionId }
                .containsExactly("ChangeSet-1")
        }
    }

    @Test
    fun `should correctly execute a changelog with multiple changesets&actions`() {

        with(getConfig()) {
            Updatarium(this)
                .executeChangeLog(
                    """
        import io.saagie.updatarium.model.changeLog

        changeLog {
            changeSet(id = "ChangeSet-1", author = "Hello 0") {    
                action { logger.info {"0"} }
            }
            changeSet(id = "ChangeSet-2", author = "Hello 1") {
                action { logger.info {"1"} }
                action { logger.info {"2"} }
            }
        }
    """.trimIndent()
                )
            assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(2)
            assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                .extracting { it.executionId }
                .containsExactly("ChangeSet-1", "ChangeSet-2")
        }
    }

    @Test
    fun `should correctly execute a simple changelog using Path`() {

        // No tags supplied
        with(getConfig()) {
            Updatarium(this).executeChangeLog(changelogPath)
            Updatarium(this).executeChangeLog(changelogWithTagPath)

            assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(2)
            assertThat((this.persistEngine as TestPersistEngine).changeSetTested)
                .containsExactly(
                    "${changelogPath.toAbsolutePath()}_ChangeSet-1",
                    "${changelogWithTagPath.toAbsolutePath()}_ChangeSet-2"
                )
            assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                .extracting { it.executionId }
                .containsExactly(
                    "${changelogPath.toAbsolutePath()}_ChangeSet-1",
                    "${changelogWithTagPath.toAbsolutePath()}_ChangeSet-2"
                )
        }

        // With tags supplied
        with(getConfig()) {
            Updatarium(this)
                .executeChangeLog(
                    changelogPath,
                    "hello"
                )
            Updatarium(this)
                .executeChangeLog(
                    changelogWithTagPath,
                    "hello"
                )
            assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(1)
            assertThat((this.persistEngine as TestPersistEngine).changeSetTested).containsExactly(
                "${changelogWithTagPath.toAbsolutePath()}_ChangeSet-2"
            )
            assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                .extracting { it.executionId }
                .containsExactly("${changelogWithTagPath.toAbsolutePath()}_ChangeSet-2")
        }
    }

    @Test
    fun `should correctly execute a very simple changelog using Reader`() {

        // No tags supplied
        with(getConfig()) {
            Updatarium(this).executeChangeLog(
                Files.newBufferedReader(
                    Paths.get(
                        UpdatariumITest::class.java.getResource(
                            "/changelogs/changelog.kts"
                        ).path
                    )
                )
            )
            Updatarium(this).executeChangeLog(
                Files.newBufferedReader(
                    Paths.get(
                        UpdatariumITest::class.java.getResource("/changelogs/changelog_with_tags.kts").path
                    )
                )
            )
            assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(2)
            assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                .extracting { it.executionId }
                .containsExactly("ChangeSet-1", "ChangeSet-2")
        }

        // With tags supplied
        with(getConfig()) {
            Updatarium(this).executeChangeLog(
                Files.newBufferedReader(
                    Paths.get(
                        UpdatariumITest::class.java.getResource(
                            "/changelogs/changelog.kts"
                        ).path
                    )
                ),
                "hello"
            )
            Updatarium(this).executeChangeLog(
                Files.newBufferedReader(
                    Paths.get(
                        UpdatariumITest::class.java.getResource(
                            "/changelogs/changelog_with_tags.kts"
                        ).path
                    )
                ),
                "hello"
            )
            assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(1)
            assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                .extracting { it.executionId }
                .containsExactly("ChangeSet-2")
        }
    }

    @Test
    fun `should correctly execute a list of changelog`() {

        // No tags supplied
        with(getConfig()) {
            Updatarium(this).executeChangeLogs(
                resourcesPath,
                "changelog(.*).kts"
            )
            assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(2)
            assertThat((this.persistEngine as TestPersistEngine).changeSetTested)
                .containsExactly(
                    "${changelogPath.toAbsolutePath()}_ChangeSet-1",
                    "${changelogWithTagPath.toAbsolutePath()}_ChangeSet-2"
                )
            assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                .extracting { it.executionId }
                .containsExactly(
                    "${changelogPath.toAbsolutePath()}_ChangeSet-1",
                    "${changelogWithTagPath.toAbsolutePath()}_ChangeSet-2"
                )
        }
        // With tags supplied
        with(getConfig()) {
            Updatarium(this).executeChangeLogs(
                resourcesPath,
                "changelog(.*).kts",
                "hello"
            )

            assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(1)
            assertThat((this.persistEngine as TestPersistEngine).changeSetTested)
                .containsExactly(
                    "${changelogWithTagPath.toAbsolutePath()}_ChangeSet-2"
                )
            assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                .extracting { it.executionId }
                .containsExactly("${changelogWithTagPath.toAbsolutePath()}_ChangeSet-2")
        }
    }

    @Test
    fun `should correctly execute a changelog from DSL`() {
        with(getConfig()) {
            Updatarium(this)
                .executeChangeLog(
                    changeLog(id = "Plop") {
                        changeSet(id = "ChangeSet-1", author = "Hello World") {
                            action { logger.info { "0" } }
                            action { logger.info { "1" } }
                            action { logger.info { "2" } }
                        }
                        changeSet(id = "ChangeSet-2", author = "Toto") {
                            action { logger.info { "0" } }
                            action { logger.info { "1" } }
                            action { logger.info { "2" } }
                        }
                    }
                )
            assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(2)
            assertThat((this.persistEngine as TestPersistEngine).changeSetTested)
                .containsExactly("Plop_ChangeSet-1", "Plop_ChangeSet-2")
            assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                .extracting { it.executionId }
                .containsExactly("Plop_ChangeSet-1", "Plop_ChangeSet-2")
        }
    }

    @Test
    fun `should correctly execute a list of changelog that contains forced changeSets`() {
        with(getConfig()) {
            Updatarium(this)
                .executeChangeLog(
                    changeLog(id = "Plop") {
                        changeSet(id = "ChangeSet-1", author = "Hello World") {
                            action { logger.info { "0" } }
                            action { logger.info { "1" } }
                            action { logger.info { "2" } }
                        }
                    }
                )
            // Must not be run (already executed)
            Updatarium(this)
                .executeChangeLog(
                    changeLog(id = "Plop") {
                        changeSet(id = "ChangeSet-1", author = "Hello World") {
                            force = false // by default
                            action { logger.info { "This ChangeSet-1 must not be run as it is already run and not forced" } }
                        }
                    }
                )
            // Must be run (forced)
            Updatarium(this)
                .executeChangeLog(
                    changeLog(id = "Plop") {
                        changeSet(id = "ChangeSet-1", author = "Hello World") {
                            force = true
                            action { logger.info { "ChangeSet-1 already run but forced to run again" } }
                        }
                    }
                )
            assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(2) // Third time is not tested (forced)
            assertThat((this.persistEngine as TestPersistEngine).changeSetTested)
                .containsExactly("Plop_ChangeSet-1", "Plop_ChangeSet-1")
            assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                .extracting { it.executionId }
                .containsExactly("Plop_ChangeSet-1", "Plop_ChangeSet-1")
        }
    }

    @Nested
    inner class ExitCode {

        @Test
        fun should_exit_when_one_changelog_fail_and_failfast() {
            with(getConfig()) {
                try {
                    Updatarium(this).executeChangeLogs(
                        resourcesPath,
                        "failed(.*).kts"
                    )
                } catch (exitError: UpdatariumError.ExitError) {
                    assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(2)
                    assertThat((this.persistEngine as TestPersistEngine).changeSetTested)
                        .containsExactly(
                            "${failedChangelogPath.toAbsolutePath()}_ChangeSet-1",
                            "${failedChangelogPath.toAbsolutePath()}_ChangeSet-2"
                        )
                    assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                        .extracting { "${it.executionId}-${it.status.name}" }
                        .containsExactly(
                            "${failedChangelogPath.toAbsolutePath()}_ChangeSet-1-OK",
                            "${failedChangelogPath.toAbsolutePath()}_ChangeSet-2-FAIL"
                        )
                }
            }
        }

        @Test
        fun should_exit_when_one_changelog_fail_and_no_failfast() {
            with(getConfig().copy(failFast = false)) {
                try {
                    Updatarium(this).executeChangeLogs(
                        resourcesPath,
                        "failed(.*).kts"
                    )
                } catch (exitError: UpdatariumError.ExitError) {
                    assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(3)
                    assertThat((this.persistEngine as TestPersistEngine).changeSetTested)
                        .containsExactly(
                            "${failedChangelogPath.toAbsolutePath()}_ChangeSet-1",
                            "${failedChangelogPath.toAbsolutePath()}_ChangeSet-2",
                            "${failedChangelogPath.toAbsolutePath()}_ChangeSet-3"
                        )
                    assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                        .extracting { "${it.executionId}-${it.status.name}" }
                        .containsExactly(
                            "${failedChangelogPath.toAbsolutePath()}_ChangeSet-1-OK",
                            "${failedChangelogPath.toAbsolutePath()}_ChangeSet-2-FAIL",
                            "${failedChangelogPath.toAbsolutePath()}_ChangeSet-3-OK"
                        )
                }
            }
        }

        @Test
        fun should_exit_when_a_changelog_fail_and_failfast() {
            with(getConfig()) {
                try {
                    Updatarium(this).executeChangeLog(failedChangelogPath)
                } catch (exitError: UpdatariumError.ExitError) {
                    assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(2)
                    assertThat((this.persistEngine as TestPersistEngine).changeSetTested)
                        .containsExactly(
                            "${failedChangelogPath.toAbsolutePath()}_ChangeSet-1",
                            "${failedChangelogPath.toAbsolutePath()}_ChangeSet-2"
                        )
                    assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                        .extracting { "${it.executionId}-${it.status.name}" }
                        .containsExactly(
                            "${failedChangelogPath.toAbsolutePath()}_ChangeSet-1-OK",
                            "${failedChangelogPath.toAbsolutePath()}_ChangeSet-2-FAIL"
                        )
                }
            }
        }

        @Test
        fun should_exit_when_a_changelog_fail_and_no_failfast() {
            with(getConfig().copy(failFast = false)) {
                try {
                    Updatarium(this).executeChangeLog(failedChangelogPath)
                } catch (exitError: UpdatariumError.ExitError) {
                    assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(3)
                    assertThat((this.persistEngine as TestPersistEngine).changeSetTested)
                        .containsExactly(
                            "${failedChangelogPath.toAbsolutePath()}_ChangeSet-1",
                            "${failedChangelogPath.toAbsolutePath()}_ChangeSet-2",
                            "${failedChangelogPath.toAbsolutePath()}_ChangeSet-3"
                        )
                    assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                        .extracting { "${it.executionId}-${it.status.name}" }
                        .containsExactly(
                            "${failedChangelogPath.toAbsolutePath()}_ChangeSet-1-OK",
                            "${failedChangelogPath.toAbsolutePath()}_ChangeSet-2-FAIL",
                            "${failedChangelogPath.toAbsolutePath()}_ChangeSet-3-OK"
                        )
                }
            }
        }
    }

    @Nested
    inner class ListFilesRecursivelyTests {

        val resourcesPath01 = Paths.get(UpdatariumITest::class.java.getResource("/01").path)
        val changelogPath01 = Paths.get(UpdatariumITest::class.java.getResource("/01/01-changelog.kts").path)
        val changelogWithTagPath02 =
            Paths.get(UpdatariumITest::class.java.getResource("/01/02/02-changelog_with_tags.kts").path)

        @Test
        fun `should use the correct way to list files`() {
            // with listFilesRecursively
            with(getConfig()) {
                Updatarium(this).executeChangeLogs(
                    resourcesPath01,
                    "0(.*)-changelog(.*).kts"
                )
                assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(2)
                assertThat((this.persistEngine as TestPersistEngine).changeSetTested)
                    .containsExactly(
                        "${changelogPath01.toAbsolutePath()}_ChangeSet-1",
                        "${changelogWithTagPath02.toAbsolutePath()}_ChangeSet-2"
                    )
                assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked.map { it.executionId })
                    .containsExactly(
                        "${changelogPath01.toAbsolutePath()}_ChangeSet-1",
                        "${changelogWithTagPath02.toAbsolutePath()}_ChangeSet-2"
                    )
            }

            // Not listFilesRecursively
            with(getConfig().copy(listFilesRecursively = false)) {
                Updatarium(this).executeChangeLogs(
                    resourcesPath01,
                    "0(.*)-changelog(.*).kts"
                )
                assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(1)
                assertThat((this.persistEngine as TestPersistEngine).changeSetTested)
                    .containsExactly(
                        "${changelogPath01.toAbsolutePath()}_ChangeSet-1"
                    )
                assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                    .extracting { it.executionId }
                    .containsExactly("${changelogPath01.toAbsolutePath()}_ChangeSet-1")
            }
        }

        @Test
        fun `should return 1 if configuration_listFilesRecursively is set at false`() {
            val maxDepth = Updatarium(getConfig().copy(listFilesRecursively = false)).generateMaxDepth()
            assertThat(maxDepth).isEqualTo(1)
        }

        @Test
        fun `should return Int_MAX_VALUE if configuration_listFilesRecursively is set at true`() {
            val maxDepth = Updatarium(getConfig().copy(listFilesRecursively = true)).generateMaxDepth()
            assertThat(maxDepth).isEqualTo(Int.MAX_VALUE)
        }
    }
}
