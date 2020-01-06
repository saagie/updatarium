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
package dsl

import com.autodsl.annotation.AutoDsl
import dsl.action.Action
import mu.KLoggable
import persist.DefaultPersistEngine
import persist.PersistEngine

@AutoDsl
data class ChangeSet(val id: String, val author: String, val actions: List<Action> = mutableListOf()) : KLoggable {
    override val logger = logger()

    fun execute(engine: PersistEngine = DefaultPersistEngine()) {
        if (engine.notAlreadyExecuted(id)) {
            logger.info { "$id will be executed" }
            engine.lock(this)
            try {
                this.actions.forEach {
                    it.execute()
                }
                engine.unlock(this, Status.OK)
                logger.info { "$id marked as ${Status.OK}" }
            } catch (e: Exception) {
                logger.error(e) { "Error during apply update" }
                engine.unlock(this, Status.KO)
                logger.info { "$id marked as ${Status.KO}" }
            }
        } else {
            logger.info { "$id already executed" }
        }
    }

    private fun unlock() {
        logger.info { "UNLOCK" }
    }
}