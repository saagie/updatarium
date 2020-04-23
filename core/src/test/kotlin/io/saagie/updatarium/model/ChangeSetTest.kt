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
import org.junit.jupiter.api.fail

class ChangeSetTest {

    private val executionId = "parentId"

    @Test
    fun should_execute_all_actions_if_no_error() {
        val actionRecord = mutableListOf<String>()
        val changeSet = ChangeSet(
            id = "changeSet1",
            author = "test",
            actions = listOf(
                Action { actionRecord.add("action1") },
                Action { actionRecord.add("action2") },
                Action { actionRecord.add("action3") },
                Action { actionRecord.add("action4") }
            )
        )
        val config = UpdatariumConfiguration(persistEngine = TestPersistEngine(), listFilesRecursively = true)
        changeSet.execute(executionId, config)

        assertThat(actionRecord)
            .hasSize(4)
        assertThat(actionRecord)
            .containsExactly("action1", "action2", "action3", "action4")
        val changeSetUnLocked = (config.persistEngine as TestPersistEngine).changeSetUnLocked
        val status = changeSetUnLocked.first { it.changeSet == changeSet }.status
        assertThat(status).isEqualTo(ExecutionStatus.OK)
    }

    @Test
    fun should_stop_action_if_error() {
        val actionRecord = mutableListOf<String>()
        val changeSet = ChangeSet(
            id = "changeSet1",
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
        changeSet.execute(executionId, config)

        assertThat(actionRecord)
            .containsExactly("action1", "action2")
        val changeSetUnLocked = (config.persistEngine as TestPersistEngine).changeSetUnLocked
        val status = changeSetUnLocked.first { it.changeSet == changeSet }.status
        assertThat(status).isEqualTo(ExecutionStatus.FAIL)
    }

    @Test
    fun should_run_nothing_when_dryrun_is_activated() {
        val actionRecord = mutableListOf<String>()
        val changeSet = ChangeSet(
            id = "changeSet1",
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
        changeSet.execute(executionId, config)

        assertThat(actionRecord).isEmpty()
        assertThat((config.persistEngine as TestPersistEngine).changeSetUnLocked).isEmpty()
    }

    @Test
    fun should_execute_all_actions_when_marked_as_force_run_even_if_already_executed() {
        val actionRecord = mutableListOf<String>()
        val actionRecordForced = mutableListOf<String>()

        val changeSet = ChangeSet(
            id = "changeSet1",
            author = "test",
            actions = listOf(
                Action { actionRecord.add("action1") },
                Action { actionRecord.add("action2") },
                Action { actionRecord.add("action3") },
                Action { actionRecord.add("action4") }
            )
        )

        val changeSetForced = ChangeSet(
            id = "changeSetForceRun",
            author = "test",
            force = true,
            actions = listOf(
                Action { actionRecordForced.add("action1") },
                Action { actionRecordForced.add("action2") },
                Action { actionRecordForced.add("action3") },
                Action { actionRecordForced.add("action4") }
            )
        )
        val config =
            UpdatariumConfiguration(persistEngine = TestPersistEngine(), listFilesRecursively = true)

        fun executeTwice(changeSet: ChangeSet) = changeSet.run {
            execute(executionId, config)
            execute(executionId, config)
        }

        // Trying to execute both changeSets twice
        executeTwice(changeSet)
        executeTwice(changeSetForced)

        val changeSetsUnLocked = (config.persistEngine as TestPersistEngine).changeSetUnLocked
        val changeSetUnLocked = changeSetsUnLocked.first { it.changeSet == changeSet }
        val changedSetForcedUnlocked = changeSetsUnLocked.first { it.changeSet == changeSetForced }

        assertThat(changeSetUnLocked.status).isEqualTo(ExecutionStatus.OK)
        assertThat(changedSetForcedUnlocked.status).isEqualTo(ExecutionStatus.OK)

        assertThat(changeSetsUnLocked.count { it.changeSet == changeSet }).isEqualTo(1) // Run once
        assertThat(changeSetsUnLocked.count { it.changeSet == changeSetForced })
            .isEqualTo(2) // Run twice as it is forced
    }

    @Test
    fun should_stop_if_the_changeSet_has_already_been_executed_and_in_error() {
        val actionRecord = mutableListOf<String>()
        val changeSet = ChangeSet(
            id = "changeSet1",
            author = "test",
            actions = listOf(
                Action { actionRecord.add("action1") },
                Action { throw IllegalStateException("W00T an error here") }
            )
        )
        val config = UpdatariumConfiguration(persistEngine = TestPersistEngine(), listFilesRecursively = true)
        changeSet.execute(executionId, config)

        assertThat(actionRecord).hasSize(1)
        assertThat(actionRecord).containsExactly("action1")
        val changeSetUnLocked = (config.persistEngine as TestPersistEngine).changeSetUnLocked
        val status = changeSetUnLocked.first { it.changeSet == changeSet }.status
        assertThat(status).isEqualTo(ExecutionStatus.FAIL)

        try {
            changeSet.execute(executionId, config)
            fail("Should return an AlreadyExecutedAndInError")
        } catch (alreadyExecutedAndInError: UpdatariumError.AlreadyExecutedAndInError) {
            assertThat(alreadyExecutedAndInError.executionId).isEqualTo("${executionId}_changeSet1")
        }
    }
}
