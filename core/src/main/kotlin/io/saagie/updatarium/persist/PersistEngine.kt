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

import io.saagie.updatarium.model.ChangeSet
import io.saagie.updatarium.model.Status
import mu.KLoggable

/**
 * Engine to io.saagie.updatarium.persist all changeset executions
 *
 * You should use this abstract class to create your own io.saagie.updatarium.persist io.saagie.updatarium.engine using existings functions
 */
abstract class PersistEngine(open val configuration: PersistConfig) : KLoggable {
    override val logger = logger()
    /**
     * This function will check the connection to the persistence system.
     * If no check can be run, just do nothing.
     */
    abstract fun checkConnection()

    /**
     * This function will check that the changeset (by its ID) have never be ran.
     * Return true if the changeset has never be ran, false otherwise.
     */
    abstract fun notAlreadyExecuted(changeSetId: String): Boolean

    /**
     * This function is here to "lock" the changeset, that's mean store a reference thant the changeset in parameter will be execute.
     *
     * It's to be sure, no parallel execution of the same changeset (using the same persistence io.saagie.updatarium.engine) can be possible.
     */
    abstract fun lock(changeSet: ChangeSet)

    /**
     * This function is called after the changeset execution, so you can now update the changeset status (in parameter) and store the logs.
     */
    abstract fun unlock(
        changeSet: ChangeSet,
        status: Status,
        logs: List<String>
    )
}
