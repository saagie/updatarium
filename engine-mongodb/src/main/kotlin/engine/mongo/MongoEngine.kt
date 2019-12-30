/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2019 Pierre Leresteux.
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
package engine.mongo

import com.mongodb.ConnectionString
import com.mongodb.client.MongoIterable
import org.litote.kmongo.KMongo

const val MONGODB_CONNECTIONSTRING = "MONGODB_CONNECTIONSTRING"

class MongoEngine {

    val mongoClient by lazy { KMongo.createClient(ConnectionString(getConnectionString())) }

    private fun getConnectionString(): String {
        if (System.getenv().containsKey(MONGODB_CONNECTIONSTRING)) {
            return System.getenv(MONGODB_CONNECTIONSTRING)
        }
        throw IllegalArgumentException("$MONGODB_CONNECTIONSTRING is missing (environment variable)")
    }

    fun listDatabase(): MongoIterable<String> = mongoClient.listDatabaseNames()

}