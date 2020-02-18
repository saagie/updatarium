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
package io.saagie.updatarium.model

internal data class ChangelogExecutionState(val exceptions: List<UpdatariumError.ChangeSetError> = emptyList()) {
    internal val hasError: Boolean = exceptions.isNotEmpty()

    val report: ChangeLogReport by lazy {
        ChangeLogReport(exceptions)
    }

    fun execute(failFast: Boolean, block: () -> List<UpdatariumError.ChangeSetError>): ChangelogExecutionState =
        if (hasError && failFast) this // fail fast and already failed => do nothing
        else try {
            val stepErrors = block()
            copy(exceptions = exceptions + stepErrors)
        } catch (e: UpdatariumError.ChangeSetError) {
            copy(exceptions = exceptions + e)
        }
}
