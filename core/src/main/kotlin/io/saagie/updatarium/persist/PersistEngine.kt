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

import io.saagie.updatarium.log.InMemoryAppenderAccess
import io.saagie.updatarium.log.InMemoryAppenderManager
import io.saagie.updatarium.model.ChangeSet
import io.saagie.updatarium.model.ExecutionReport
import io.saagie.updatarium.model.ExecutionStatus
import io.saagie.updatarium.persist.model.PageRequest
import mu.KotlinLogging

/**
 * Engine to io.saagie.updatarium.persist all changeSet executions
 *
 * You should use this abstract class to create your own io.saagie.updatarium.persist io.saagie.updatarium.engine using existings functions
 */
abstract class PersistEngine(open val configuration: PersistConfig) {
    protected val logger = KotlinLogging.logger {}

    /**
     * This function will check the connection to the persistence system.
     * If no check can be run, just do nothing.
     */
    abstract fun checkConnection()

    /**
     * This function will return the latest changeSet execution status (by its ID).
     * Return Status.NOT_EXECUTED if the changeSet has never been run, the persisted Status otherwise.
     */
    abstract fun findLatestExecutionStatus(changeSetId: String): ExecutionStatus

    /**
     * This function will return the executions filtered by status and optionally by change set.
     * The result is paginated depending of the page argument.
     */
    abstract fun findExecutions(
        page: PageRequest = PageRequest(),
        filterStatus: Set<ExecutionStatus> = ExecutionStatus.values().toSet(),
        filterChangeSetId: String? = null
    ): List<ExecutionReport>

    /**
     * This function returns the count of changeSet executions.
     */
    abstract fun executionCount(): Int

    /**
     * This function is here to "lock" the changeSet, that's mean store a reference thant the changeSet in the parameter will be executed.
     *
     * It's to be sure, no parallel execution of the same changeSet (using the same persistence io.saagie.updatarium.engine) can be possible.
     */
    protected abstract fun lock(executionId: String, changeSet: ChangeSet)

    /**
     * This function is called after the changeSet execution, so you can now update the changeSet status (in the parameter) and store the logs.
     */
    protected abstract fun unlock(executionId: String, changeSet: ChangeSet, status: ExecutionStatus, logs: List<String>)

    /**
     * Run changeSet code, and possibly lock the changeSet before
     *
     * @param lock if weed need to lock the run
     * @param block the changeSet execution block
     */
    internal fun ChangeSet.runWithPersistEngine(
        executionId: String,
        lock: Boolean,
        block: ChangeSet.() -> Unit
    ): Throwable? =
        try {
            if (lock) {
                lock(executionId, this)
            }
            InMemoryAppenderManager.record { block() }
            logger.info { "$executionId marked as ${ExecutionStatus.OK}" }
            if (lock) {
                val logs = InMemoryAppenderAccess.getEvents(persistConfig = configuration, success = true)
                unlock(executionId, this, ExecutionStatus.OK, logs)
            }
            null
        } catch (e: Exception) {
            logger.error(e) { "Error during apply update" }
            logger.info { "$executionId marked as ${ExecutionStatus.FAIL}" }
            if (lock) {
                val logs = InMemoryAppenderAccess.getEvents(persistConfig = configuration, success = false)
                unlock(executionId, this, ExecutionStatus.FAIL, logs)
            }
            e
        }
}
