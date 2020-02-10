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
package io.saagie.updatarium.dsl

import com.autodsl.annotation.AutoDsl
import io.saagie.updatarium.config.UpdatariumConfiguration
import io.saagie.updatarium.dsl.UpdatariumError.ChangesetError
import io.saagie.updatarium.log.InMemoryAppenderManager
import mu.KLoggable
import mu.KLogger
import mu.KotlinLogging

@AutoDsl
data class Changelog(var changesets: List<ChangeSet> = mutableListOf()) : KLoggable {
    override val logger = logger()

    private var id = ""

    /**
     * Set the ID of the changelog
     */
    fun setId(id: String) {
        this.id = id
    }

    /**
     * It will execute each changesets present in this changelog sequentially and return the list of Changeset
     * exceptions (if not failfast)
     */
    fun execute(
        configuration: UpdatariumConfiguration,
        tags: List<String> = emptyList()
    ): ChangelogReport {
        val exceptions: MutableList<ChangesetError> = mutableListOf()
        configuration.persistEngine.checkConnection()
        InMemoryAppenderManager.setup(persistConfig = configuration.persistEngine.configuration)
        matchedChangesets(tags).forEach {
            exceptions.addAll(it.setChangelogId(id).execute(configuration))
            if (configuration.failfast && exceptions.isNotEmpty()){
                return ChangelogReport(exceptions)
            }
        }
        InMemoryAppenderManager.tearDown()
        return ChangelogReport(exceptions)
    }

    /**
     * filter the changesets that matched with the targetTags
     * return all changesets if the targetTags list is empty
     * return only the matched changesets (at least one tag matched)
     */
    fun matchedChangesets(targetTags: List<String> = emptyList()) =
        this.changesets
            .filter { targetTags.isEmpty() || targetTags.intersect(it.tags ?: emptyList()).isNotEmpty() }
}

data class ChangelogReport(
    val changeSetException: List<ChangesetError>,
    override val logger: KLogger = KotlinLogging.logger {}
) : KLoggable
