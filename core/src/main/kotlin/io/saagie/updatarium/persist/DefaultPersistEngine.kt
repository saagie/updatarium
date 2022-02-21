/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2019-2022 Creative Data.
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

/**
 * This is a basic implementation of the PersistEngine.
 * It will store nothing and just let you execute the changeSet.
 *
 * :warning: please do not use this in production if you don't want to replay all changeSets.
 */
class DefaultPersistEngine(
    configuration: PersistConfig =
        PersistConfig(onSuccessStoreLogs = true, onErrorStoreLogs = true)
) : PersistEngine(configuration) {
    override fun checkConnection() {
        logger.warn { "***********************" }
        logger.warn { "*NO PERSIST ENGINE !!!*" }
        logger.warn { "***********************" }
    }

    override fun findLatestExecutionStatus(changeSetId: String): ExecutionStatus = NOT_EXECUTED

    override fun lock(executionId: String, changeSet: ChangeSet) {
        logger.info { "$executionId marked as ${ExecutionStatus.EXECUTE}" }
    }

    override fun unlock(executionId: String, changeSet: ChangeSet, status: ExecutionStatus, logs: List<String>) {
        logger.info { "$executionId marked as $status" }
        logger.info { logs }
    }
}
