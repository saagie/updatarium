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
package io.saagie.updatarium.persist

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import io.saagie.updatarium.model.ChangeSet
import io.saagie.updatarium.model.ExecutionStatus
import io.saagie.updatarium.model.ExecutionStatus.NOT_EXECUTED
import io.saagie.updatarium.persist.model.MongoDbChangeSet
import io.saagie.updatarium.persist.model.toMongoDbDocument
import org.bson.UuidRepresentation
import org.litote.kmongo.KMongo
import org.litote.kmongo.descending
import org.litote.kmongo.eq
import org.litote.kmongo.getCollection
import java.time.Instant

const val MONGODB_PERSIST_CONNECTIONSTRING = "MONGODB_PERSIST_CONNECTIONSTRING"
const val MONGODB_PERSIST_DATABASE = "MONGODB_PERSIST_DATABASE"
const val MONGODB_PERSIST_CHANGESET_COLLECTION = "MONGODB_PERSIST_CHANGESET_COLLECTION"
const val DATABASE = "Updatarium"
const val COLLECTION = "changeset"

class MongodbPersistEngine(override val configuration: PersistConfig = PersistConfig()) : PersistEngine(configuration) {

    private val collection by lazy {
        with(
            KMongo.createClient(
                MongoClientSettings.builder().applyConnectionString(ConnectionString(getConnectionString()))
                    .uuidRepresentation(UuidRepresentation.JAVA_LEGACY).build()
            )
        ) {
            this.getDatabase(System.getenv().getOrDefault(MONGODB_PERSIST_DATABASE, DATABASE))
                .getCollection<MongoDbChangeSet>(
                    System.getenv().getOrDefault(MONGODB_PERSIST_CHANGESET_COLLECTION, COLLECTION)
                )
                .apply {
                    this.createIndex(
                        Indexes.ascending("changeSetId"),
                        IndexOptions().name("changeSetId_1")
                    )
                }
        }
    }

    private fun getConnectionString(): String =
        System.getenv(MONGODB_PERSIST_CONNECTIONSTRING)
            ?: throw IllegalArgumentException("$MONGODB_PERSIST_CONNECTIONSTRING is missing (environment variable)")

    override fun checkConnection() {
        logger.info { "CheckConnection ... " }
        collection.countDocuments()
        logger.info { "Connection to mongodb instance : successful" }
    }

    override fun findLatestExecutionStatus(changeSetId: String): ExecutionStatus =
        when (val doc = getLastRecordedChangeSet(changeSetId)) {
            null -> {
                logger.info { "$changeSetId not exists" }
                NOT_EXECUTED
            }
            else -> {
                logStatus(changeSetId, doc.status)
                ExecutionStatus.valueOf(doc.status)
            }
        }

    private fun logStatus(changeSetId: String, status: String) {
        when (status) {
            ExecutionStatus.OK.name -> {
                logger.info { "$changeSetId already executed: OK" }
            }
            ExecutionStatus.EXECUTE.name -> {
                logger.info { "$changeSetId already in progress?" }
            }
            ExecutionStatus.RETRY.name -> {
                logger.info { "$changeSetId was marked as: RETRY" }
            }
            ExecutionStatus.MANUAL_OK.name -> {
                logger.info { "$changeSetId was marked as: OK (manually)" }
            }
            ExecutionStatus.FAIL.name -> {
                logger.warn { "$changeSetId was already executed in error" }
            }
            else -> {
                logger.info { "$changeSetId has an the status: $status" }
            }
        }
    }

    private fun getLastRecordedChangeSet(changeSetId: String): MongoDbChangeSet? =
        collection.find(MongoDbChangeSet::changeSetId eq changeSetId)
            .sort(descending(MongoDbChangeSet::statusDate))
            .first()

    override fun lock(executionId: String, changeSet: ChangeSet) {
        collection.insertOne(changeSet.toMongoDbDocument(executionId))
        logger.info { "$executionId marked as ${ExecutionStatus.EXECUTE}" }
    }

    override fun unlock(executionId: String, changeSet: ChangeSet, status: ExecutionStatus, logs: List<String>) {
        collection.insertOne(
            changeSet.toMongoDbDocument(executionId).copy(
                status = status.name,
                statusDate = Instant.now(),
                log = logs
            )
        )
        logger.info { "$executionId marked as $status" }
    }
}
