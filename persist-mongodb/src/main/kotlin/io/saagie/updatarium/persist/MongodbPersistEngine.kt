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

import com.mongodb.ConnectionString
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import io.saagie.updatarium.model.ChangeSet
import io.saagie.updatarium.model.Status
import io.saagie.updatarium.persist.model.MongoDbChangeSet
import io.saagie.updatarium.persist.model.toMongoDbDocument
import org.bson.BsonDocument
import java.time.Instant
import org.litote.kmongo.KMongo
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.set
import org.litote.kmongo.setTo

const val MONGODB_PERSIST_CONNECTIONSTRING = "MONGODB_PERSIST_CONNECTIONSTRING"
const val DATABASE = "Updatarium"
const val COLLECTION = "changeset"

class MongodbPersistEngine(override val configuration: PersistConfig = PersistConfig()) : PersistEngine(configuration) {

    private val collection by lazy {
        with(KMongo.createClient(ConnectionString(getConnectionString()))) {
            this.getDatabase(DATABASE).getCollection<MongoDbChangeSet>(COLLECTION).apply {
                this.createIndex(
                    Indexes.ascending("changeSetId"),
                    IndexOptions().name("changeSetId_1")
                )
            }
        }
    }

    private fun getConnectionString(): String {
        if (System.getenv().containsKey(MONGODB_PERSIST_CONNECTIONSTRING)) {
            return System.getenv(MONGODB_PERSIST_CONNECTIONSTRING)
        }
        throw IllegalArgumentException("$MONGODB_PERSIST_CONNECTIONSTRING is missing (environment variable)")
    }

    override fun checkConnection() {
        logger.info { "CheckConnection ... " }
        collection.countDocuments()
        logger.info { "Connection to mongodb instance : successful" }
    }

    override fun notAlreadyExecuted(changeSetId: String): Boolean {
        when (val doc = collection.findOne(MongoDbChangeSet::changeSetId eq changeSetId)) {
            null -> {
                logger.info { "$changeSetId not exists" }
                return true
            }
            else -> {
                when (doc.status) {
                    Status.OK.name -> {
                        logger.info { "$changeSetId already executed : OK" }
                    }
                    Status.EXECUTE.name -> {
                        logger.info { "$changeSetId already in progress ?" }
                    }
                    else -> {
                        logger.warn { "$changeSetId was already executed in error" }
                    }
                }
                return false
            }
        }
    }

    override fun lock(executionId: String, changeSet: ChangeSet) {
        collection.insertOne(changeSet.toMongoDbDocument(executionId))
        logger.info { "$executionId marked as ${Status.EXECUTE}" }
    }

    override fun unlock(executionId: String, changeSet: ChangeSet, status: Status, logs: List<String>) {
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
