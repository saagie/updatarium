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
import io.saagie.updatarium.dsl.UpdatariumError
import io.saagie.updatarium.persist.TestPersistEngine
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths

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
                        +BasicAction {logger.info {"Hello world"}
                        }
                    }
                }
            }
        }
    """.trimIndent()
                )
            assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(1)
            assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                .extracting { it.first.id }
                .containsExactly("ChangeSet-1")
        }
    }

    @Test
    fun `should correctly execute a simple changelog with multiple actions`() {

        with(getConfig()) {
            Updatarium(this)
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
                        +BasicAction {logger.info {"0"}
                        }
                        +BasicAction {logger.info {"1"}
                        }
                        +BasicAction {logger.info {"2"}
                        }
                    }
                }
            }
        }
    """.trimIndent()
                )
            assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(1)
            assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                .extracting { it.first.id }
                .containsExactly("ChangeSet-1")
        }
    }

    @Test
    fun `should correctly execute a changelog with multiple changesets&actions`() {

        with(getConfig()) {
            Updatarium(this)
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
                        +BasicAction { logger.info {"0"}
                        }
                    }
                }
                +changeSet {
                    id = "ChangeSet-2"
                    author = "Hello 1"
                    actions {
                        +BasicAction { logger.info {"1"}
                        }
                        +BasicAction { logger.info {"2"}
                        }
                    }
                }
            }
        }
    """.trimIndent()
                )
            assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(2)
            assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                .extracting { it.first.id }
                .containsExactly("ChangeSet-1", "ChangeSet-2")
        }
    }

    @Test
    fun `should correctly execute a simple changelog using Path`() {

        // No tags supplied
        with(getConfig()) {
            Updatarium(this).executeChangelog(changelogPath)
            Updatarium(this).executeChangelog(changelogWithTagPath)

            assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(2)
            assertThat((this.persistEngine as TestPersistEngine).changeSetTested)
                .containsExactly(
                    "${changelogPath.toAbsolutePath().toString()}_ChangeSet-1",
                    "${changelogWithTagPath.toAbsolutePath().toString()}_ChangeSet-2"
                )
            assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                .extracting { it.first.id }
                .containsExactly("ChangeSet-1", "ChangeSet-2")
        }

        // With tags supplied
        with(getConfig()) {
            Updatarium(this)
                .executeChangelog(
                    changelogPath,
                    "hello"
                )
            Updatarium(this)
                .executeChangelog(
                    changelogWithTagPath,
                    "hello"
                )
            assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(1)
            assertThat((this.persistEngine as TestPersistEngine).changeSetTested).containsExactly(
                "${changelogWithTagPath.toAbsolutePath().toString()}_ChangeSet-2"
            )
            assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                .extracting { it.first.id }
                .containsExactly("ChangeSet-2")
        }
    }

    @Test
    fun `should correctly execute a very simple changelog using Reader`() {

        // No tags supplied
        with(getConfig()) {
            Updatarium(this).executeChangelog(
                Files.newBufferedReader(
                    Paths.get(
                        UpdatariumITest::class.java.getResource(
                            "/changelogs/changelog.kts"
                        ).path
                    )
                )
            )
            Updatarium(this).executeChangelog(
                Files.newBufferedReader(
                    Paths.get(
                        UpdatariumITest::class.java.getResource("/changelogs/changelog_with_tags.kts").path
                    )
                )
            )
            assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(2)
            assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                .extracting { it.first.id }
                .containsExactly("ChangeSet-1", "ChangeSet-2")
        }

        // With tags supplied
        with(getConfig()) {
            Updatarium(this).executeChangelog(
                Files.newBufferedReader(
                    Paths.get(
                        UpdatariumITest::class.java.getResource(
                            "/changelogs/changelog.kts"
                        ).path
                    )
                ),
                "hello"
            )
            Updatarium(this).executeChangelog(
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
                .extracting { it.first.id }
                .containsExactly("ChangeSet-2")
        }
    }

    @Test
    fun `should correctly execute a list of changelog`() {

        // No tags supplied
        with(getConfig()) {
            Updatarium(this).executeChangelogs(
                resourcesPath,
                "changelog(.*).kts"
            )
            assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(2)
            assertThat((this.persistEngine as TestPersistEngine).changeSetTested)
                .containsExactly(
                    "${changelogPath.toAbsolutePath().toString()}_ChangeSet-1",
                    "${changelogWithTagPath.toAbsolutePath().toString()}_ChangeSet-2"
                )
            assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                .extracting { it.first.id }
                .containsExactly("ChangeSet-1", "ChangeSet-2")
        }
        // With tags supplied
        with(getConfig()) {
            Updatarium(this).executeChangelogs(
                resourcesPath,
                "changelog(.*).kts",
                "hello"
            )

            assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(1)
            assertThat((this.persistEngine as TestPersistEngine).changeSetTested)
                .containsExactly(
                    "${changelogWithTagPath.toAbsolutePath().toString()}_ChangeSet-2"
                )
            assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                .extracting { it.first.id }
                .containsExactly("ChangeSet-2")
        }
    }

    @Nested
    inner class ExitCode {

        @Test
        fun should_exit_when_one_changelog_fail_and_failfast() {
            with(getConfig()) {
                try {
                    Updatarium(this).executeChangelogs(
                        resourcesPath,
                        "failed(.*).kts"
                    )
                } catch (exitError: UpdatariumError.ExitError) {
                    assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(2)
                    assertThat((this.persistEngine as TestPersistEngine).changeSetTested)
                        .containsExactly(
                            "${failedChangelogPath.toAbsolutePath().toString()}_ChangeSet-1",
                            "${failedChangelogPath.toAbsolutePath().toString()}_ChangeSet-2"
                        )
                    assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                        .extracting { "${it.first.id}-${it.second.name}" }
                        .containsExactly("ChangeSet-1-OK", "ChangeSet-2-KO")
                }
            }
        }

        @Test
        fun should_exit_when_one_changelog_fail_and_no_failfast() {
            with(getConfig().copy(failfast = false)) {
                try {
                    Updatarium(this).executeChangelogs(
                        resourcesPath,
                        "failed(.*).kts"
                    )
                } catch (exitError: UpdatariumError.ExitError) {
                    assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(3)
                    assertThat((this.persistEngine as TestPersistEngine).changeSetTested)
                        .containsExactly(
                            "${failedChangelogPath.toAbsolutePath().toString()}_ChangeSet-1",
                            "${failedChangelogPath.toAbsolutePath().toString()}_ChangeSet-2",
                            "${failedChangelogPath.toAbsolutePath().toString()}_ChangeSet-3"
                        )
                    assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                        .extracting { "${it.first.id}-${it.second.name}" }
                        .containsExactly("ChangeSet-1-OK", "ChangeSet-2-KO", "ChangeSet-3-OK")
                }
            }
        }

        @Test
        fun should_exit_when_a_changelog_fail_and_failfast() {
            with(getConfig()) {
                try {
                    Updatarium(this).executeChangelog(failedChangelogPath)
                } catch (exitError: UpdatariumError.ExitError) {
                    assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(2)
                    assertThat((this.persistEngine as TestPersistEngine).changeSetTested)
                        .containsExactly(
                            "${failedChangelogPath.toAbsolutePath().toString()}_ChangeSet-1",
                            "${failedChangelogPath.toAbsolutePath().toString()}_ChangeSet-2"
                        )
                    assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                        .extracting { "${it.first.id}-${it.second.name}" }
                        .containsExactly("ChangeSet-1-OK", "ChangeSet-2-KO")
                }
            }
        }

        @Test
        fun should_exit_when_a_changelog_fail_and_no_failfast() {
            with(getConfig().copy(failfast = false)) {
                try {
                    Updatarium(this).executeChangelog(failedChangelogPath)
                } catch (exitError: UpdatariumError.ExitError) {
                    assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(3)
                    assertThat((this.persistEngine as TestPersistEngine).changeSetTested)
                        .containsExactly(
                            "${failedChangelogPath.toAbsolutePath().toString()}_ChangeSet-1",
                            "${failedChangelogPath.toAbsolutePath().toString()}_ChangeSet-2",
                            "${failedChangelogPath.toAbsolutePath().toString()}_ChangeSet-3"
                        )
                    assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                        .extracting { "${it.first.id}-${it.second.name}" }
                        .containsExactly("ChangeSet-1-OK", "ChangeSet-2-KO", "ChangeSet-3-OK")
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
                Updatarium(this).executeChangelogs(
                    resourcesPath01,
                    "0(.*)-changelog(.*).kts"
                )
                assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(2)
                assertThat((this.persistEngine as TestPersistEngine).changeSetTested)
                    .containsExactly(
                        "${changelogPath01.toAbsolutePath().toString()}_ChangeSet-1",
                        "${changelogWithTagPath02.toAbsolutePath().toString()}_ChangeSet-2"
                    )
                assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                    .extracting { it.first.id }
                    .containsExactly("ChangeSet-1", "ChangeSet-2")
            }

            // Not listFilesRecursively
            with(getConfig().copy(listFilesRecursively = false)) {
                Updatarium(this).executeChangelogs(
                    resourcesPath01,
                    "0(.*)-changelog(.*).kts"
                )
                assertThat((this.persistEngine as TestPersistEngine).changeSetTested).hasSize(1)
                assertThat((this.persistEngine as TestPersistEngine).changeSetTested)
                    .containsExactly(
                        "${changelogPath01.toAbsolutePath().toString()}_ChangeSet-1"
                    )
                assertThat((this.persistEngine as TestPersistEngine).changeSetUnLocked)
                    .extracting { it.first.id }
                    .containsExactly("ChangeSet-1")
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
