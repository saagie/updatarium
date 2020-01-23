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
import io.saagie.updatarium.dsl.ChangeSet
import io.saagie.updatarium.dsl.Status
import io.saagie.updatarium.log.InMemoryEvent
import io.saagie.updatarium.persist.model.MongoDbChangeset
import io.saagie.updatarium.persist.model.toMongoDbDocument
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.LogEvent
import org.litote.kmongo.*

const val MONGODB_PERSIST_CONNECTIONSTRING = "MONGODB_PERSIST_CONNECTIONSTRING"
const val DATABASE = "Updatarium"
const val COLLECTION = "changelog"

class MongodbPersistEngine : PersistEngine() {

    private val collection by lazy {
        with(KMongo.createClient(ConnectionString(getConnectionString()))) {
            this.getDatabase(DATABASE).getCollection<MongoDbChangeset>(COLLECTION)
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
        when (val doc = collection.findOne(MongoDbChangeset::changesetId eq changeSetId)) {
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

    override fun lock(changeSet: ChangeSet) {
        collection.insertOne(changeSet.toMongoDbDocument())
        logger.info { "${changeSet.id} marked as ${Status.EXECUTE}" }
    }

    override fun unlock(
        changeSet: ChangeSet,
        status: Status,
        logs: List<InMemoryEvent<Level, LogEvent>>
    ) {
        collection.updateOne(
            MongoDbChangeset::changesetId eq changeSet.id,
            set(
                MongoDbChangeset::status setTo status.name,
                MongoDbChangeset::log setTo logs.toStringList()
            )
        )
        logger.info { "${changeSet.id} marked as $status" }
    }
}

private fun List<InMemoryEvent<Level, LogEvent>>.toStringList(): List<String> = this.map { event ->
    "${event.time} [${event.level.name()}] ${event.message} ${event.exception ?: ""}"
}

