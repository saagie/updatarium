/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2019-2023 Creative Data.
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
package io.saagie.updatarium.model

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasMessage
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import io.saagie.updatarium.config.UpdatariumConfiguration
import io.saagie.updatarium.persist.TestPersistEngine
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ChangeLogTest {

    val noop = Action {}

    @Nested
    inner class ExecuteTest {
        @Test
        fun should_execute_all_changeSets_if_no_error() {

            val changelog = ChangeLog(
                changeSets = listOf(
                    ChangeSet(id = "changeSet1", author = "test", actions = listOf(noop, noop)),
                    ChangeSet(id = "changeSet2", author = "test", actions = listOf(noop))
                )
            )

            val config = UpdatariumConfiguration(persistEngine = TestPersistEngine())
            changelog.execute(config)

            assertThat((config.persistEngine as TestPersistEngine).changeSetTested)
                .containsExactly("changeSet1", "changeSet2")
            assertThat(
                (config.persistEngine as TestPersistEngine).changeSetUnLocked
                    .map { it.executionId }).containsExactly("changeSet1", "changeSet2")
            assertThat(
                (config.persistEngine as TestPersistEngine).changeSetUnLocked
                    .map { "${it.executionId}-${it.status}" }).containsExactly(
                "changeSet1-OK",
                "changeSet2-OK"
            )
        }

        @Test
        fun should_stop_when_an_action_throws_an_exception() {
            val changelog = ChangeLog(
                changeSets = listOf(
                    ChangeSet(
                        id = "changeSet1",
                        author = "test",
                        actions = listOf(
                            noop,
                            Action { throw IllegalStateException("Fail in action") }
                        )
                    ),
                    ChangeSet(
                        id = "changeSet2",
                        author = "test",
                        actions = listOf(noop)
                    )
                )
            )

            val config = UpdatariumConfiguration(failFast = false, persistEngine = TestPersistEngine())
            changelog.execute(config)

            assertThat((config.persistEngine as TestPersistEngine).changeSetTested).containsExactly(
                "changeSet1",
                "changeSet2"
            )
            assertThat(
                (config.persistEngine as TestPersistEngine).changeSetUnLocked.map { it.executionId }).containsExactly(
                "changeSet1",
                "changeSet2"
            )
            assertThat(
                (config.persistEngine as TestPersistEngine).changeSetUnLocked
                    .map { "${it.executionId}-${it.status}" }).containsExactly(
                "changeSet1-FAIL",
                "changeSet2-OK"
            )
        }
    }

    @Test
    fun should_stop_when_an_action_throws_an_exception_and_failfast() {
        val changelog = changeLog {
            changeSet(id = "changeSet1", author = "test") {
                action { }
                action { throw IllegalStateException("Fail in action") }
            }
            changeSet(id = "changeSet2", author = "test") {
                action { }
            }
        }

        val config = UpdatariumConfiguration(failFast = true, persistEngine = TestPersistEngine())
        val changelogReport = changelog.execute(config)
        assertThat(changelogReport.changeSetExceptions).hasSize(1)
        with(changelogReport.changeSetExceptions.first()) {
            assertThat(this.changeSet).isEqualTo(changelog.changeSets.first())
            assertThat(this.e ?: throw IllegalStateException()).hasMessage("Fail in action")
            assertThat((config.persistEngine as TestPersistEngine).changeSetTested).containsExactly(
                "changeSet1"
            )
            assertThat(
                (config.persistEngine as TestPersistEngine).changeSetUnLocked.map { it.executionId }).containsExactly(
                "changeSet1"
            )
            assertThat(
                (config.persistEngine as TestPersistEngine).changeSetUnLocked
                    .map { "${it.executionId}-${it.status}" }).containsExactly("changeSet1-FAIL")
        }
    }

    @Nested
    inner class MatchedChangeSetsTest {

        @Test
        fun should_returns_all_changeSets_when_no_tag_supplied_and_no_tag_in_changelog() {
            val changeSet1 =
                ChangeSet(id = "changeSet1", author = "test", actions = listOf(noop, noop), tags = emptyList())
            val changeSet2 = ChangeSet(id = "changeSet2", author = "test", actions = listOf(noop), tags = emptyList())
            val changeLog = ChangeLog(changeSets = listOf(changeSet1, changeSet2))

            val matchedChangeSets = changeLog.matchedChangeSets(targetTags = emptyList())

            assertThat(matchedChangeSets).containsExactly(changeSet1, changeSet2)
        }

        @Test
        fun should_returns_all_changeSets_when_no_tag_supplied_and_tags_set_in_changelog() {
            val changeSet1 =
                ChangeSet(id = "changeSet1", author = "test", actions = listOf(noop, noop), tags = listOf("before"))
            val changeSet2 =
                ChangeSet(id = "changeSet2", author = "test", actions = listOf(noop), tags = listOf("after"))
            val changeLog = ChangeLog(changeSets = listOf(changeSet1, changeSet2))

            val matchedChangeSets = changeLog.matchedChangeSets(targetTags = emptyList())

            assertThat(matchedChangeSets).containsExactly(changeSet1, changeSet2)
        }

        @Test
        fun should_returns_no_changeSet_when_tag_are_supplied_and_no_tag_in_changelog() {
            val changeSet1 =
                ChangeSet(id = "changeSet1", author = "test", actions = listOf(noop, noop), tags = emptyList())
            val changeSet2 = ChangeSet(id = "changeSet2", author = "test", actions = listOf(noop), tags = emptyList())
            val changeLog = ChangeLog(changeSets = listOf(changeSet1, changeSet2))

            val matchedChangeSets = changeLog.matchedChangeSets(targetTags = listOf("after"))

            assertThat(matchedChangeSets).isEmpty()
        }

        @Test
        fun should_returns_no_changeSet_when_tag_are_supplied_and_tag_in_changelog_but_not_matched() {
            val changeSet1 =
                ChangeSet(id = "changeSet1", author = "test", actions = listOf(noop, noop), tags = listOf("before"))
            val changeSet2 =
                ChangeSet(id = "changeSet2", author = "test", actions = listOf(noop), tags = listOf("after"))
            val changeLog = ChangeLog(changeSets = listOf(changeSet1, changeSet2))

            val matchedChangeSets = changeLog.matchedChangeSets(targetTags = listOf("plop"))

            assertThat(matchedChangeSets).isEmpty()
        }

        @Test
        fun should_returns_changeSet1_when_tag_are_supplied_and_tag_in_changeSet1_matched() {
            val changeSet1 =
                ChangeSet(id = "changeSet1", author = "test", actions = listOf(noop, noop), tags = listOf("before"))
            val changeSet2 =
                ChangeSet(id = "changeSet2", author = "test", actions = listOf(noop), tags = listOf("after"))
            val changeLog = ChangeLog(changeSets = listOf(changeSet1, changeSet2))

            val matchedChangeSets = changeLog.matchedChangeSets(targetTags = listOf("before"))

            assertThat(matchedChangeSets).containsExactly(changeSet1)
        }

        @Test
        fun should_returns_all_changeSets_when_all_tags_matched() {
            val changeSet1 =
                ChangeSet(id = "changeSet1", author = "test", actions = listOf(noop, noop), tags = listOf("before"))
            val changeSet2 =
                ChangeSet(id = "changeSet2", author = "test", actions = listOf(noop), tags = listOf("after"))
            val changeLog = ChangeLog(changeSets = listOf(changeSet1, changeSet2))

            val matchedChangeSets = changeLog.matchedChangeSets(targetTags = listOf("before", "after"))

            assertThat(matchedChangeSets).containsExactly(changeSet1, changeSet2)
        }

        @Test
        fun should_returns_all_changeSets_with_a_list_of_tags_when_all_tags_matched() {
            val changeSet1 =
                ChangeSet(id = "changeSet1", author = "test", actions = listOf(noop, noop), tags = listOf("before"))
            val changeSet2 =
                ChangeSet(id = "changeSet2", author = "test", actions = listOf(noop), tags = listOf("before", "after"))
            val changeLog = ChangeLog(changeSets = listOf(changeSet1, changeSet2))

            val matchedChangeSets = changeLog.matchedChangeSets(targetTags = listOf("before"))

            assertThat(matchedChangeSets).containsExactly(changeSet1, changeSet2)
        }
    }
}
