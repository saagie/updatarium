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
import io.saagie.updatarium.model.Status
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
     * This function will check that the changeSet (by its ID) have never been run.
     * Return true if the changeSet has never been run, false otherwise.
     */
    abstract fun notAlreadyExecuted(changeSetId: String): Status

    /**
     * This function is here to "lock" the changeSet, that's mean store a reference thant the changeSet in the parameter will be executed.
     *
     * It's to be sure, no parallel execution of the same changeSet (using the same persistence io.saagie.updatarium.engine) can be possible.
     */
    protected abstract fun lock(executionId: String, changeSet: ChangeSet)

    /**
     * This function is called after the changeSet execution, so you can now update the changeSet status (in the parameter) and store the logs.
     */
    protected abstract fun unlock(executionId: String, changeSet: ChangeSet, status: Status, logs: List<String>)

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
            logger.info { "$executionId marked as ${Status.OK}" }
            if (lock) {
                val logs = InMemoryAppenderAccess.getEvents(persistConfig = configuration, success = true)
                unlock(executionId, this, Status.OK, logs)
            }
            null
        } catch (e: Exception) {
            logger.error(e) { "Error during apply update" }
            logger.info { "$executionId marked as ${Status.KO}" }
            if (lock) {
                val logs = InMemoryAppenderAccess.getEvents(persistConfig = configuration, success = false)
                unlock(executionId, this, Status.KO, logs)
            }
            e
        }
}
