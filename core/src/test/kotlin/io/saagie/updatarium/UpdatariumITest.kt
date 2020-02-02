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
import io.saagie.updatarium.persist.TestPersistEngine
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths

@DisplayName("Updatarium Integration Tests")
class UpdatariumITest {

    @Test
    fun `should correctly execute a very simple changelog`() {
        with(TestPersistEngine()) {
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
            assertThat(this.changeSetTested).hasSize(1)
            assertThat(this.changeSetUnLocked)
                .extracting { it.first.id }
                .containsExactly("ChangeSet-1")
        }
    }

    @Test
    fun `should correctly execute a simple changelog with multiple actions`() {

        with(TestPersistEngine()) {
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
            assertThat(this.changeSetTested).hasSize(1)
            assertThat(this.changeSetUnLocked)
                .extracting { it.first.id }
                .containsExactly("ChangeSet-1")
        }
    }

    @Test
    fun `should correctly execute a changelog with multiple changesets&actions`() {

        with(TestPersistEngine()) {
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
            assertThat(this.changeSetTested).hasSize(2)
            assertThat(this.changeSetUnLocked)
                .extracting { it.first.id }
                .containsExactly("ChangeSet-1", "ChangeSet-2")
        }
    }

    @Test
    fun `should correctly execute a simple changelog using Path`() {

        // No tags supplied
        with(TestPersistEngine()) {
            Updatarium(this).executeChangelog(
                Paths.get(UpdatariumITest::class.java.getResource("/changelogs/changelog.kts").path)
            )
            Updatarium(this).executeChangelog(
                Paths.get(UpdatariumITest::class.java.getResource("/changelogs/changelog_with_tags.kts").path)
            )
            assertThat(this.changeSetTested).hasSize(2)
            assertThat(this.changeSetUnLocked)
                .extracting { it.first.id }
                .containsExactly("ChangeSet-1", "ChangeSet-2")
        }

        // With tags supplied
        with(TestPersistEngine()) {
            Updatarium(this)
                .executeChangelog(
                    Paths.get(UpdatariumITest::class.java.getResource("/changelogs/changelog.kts").path),
                    "hello"
                )
            Updatarium(this)
                .executeChangelog(
                    Paths.get(UpdatariumITest::class.java.getResource("/changelogs/changelog_with_tags.kts").path),
                    "hello"
                )
            assertThat(this.changeSetTested).hasSize(1)
            assertThat(this.changeSetUnLocked)
                .extracting { it.first.id }
                .containsExactly("ChangeSet-2")
        }

    }

    @Test
    fun `should correctly execute a very simple changelog using Reader`() {

        // No tags supplied
        with(TestPersistEngine()) {
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
            assertThat(this.changeSetTested).hasSize(2)
            assertThat(this.changeSetUnLocked)
                .extracting { it.first.id }
                .containsExactly("ChangeSet-1", "ChangeSet-2")
        }

        // With tags supplied
        with(TestPersistEngine()) {
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
            assertThat(this.changeSetTested).hasSize(1)
            assertThat(this.changeSetUnLocked)
                .extracting { it.first.id }
                .containsExactly("ChangeSet-2")
        }
    }

    @Test
    fun `should correctly execute a list of changelog`() {

        // No tags supplied
        with(TestPersistEngine()) {
            Updatarium(this).executeChangelogs(
                Paths.get(UpdatariumITest::class.java.getResource("/changelogs").path),
                "changelog(.*).kts"
            )
            assertThat(this.changeSetTested).hasSize(2)
            assertThat(this.changeSetUnLocked)
                .extracting { it.first.id }
                .containsExactly("ChangeSet-1", "ChangeSet-2")
        }
        // With tags supplied
        with(TestPersistEngine()) {
            Updatarium(this).executeChangelogs(
                Paths.get(UpdatariumITest::class.java.getResource("/changelogs").path),
                "changelog(.*).kts",
                "hello"
            )

            assertThat(this.changeSetTested).hasSize(1)
            assertThat(this.changeSetUnLocked)
                .extracting { it.first.id }
                .containsExactly("ChangeSet-2")
        }
    }
}
