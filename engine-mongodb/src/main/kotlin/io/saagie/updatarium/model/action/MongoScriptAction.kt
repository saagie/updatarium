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
package io.saagie.updatarium.model.action

import io.saagie.updatarium.engine.mongo.MongoEngine
import io.saagie.updatarium.model.ChangeSetDsl
import mu.KotlinLogging

const val MONGODB_CONNECTIONSTRING = "MONGODB_CONNECTIONSTRING"

fun ChangeSetDsl.mongoAction(
    connectionStringEnvVar: String = MONGODB_CONNECTIONSTRING,
    block: MongoScriptActionDsl.() -> Unit
) {
    this.action { MongoScriptActionDsl(connectionStringEnvVar).block() }
}

class MongoScriptActionDsl(connectionStringEnvVar: String = MONGODB_CONNECTIONSTRING) {
    val logger = KotlinLogging.logger("mongoAction")
    val mongoEngine = MongoEngine(connectionStringEnvVar)
    val mongoClient = mongoEngine.mongoClient

    fun onCollection(databaseName: String, collectionName: String) =
        mongoClient.getDatabase(databaseName).getCollection(collectionName)

    fun onCollections(databaseNameRegex: Regex, collectionName: String) =
        mongoClient.listDatabaseNames().filter { it.matches(databaseNameRegex) }.map {
            mongoClient.getDatabase(it).getCollection(collectionName)
        }
}
