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
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

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

            assertThat((config.persistEngine as TestPersistEngine).changeSetTested).containsExactly(
                "changeSet1",
                "changeSet2"
            )
            assertThat(
                (config.persistEngine as TestPersistEngine).changeSetUnLocked
                    .map { it.first.id }).containsExactly("changeSet1", "changeSet2")
            assertThat(
                (config.persistEngine as TestPersistEngine).changeSetUnLocked
                    .map { "${it.first.id}-${it.second}" }).containsExactly(
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
                (config.persistEngine as TestPersistEngine).changeSetUnLocked.map { it.first.id }).containsExactly(
                "changeSet1",
                "changeSet2"
            )
            assertThat(
                (config.persistEngine as TestPersistEngine).changeSetUnLocked
                    .map { "${it.first.id}-${it.second}" }).containsExactly(
                "changeSet1-KO",
                "changeSet2-OK"
            )
        }
    }

    @Test
    fun should_stop_when_an_action_throws_an_exception_and_failfast() {
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

        val config = UpdatariumConfiguration(failFast = true, persistEngine = TestPersistEngine())
        val changelogReport = changelog.execute(config)
        assertThat(changelogReport.changeSetExceptions).hasSize(1)
        with(changelogReport.changeSetExceptions.first()) {
            assertThat(this.changeSet).isEqualTo(changelog.changeSets.first())
            assertThat(this.e!!).hasMessage("Fail in action")
            assertThat((config.persistEngine as TestPersistEngine).changeSetTested).containsExactly(
                "changeSet1"
            )
            assertThat(
                (config.persistEngine as TestPersistEngine).changeSetUnLocked.map { it.first.id }).containsExactly(
                "changeSet1"
            )
            assertThat(
                (config.persistEngine as TestPersistEngine).changeSetUnLocked
                    .map { "${it.first.id}-${it.second}" }).containsExactly("changeSet1-KO")
        }
    }

    @Nested
    inner class MatchedChangeSetsTest {

        @Test
        fun should_returns_all_changeSets_when_no_tag_supplied_and_no_tag_in_changelog() {
            val matchedChangeSets = changelogWithtags(Pair(emptyList(), emptyList())).matchedChangeSets()

            assertThat(matchedChangeSets).hasSize(2)
            assertThat(matchedChangeSets.map { it.id }).containsExactly("changeSet1", "changeSet2")
        }

        @Test
        fun should_returns_all_changeSets_when_no_tag_supplied_and_tags_set_in_changelog() {
            val matchedChangeSets = changelogWithtags(Pair(listOf("before"), listOf("after")))
                .matchedChangeSets()

            assertThat(matchedChangeSets).hasSize(2)
            assertThat(matchedChangeSets.map { it.id }).containsExactly("changeSet1", "changeSet2")
        }

        @Test
        fun should_returns_no_changeSet_when_tag_are_supplied_and_no_tag_in_changelog() {
            val matchedChangeSets = changelogWithtags(Pair(emptyList(), emptyList()))
                .matchedChangeSets(listOf("after"))

            assertThat(matchedChangeSets).isEmpty()
        }

        @Test
        fun should_returns_no_changeSet_when_tag_are_supplied_and_tag_in_changelog_but_not_matched() {
            val matchedChangeSets = changelogWithtags(Pair(listOf("one"), listOf("two")))
                .matchedChangeSets(listOf("three"))

            assertThat(matchedChangeSets).isEmpty()
        }

        @Test
        fun should_returns_changeSet1_when_tag_are_supplied_and_tag_in_changeSet1_matched() {
            val matchedChangeSets = changelogWithtags(Pair(listOf("before"), listOf("after")))
                .matchedChangeSets(listOf("before"))

            assertThat(matchedChangeSets).hasSize(1)
            assertThat(matchedChangeSets.map { it.id }).containsExactly("changeSet1")
        }

        @Test
        fun should_returns_all_changeSets_when_all_tags_matched() {
            val matchedChangeSets = changelogWithtags(Pair(listOf("before"), listOf("after")))
                .matchedChangeSets(listOf("before", "after"))

            assertThat(matchedChangeSets).hasSize(2)
            assertThat(matchedChangeSets.map { it.id }).containsExactly("changeSet1", "changeSet2")
        }

        @Test
        fun should_returns_all_changeSets_with_a_list_of_tags_when_all_tags_matched() {
            val matchedChangeSets = changelogWithtags(Pair(listOf("before"), listOf("before", "after")))
                .matchedChangeSets(listOf("before"))

            assertThat(matchedChangeSets).hasSize(2)
            assertThat(matchedChangeSets.map { it.id }).containsExactly("changeSet1", "changeSet2")
        }
    }

    @Nested
    inner class ForceChangeSetsTest {

        @Test
        fun should_execute_forced_changeSet(){
            val changelog = ChangeLog(
                changeSets = listOf(
                    ChangeSet(id = "changeSet1", author = "test", actions = listOf(noop, noop), force = true)
                )
            )

            val config = UpdatariumConfiguration(persistEngine = TestPersistEngine())
            changelog.execute(config)

            //if force true then not present in changeSetTested
            assertThat((config.persistEngine as TestPersistEngine).changeSetTested).isEmpty()
            assertThat(
                (config.persistEngine as TestPersistEngine).changeSetUnLocked
                    .map { it.first.id }).containsExactly("changeSet1")
            assertThat(
                (config.persistEngine as TestPersistEngine).changeSetUnLocked
                    .map { "${it.first.id}-${it.second}" }).containsExactly(
                "changeSet1-OK"
            )
        }

        @Test
        fun should_execute_again_if_a_changeSet_is_forced(){
            val changelog = ChangeLog(
                changeSets = listOf(
                    ChangeSet(id = "changeSet1", author = "test", actions = listOf(noop, noop)),
                    ChangeSet(id = "changeSet1", author = "test", actions = listOf(noop, noop), force = true)
                )
            )

            val config = UpdatariumConfiguration(persistEngine = TestPersistEngine())
            changelog.execute(config)

            //if force true then not present in changeSetTested
            assertThat((config.persistEngine as TestPersistEngine).changeSetTested).containsExactly(
                "changeSet1"
            )
            assertThat(
                (config.persistEngine as TestPersistEngine).changeSetUnLocked
                    .map { it.first.id }).containsExactly("changeSet1", "changeSet1")
            assertThat(
                (config.persistEngine as TestPersistEngine).changeSetUnLocked
                    .map { "${it.first.id}-${it.second}" }).containsExactly(
                "changeSet1-OK", "changeSet1-OK"
            )
        }

    }

    fun changelogWithtags(tags: Pair<List<String>, List<String>>) = ChangeLog(
        changeSets = listOf(
            ChangeSet(
                id = "changeSet1",
                author = "test",
                tags = tags.first,
                actions = listOf(noop, noop)
            ),
            ChangeSet(
                id = "changeSet2",
                author = "test",
                tags = tags.second,
                actions = listOf(noop)
            )
        )
    )
}
