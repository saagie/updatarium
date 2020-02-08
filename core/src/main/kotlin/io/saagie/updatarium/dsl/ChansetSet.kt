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

import com.autodsl.annotation.AutoDsl
import io.saagie.updatarium.config.UpdatariumConfiguration
import io.saagie.updatarium.dsl.Status.KO
import io.saagie.updatarium.dsl.Status.OK
import io.saagie.updatarium.dsl.action.Action
import io.saagie.updatarium.log.InMemoryAppenderAccess
import io.saagie.updatarium.log.InMemoryAppenderManager
import mu.KLoggable

@AutoDsl
data class ChangeSet(
    val id: String,
    val author: String,
    val tags: List<String>? = mutableListOf(),
    val actions: List<Action> = mutableListOf()
) : KLoggable {
    override val logger = logger()

    private var changelogId = ""

    /**
     * Set the changelogId is not empty
     */
    fun setChangelogId(id: String): ChangeSet {
        if (id.isNotEmpty()) {
            this.changelogId = "${id}_"
        }
        return this
    }

    /**
     * Generate an ID (changelogId id)
     */
    fun calculateId() = "$changelogId$id"

    /**
     * The changeset execution :
     * - check if the changeset has already been execution (OK or KO)
     * - if not :
     *      - lock the changeset
     *      - execute each action sequientially.
     *      - unlock the changeset (with the correct status)
     *  Status => OK if all actions was OK, KO otherwise ...
     */
    fun execute(configuration: UpdatariumConfiguration = UpdatariumConfiguration()) {
        val persistEngine = configuration.persistEngine
        if (persistEngine.notAlreadyExecuted(calculateId())) {
            logger.info { "$id will be executed" }
            if (!(configuration.dryRun)) {
                persistEngine.lock(this)
            }
            try {
                InMemoryAppenderManager.startRecord()
                this.actions.forEach {
                    if (configuration.dryRun) {
                        logger.warn { "DryRun => don't run it" }
                    } else {
                        it.execute()
                    }
                }
                InMemoryAppenderManager.stopRecord()
                this.sendUnlockToPersistEngine(
                    configuration, OK, InMemoryAppenderAccess
                        .getEvents(persistConfig = persistEngine.configuration, success = true)
                )
                logger.info { "$id marked as $OK" }
            } catch (e: Exception) {
                logger.error(e) { "Error during apply update" }
                this.sendUnlockToPersistEngine(
                    configuration, KO, InMemoryAppenderAccess
                        .getEvents(persistConfig = persistEngine.configuration, success = false)
                )
                logger.info { "$id marked as $KO" }
                if (configuration.failfast) {
                    throw e
                }
            }
        } else {
            logger.info { "$id already executed" }
        }
    }

    private fun ChangeSet.sendUnlockToPersistEngine(
        configuration: UpdatariumConfiguration,
        status: Status,
        events: List<String>
    ) {
        if (!(configuration.dryRun)) {
            configuration.persistEngine.unlock(this, status, events)
        }
    }
}
