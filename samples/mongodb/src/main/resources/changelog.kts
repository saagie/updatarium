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
import io.saagie.updatarium.model.action.mongoAction
import io.saagie.updatarium.model.changeLog
import org.litote.kmongo.*

changeLog {
    changeSet(id = "ChangeSet-Mongodb-1", author = "MongoDb") {
        mongoAction {
            logger.info { "MongoDb" }
            val database = "sample_mongodb"
            val collection = "sample"

            mongoClient.dropDatabase(database)
            logger.info { "DB $database drop OK" }
            with(onCollection(database, collection)) {
                logger.info { "Collection OK" }
                this.insertOne("{name:'Yoda',age:896}")
                this.insertOne("{name:'Luke Skywalker',age:19}")
                logger.info { "2 docs inserted in collection $database.$collection" }
                this.find()
                    .forEach { logger.info { " name : ${it["name"]} - age : ${it["age"]}" } }
            }
        }

        mongoAction(connectionStringEnvVar = "OTHERCONNECTIONSTRING") {
            logger.info { "MongoDb" }
            val database = "sample_mongodb2"
            val collection = "sample"

            mongoClient.dropDatabase(database)
            logger.info { "DB $database drop OK" }
            with(onCollection(database, collection)) {
                logger.info { "Collection OK" }
                this.insertOne("{name:'Yoda',age:896}")
                this.insertOne("{name:'Luke Skywalker',age:19}")
                logger.info { "2 docs inserted in collection $database.$collection" }
                this.find()
                    .forEach { logger.info { " name : ${it["name"]} - age : ${it["age"]}" } }

            }
        }
    }
}