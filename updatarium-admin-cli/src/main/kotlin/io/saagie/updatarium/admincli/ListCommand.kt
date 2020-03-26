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
package io.saagie.updatarium.admincli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.unique
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import io.saagie.updatarium.model.ExecutionStatus
import io.saagie.updatarium.persist.MongodbPersistEngine
import io.saagie.updatarium.persist.model.PageRequest
import io.saagie.updatarium.persist.model.Sort

class ListCommand : CliktCommand(help = "List the changeSet executions", name = "list") {

    companion object {
        const val DEFAULT_LIMIT_VALUE = 10
        val EXECUTION_REPORT_RANGE_LIMIT = 0 .. 1000
        val HEADERS = listOf(
            "execution id",
            "change set",
            "status",
            "forced?",
            "author",
            "record at"
        )
    }

    private val limit by option(
        "--limit",
        help = "limit the changeSet display count " +
                "(default 10, min ${EXECUTION_REPORT_RANGE_LIMIT.first}, max ${EXECUTION_REPORT_RANGE_LIMIT.last})"
    )
        .int()
        .restrictTo(min = EXECUTION_REPORT_RANGE_LIMIT.first, max = EXECUTION_REPORT_RANGE_LIMIT.last)
        .default(DEFAULT_LIMIT_VALUE)

    private val skip by option(
        "--skip",
        help = "skip some changeSets (default 0, min 0)"
    )
        .int()
        .restrictTo(min = 0)
        .default(0)

    private val filteredChangeSet by option(
        "--change-set-id",  "-c",
        help = "filter with one specific change set"
    )

    private val withStatus by option(
        "--with-status", "-s",
        help = "filter executions with specific status"
    )
        .choice(ExecutionStatus.values().map { it.name to it }.toMap())
        .multiple()
        .unique()

    private val withError by option(
        "--with-error", "-e",
        help = "filter failed execution"
    ).flag()

    override fun run() {
        val requestedStatus = if (withError) withStatus.plus(ExecutionStatus.FAIL) else withStatus
        val statusToDisplay = if (requestedStatus.isEmpty()) ExecutionStatus.values().toSet() else requestedStatus

        val rows = MongodbPersistEngine().findExecutions(
            page = PageRequest(
                size = limit,
                skip = skip,
                order = Sort.DESC
            ),
            filterStatus = statusToDisplay,
            filterChangeSetId = filteredChangeSet

        )
            .sortedBy { it.statusDate }
            .map {
                listOf(
                    it.executionId,
                    it.changeSetId,
                    it.status.name,
                    if (it.forced) "FORCED" else "",
                    it.author,
                    it.statusDate.toString()
                )
            }
        DisplayableTable(HEADERS, rows).print()
    }
}
