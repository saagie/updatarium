/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2019 Pierre Leresteux.
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
package engine.bash

import dsl.action.BashScriptAction
import java.io.File

class BashEngine {
    fun runCommand(
        bashScriptAction: BashScriptAction
    ) {
        with(bashScriptAction) {
            val proc = ProcessBuilder(listOf("bash","-c",script))
                .directory(File(workingDir))
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start().apply { waitFor(timeoutAmount, timeoutUnit) }
            val out = proc.inputStream.bufferedReader().readText()
            when {
                proc.exitValue() == 0 -> logger.info { out.dropLast(1) }
                else -> throw Exception("Command '$script' execution error $out")
            }
        }
    }
}