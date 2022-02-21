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
package io.saagie.updatarium.config

import io.saagie.updatarium.persist.DefaultPersistEngine
import io.saagie.updatarium.persist.PersistEngine

data class UpdatariumConfiguration(
    val dryRun: Boolean = false,
    val failFast: Boolean = true,
    val persistEngine: PersistEngine = DefaultPersistEngine(),
    val listFilesRecursively: Boolean = true
)
