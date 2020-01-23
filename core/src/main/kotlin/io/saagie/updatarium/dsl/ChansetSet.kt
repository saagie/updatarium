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
import io.saagie.updatarium.dsl.action.Action
import io.saagie.updatarium.log.InMemoryAppenderAccess
import io.saagie.updatarium.log.InMemoryAppenderManager
import mu.KLoggable
import io.saagie.updatarium.persist.DefaultPersistEngine
import io.saagie.updatarium.persist.PersistEngine

@AutoDsl
data class ChangeSet(
    val id: String,
    val author: String,
    val tags: List<String>? = mutableListOf(),
    val actions: List<Action> = mutableListOf()
) : KLoggable {
    override val logger = logger()

    /**
     * The changeset execution :
     * - check if the changeset has already been execution (OK or KO)
     * - if not :
     *      - lock the changeset
     *      - execute each action sequientially.
     *      - unlock the changeset (with the correct status)
     *  Status => OK if all actions was OK, KO otherwise ...
     */
    fun execute(persistEngine: PersistEngine = DefaultPersistEngine()) {
        if (persistEngine.notAlreadyExecuted(id)) {
            logger.info { "$id will be executed" }
            persistEngine.lock(this)
            try {
                InMemoryAppenderManager.startRecord()
                this.actions.forEach {
                    it.execute()
                }
                InMemoryAppenderManager.stopRecord()
                persistEngine.unlock(this, Status.OK,InMemoryAppenderAccess.getEvents())
                logger.info { "$id marked as ${Status.OK}" }
            } catch (e: Exception) {
                logger.error(e) { "Error during apply update" }
                persistEngine.unlock(this, Status.KO, InMemoryAppenderAccess.getEvents())
                logger.info { "$id marked as ${Status.KO}" }
            }
        } else {
            logger.info { "$id already executed" }
        }
    }
}
