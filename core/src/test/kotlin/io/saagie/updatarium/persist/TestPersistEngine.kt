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
package io.saagie.updatarium.persist

import io.saagie.updatarium.dsl.ChangeSet
import io.saagie.updatarium.dsl.Status
import io.saagie.updatarium.log.InMemoryEvent
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.LogEvent

class TestPersistEngine : PersistEngine() {
    val changeSetTested = mutableListOf<String>()
    val changeSetLocked = mutableListOf<ChangeSet>()
    val changeSetUnLocked = mutableListOf<Pair<ChangeSet, Status>>()

    override fun checkConnection() {
    }

    override fun notAlreadyExecuted(changeSetId: String): Boolean {
        changeSetTested.add(changeSetId)
        return true
    }

    override fun lock(changeSet: ChangeSet) {
        changeSetLocked.add(changeSet)
    }

    override fun unlock(changeSet: ChangeSet, status: Status, logs: List<InMemoryEvent<Level, LogEvent>>) {
        changeSetUnLocked.add(Pair(changeSet, status))
    }
}
