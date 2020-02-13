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
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import io.saagie.updatarium.config.UpdatariumConfiguration
import io.saagie.updatarium.persist.TestPersistEngine
import org.junit.jupiter.api.Test

class ChangeSetTest {

    @Test
    fun should_execute_all_actions_if_no_error() {
        val actionRecord = mutableListOf<String>()
        val changeset = ChangeSet(
            id = "changeset1",
            author = "test",
            actions = listOf(
                Action { actionRecord.add("action1") },
                Action { actionRecord.add("action2") },
                Action { actionRecord.add("action3") },
                Action { actionRecord.add("action4") }
            )
        )
        val config = UpdatariumConfiguration(persistEngine = TestPersistEngine(), listFilesRecursively = true)
        changeset.execute(config)

        assertThat(actionRecord)
            .hasSize(4)
        assertThat(actionRecord)
            .containsExactly("action1", "action2", "action3", "action4")
        assertThat((config.persistEngine as TestPersistEngine).changeSetUnLocked.filter { it.first == changeset }
            .first().second).isEqualTo(Status.OK)
    }

    @Test
    fun should_stop_action_if_error() {
        val actionRecord = mutableListOf<String>()
        val changeset = ChangeSet(
            id = "changeset1",
            author = "test",
            actions = listOf(
                Action { actionRecord.add("action1") },
                Action { actionRecord.add("action2") },
                Action { throw IllegalStateException() },
                Action { actionRecord.add("action4") }
            )
        )
        val config = UpdatariumConfiguration(
            failFast = false,
            persistEngine = TestPersistEngine(),
            listFilesRecursively = true
        )
        changeset.execute(config)

        assertThat(actionRecord)
            .hasSize(2)
        assertThat(actionRecord)
            .containsExactly("action1", "action2")
        assertThat((config.persistEngine as TestPersistEngine).changeSetUnLocked.filter { it.first == changeset }
            .first().second).isEqualTo(Status.KO)
    }

    @Test
    fun should_run_nothing_when_dryrun_is_activated() {
        val actionRecord = mutableListOf<String>()
        val changeset = ChangeSet(
            id = "changeset1",
            author = "test",
            actions = listOf(
                Action { actionRecord.add("action1") },
                Action { actionRecord.add("action2") },
                Action { actionRecord.add("action3") },
                Action { actionRecord.add("action4") }
            )
        )
        val config =
            UpdatariumConfiguration(dryRun = true, persistEngine = TestPersistEngine(), listFilesRecursively = true)
        changeset.execute(config)

        assertThat(actionRecord).isEmpty()
        assertThat((config.persistEngine as TestPersistEngine).changeSetUnLocked).isEmpty()
    }

}
