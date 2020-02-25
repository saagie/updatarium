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
package io.saagie.updatarium.log

import io.saagie.updatarium.persist.PersistConfig
import java.util.concurrent.ConcurrentLinkedQueue
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.LogEvent

/**
 * Singleton instance of the Queue of [InMemoryEvent]
 */
object EventsQueueInstance {
    lateinit var instance: ConcurrentLinkedQueue<InMemoryEvent<Level, LogEvent>>
}

/**
 * Allow access to the [InMemoryAppenderAccess] and its Queue of [InMemoryEvent]
 */
object InMemoryAppenderAccess {

    fun getEvents(): List<InMemoryEvent<Level, LogEvent>> = EventsQueueInstance.instance.asSequence().toList()

    fun getEvents(persistConfig: PersistConfig, success: Boolean) = EventsQueueInstance.instance
        .asSequence()
        .toList()
        .filter { (success && persistConfig.onSuccessStoreLogs) || (!success && persistConfig.onErrorStoreLogs) }
        .map { persistConfig.layout(it) }
}
