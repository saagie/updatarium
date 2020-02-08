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
import io.saagie.updatarium.Updatarium
import io.saagie.updatarium.config.UpdatariumConfiguration
import io.saagie.updatarium.persist.MongodbPersistEngine
import io.saagie.updatarium.persist.PersistConfig
import org.apache.logging.log4j.Level

fun main() {
    val persistConfig = PersistConfig(level = Level.INFO, onSuccessStoreLogs = true, onErrorStoreLogs = true)
    Updatarium(UpdatariumConfiguration(persistEngine = MongodbPersistEngine(persistConfig))).executeChangelog(
        """
        import io.saagie.updatarium.dsl.action.BasicAction
        import io.saagie.updatarium.dsl.changeSet
        import io.saagie.updatarium.dsl.changelog

        changelog {
            changesets {
                +changeSet {
                    id = "ChangeSet-2"
                    author = "Hello World"
                    actions {
                        +BasicAction {
                            (1..5).forEach {
                                logger.info {"Hello ${"$"}it!"}
                                Thread.sleep(300)
                            }

                        }
                    }
                }
            }
        }
    """.trimIndent()
    )
}
