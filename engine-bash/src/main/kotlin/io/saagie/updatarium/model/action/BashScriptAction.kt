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
package io.saagie.updatarium.model.action

import io.saagie.updatarium.engine.bash.BashEngine
import io.saagie.updatarium.model.ChangeSetDsl
import mu.KotlinLogging
import java.io.File
import java.time.Duration

fun ChangeSetDsl.bashAction(
    workingDir: String = ".",
    timeout: Duration = Duration.ofMinutes(1),
    block: BashActionDsl.() -> String
) {
    this.action {
        val bashAction = BashActionDsl()
        val bashEngine = BashEngine(bashAction.logger)
        val script = bashAction.block()

        bashEngine.runCommand(script, workingDir, timeout)
    }
}

fun ChangeSetDsl.bashAction(
    file: File,
    workingDir: String = ".",
    timeout: Duration = Duration.ofMinutes(1)
) {
    bashAction(workingDir, timeout) { file.readText() }
}

class BashActionDsl {
    val logger = KotlinLogging.logger("bashAction")
}
