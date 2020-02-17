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
package io.saagie.updatarium.dsl

import assertk.assertThat
import assertk.assertions.*
import io.saagie.updatarium.config.UpdatariumConfiguration
import io.saagie.updatarium.dsl.action.BasicAction
import io.saagie.updatarium.persist.TestPersistEngine
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible


class ChangelogTest {

    @Nested
    inner class ExecuteTest {
        @Test
        fun should_execute_all_changesets_if_no_error() {
            val changelog = Changelog().apply {
                changesets = listOf(
                    ChangeSet(
                        id = "changeset1", author = "test", actions = listOf(
                            BasicAction { },
                            BasicAction { }
                        )
                    ),
                    ChangeSet(
                        id = "changeset2", author = "test", actions = listOf(
                            BasicAction { }
                        )
                    )
                )
            }

            val config = UpdatariumConfiguration(persistEngine = TestPersistEngine(), listFilesRecursively = true)
            changelog.execute(config)

            assertThat((config.persistEngine as TestPersistEngine).changeSetTested).containsExactly(
                "changeset1",
                "changeset2"
            )
            assertThat(
                (config.persistEngine as TestPersistEngine).changeSetUnLocked
                    .map { it.first.id }).containsExactly("changeset1", "changeset2")
            assertThat(
                (config.persistEngine as TestPersistEngine).changeSetUnLocked
                    .map { "${it.first.id}-${it.second}" }).containsExactly(
                "changeset1-OK",
                "changeset2-OK"
            )
        }


        @Test
        fun should_stop_when_an_action_throws_an_exception() {
            val changelog = Changelog().apply {
                changesets = listOf(
                    ChangeSet(
                        id = "changeset1", author = "test", actions = listOf(
                            BasicAction { },
                            BasicAction { throw IllegalStateException("Fail in action") }
                        )
                    ),
                    ChangeSet(
                        id = "changeset2", author = "test", actions = listOf(
                            BasicAction { }
                        )
                    )
                )
            }

            val config = UpdatariumConfiguration(
                failfast = false,
                persistEngine = TestPersistEngine(),
                listFilesRecursively = true
            )
            changelog.execute(config)

            assertThat((config.persistEngine as TestPersistEngine).changeSetTested).containsExactly(
                "changeset1",
                "changeset2"
            )
            assertThat(
                (config.persistEngine as TestPersistEngine).changeSetUnLocked.map { "${it.first.id}" }).containsExactly(
                "changeset1",
                "changeset2"
            )
            assertThat(
                (config.persistEngine as TestPersistEngine).changeSetUnLocked
                    .map { "${it.first.id}-${it.second}" }).containsExactly(
                "changeset1-KO",
                "changeset2-OK"
            )
        }
    }

    @Test
    fun should_stop_when_an_action_throws_an_exceptionand_failfast() {
        val changelog = Changelog().apply {
            changesets = listOf(
                ChangeSet(
                    id = "changeset1", author = "test", actions = listOf(
                        BasicAction { },
                        BasicAction { throw IllegalStateException("Fail in action") }
                    )
                ),
                ChangeSet(
                    id = "changeset2", author = "test", actions = listOf(
                        BasicAction { }
                    )
                )
            )
        }

        val config = UpdatariumConfiguration(
            failfast = true,
            persistEngine = TestPersistEngine(),
            listFilesRecursively = true
        )
        val changelogReport = changelog.execute(config)
        assertThat(changelogReport.changeSetException).hasSize(1)
        with(changelogReport.changeSetException.first()) {
            assertThat(this.changeSet).isEqualTo(changelog.changesets.first())
            assertThat(this.e!!).hasMessage("Fail in action")
            assertThat((config.persistEngine as TestPersistEngine).changeSetTested).containsExactly(
                "changeset1"
            )
            assertThat(
                (config.persistEngine as TestPersistEngine).changeSetUnLocked.map { "${it.first.id}" }).containsExactly(
                "changeset1"
            )
            assertThat(
                (config.persistEngine as TestPersistEngine).changeSetUnLocked
                    .map { "${it.first.id}-${it.second}" }).containsExactly("changeset1-KO")
        }
    }

