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
import assertk.assertions.containsExactly
import io.saagie.updatarium.dsl.action.BasicAction
import io.saagie.updatarium.persist.TestPersistEngine
import org.junit.jupiter.api.Test


class ChangelogTest {

    @Test
    fun should_execute_all_changesets_if_no_error() {
        val changelog = Changelog().apply {
            changesets = listOf(
                ChangeSet(
                    id = "changeset1", author = "test", actions = listOf(
                        BasicAction {  },
                        BasicAction {  }
                    )
                ),
                ChangeSet(
                    id = "changeset2", author = "test", actions = listOf(
                        BasicAction { }
                    )
                )
            )
        }

        val engine = TestPersistEngine()
        changelog.execute(engine)

        assertThat(engine.changeSetTested).containsExactly("changeset1", "changeset2")
        assertThat(engine.changeSetUnLocked.map { it.first.id }).containsExactly("changeset1", "changeset2")
        assertThat(engine.changeSetUnLocked.map { "${it.first.id}-${it.second}" }).containsExactly(
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
                        BasicAction {  },
                        BasicAction { throw IllegalStateException("Fail in action") }
                    )
                ),
                ChangeSet(
                    id = "changeset2", author = "test", actions = listOf(
                        BasicAction {  }
                    )
                )
            )
        }

        val engine = TestPersistEngine()
        changelog.execute(engine)

        assertThat(engine.changeSetTested).containsExactly("changeset1", "changeset2")
        assertThat(engine.changeSetUnLocked.map { "${it.first.id}" }).containsExactly("changeset1", "changeset2")
        assertThat(engine.changeSetUnLocked.map { "${it.first.id}-${it.second}" }).containsExactly(
            "changeset1-KO",
            "changeset2-OK"
        )
    }
}
