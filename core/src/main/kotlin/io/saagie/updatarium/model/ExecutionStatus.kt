/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2019-2022 Creative Data.
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

/**
 * Represent the status of the changeSet.
 */
enum class ExecutionStatus {
    // never executed
    NOT_EXECUTED,
    // execution in progress
    EXECUTE,
    // Execution is done with a correct status
    OK,
    // Execution is done but it has failed
    FAIL,
    // Previous FAILED execution was consider as OK manually
    MANUAL_OK,
    // Previous FAILED execution needs to be execute again
    RETRY
}