    @Nested
    inner class MatchedChangesetsTest {

        @Test
        fun should_returns_all_changesets_when_no_tag_supplied_and_no_tag_in_changelog() {
            val matchedChangesets = changelogWithtags(Pair(null, null)).matchedChangesets()

            assertThat(matchedChangesets).hasSize(2)
            assertThat(matchedChangesets.map { it.id }).containsExactly("changeset1", "changeset2")
        }

        @Test
        fun should_returns_all_changesets_when_no_tag_supplied_and_tags_set_in_changelog() {
            val matchedChangesets = changelogWithtags(Pair(listOf("before"), listOf("after")))
                .matchedChangesets()

            assertThat(matchedChangesets).hasSize(2)
            assertThat(matchedChangesets.map { it.id }).containsExactly("changeset1", "changeset2")
        }

        @Test
        fun should_returns_no_changeset_when_tag_are_supplied_and_no_tag_in_changelog() {
            val matchedChangesets = changelogWithtags(Pair(null, null))
                .matchedChangesets(listOf("after"))

            assertThat(matchedChangesets).isEmpty()
        }

        @Test
        fun should_returns_no_changeset_when_tag_are_supplied_and_tag_in_changelog_but_not_matched() {
            val matchedChangesets = changelogWithtags(Pair(listOf("one"), listOf("two")))
                .matchedChangesets(listOf("three"))

            assertThat(matchedChangesets).isEmpty()
        }

        @Test
        fun should_returns_changeset1_when_tag_are_supplied_and_tag_in_changeset1_matched() {
            val matchedChangesets = changelogWithtags(Pair(listOf("before"), listOf("after")))
                .matchedChangesets(listOf("before"))

            assertThat(matchedChangesets).hasSize(1)
            assertThat(matchedChangesets.map { it.id }).containsExactly("changeset1")
        }

        @Test
        fun should_returns_all_changesets_when_all_tags_matched() {
            val matchedChangesets = changelogWithtags(Pair(listOf("before"), listOf("after")))
                .matchedChangesets(listOf("before", "after"))

            assertThat(matchedChangesets).hasSize(2)
            assertThat(matchedChangesets.map { it.id }).containsExactly("changeset1", "changeset2")
        }

        @Test
        fun should_returns_all_changesets_with_a_list_of_tags_when_all_tags_matched() {
            val matchedChangesets = changelogWithtags(Pair(listOf("before"), listOf("before", "after")))
                .matchedChangesets(listOf("before"))

            assertThat(matchedChangesets).hasSize(2)
            assertThat(matchedChangesets.map { it.id }).containsExactly("changeset1", "changeset2")
        }
    }

    @Nested
    inner class SetIdTest {

        @Test
        fun should_set_an_id_if_set() {
            val changelog = Changelog()
            assertThat(changelog.getIdValue()).isEmpty()
            changelog.setId("newId")
            assertThat(changelog.getIdValue()).isEqualTo("newId")
        }
    }

    fun changelogWithtags(tags: Pair<List<String>?, List<String>?>) = Changelog().apply {
        changesets = listOf(
            ChangeSet(
                id = "changeset1", author = "test", tags = tags.first, actions = listOf(
                    BasicAction { },
                    BasicAction { }
                )
            ),
            ChangeSet(
                id = "changeset2", author = "test", tags = tags.second, actions = listOf(
                    BasicAction { }
                )
            )
        )
    }

    fun Changelog.getIdValue(): String = this::class.declaredMemberProperties
        .first { it.name == "id" }
        .let {
            it.isAccessible = true
            return (it.getter.call(this)) as String
        }
}
