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
import io.saagie.updatarium.dsl.action.MongoScriptAction
import io.saagie.updatarium.dsl.changeSet
import io.saagie.updatarium.dsl.changelog
import org.litote.kmongo.*

changelog {
    changesets {
        +changeSet {
            id = "ChangeSet-Mongodb-1"
            author = "MongoDb"
            actions {
                +MongoScriptAction {mongoScriptAction ->
                    mongoScriptAction.logger.info { "MongoDb" }
                    val database = "sample_mongodb"
                    val collection = "sample"

                    mongoScriptAction.mongoClient.dropDatabase(database)
                    mongoScriptAction.logger.info { "DB $database drop OK" }
                    with(mongoScriptAction.onCollection(database, collection)) {
                        mongoScriptAction.logger.info { "Collection OK" }
                        this.insertOne("{name:'Yoda',age:896}")
                        this.insertOne("{name:'Luke Skywalker',age:19}")
                        mongoScriptAction.logger.info { "2 docs inserted in collection $database.$collection" }
                        this.find()
                            .forEach {mongoScriptAction.logger.info { " name : ${it.get("name")} - age : ${it.get("age")}" }}

                    }
                }
            }
        }
    }
}
