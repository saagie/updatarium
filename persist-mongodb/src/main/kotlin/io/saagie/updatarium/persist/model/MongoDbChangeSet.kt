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
package io.saagie.updatarium.persist.model

import io.saagie.updatarium.model.ChangeSet
import io.saagie.updatarium.model.ExecutionStatus
import java.time.Instant

data class MongoDbChangeSet(
    val changeSetId: String,
    val author: String,
    val status: String,
    val lockDate: Instant = Instant.now(),
    val statusDate: Instant? = null,
    val force: Boolean = false,
    val log: List<String>
)

fun ChangeSet.toMongoDbDocument(executionId: String): MongoDbChangeSet = MongoDbChangeSet(
    changeSetId = executionId,
    author = this.author,
    status = ExecutionStatus.EXECUTE.name,
    force = this.force,
    log = mutableListOf()
)
