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
package io.saagie.updatarium.engine.mongo

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoIterable
import org.bson.UuidRepresentation
import org.litote.kmongo.KMongo

class MongoEngine(private val connectionStringEnvVar: String) {

    val mongoClient by lazy {
        KMongo.createClient(
            MongoClientSettings.builder().applyConnectionString(ConnectionString(getConnectionString()))
                .uuidRepresentation(UuidRepresentation.JAVA_LEGACY).build()
        )
    }

    private fun getConnectionString(): String {
        if (System.getenv().containsKey(connectionStringEnvVar)) {
            return System.getenv(connectionStringEnvVar)
        }
        throw IllegalArgumentException("$connectionStringEnvVar is missing (environment variable)")
    }

    fun listDatabase(): MongoIterable<String> = mongoClient.listDatabaseNames()
}
