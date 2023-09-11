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
package io.saagie.updatarium.persist

import io.saagie.updatarium.model.ChangeSet
import io.saagie.updatarium.model.ExecutionStatus
import io.saagie.updatarium.model.ExecutionStatus.NOT_EXECUTED

class TestPersistEngine : PersistEngine(PersistConfig()) {
    data class ChangeSetUnLocked(val executionId: String, val changeSet: ChangeSet, val status: ExecutionStatus)

    val changeSetTested = mutableListOf<String>()
    val changeSetLocked = mutableListOf<ChangeSet>()
    val changeSetUnLocked = mutableListOf<ChangeSetUnLocked>()

    override fun checkConnection() {
    }

    override fun findLatestExecutionStatus(changeSetId: String): ExecutionStatus {
        val notAlreadyExecuted = changeSetId !in changeSetTested
        changeSetTested.add(changeSetId)
        if (notAlreadyExecuted) {
            return NOT_EXECUTED
        }
        return changeSetUnLocked.first { it.executionId == changeSetId }.status
    }

    override fun lock(executionId: String, changeSet: ChangeSet) {
        changeSetLocked.add(changeSet)
    }

    override fun unlock(executionId: String, changeSet: ChangeSet, status: ExecutionStatus, logs: List<String>) {
        changeSetUnLocked.add(ChangeSetUnLocked(executionId, changeSet, status))
    }
}
