package io.saagie.updatarium

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
import io.saagie.updatarium.config.UpdatariumConfiguration
import io.saagie.updatarium.ktsloader.KtsObjectLoader
import io.saagie.updatarium.model.ChangeLog
import io.saagie.updatarium.model.UpdatariumError
import java.io.Reader
import java.nio.file.Files
import java.nio.file.Path
import mu.KotlinLogging

/**
 * Main class of Updatarium.
 *
 * Updatarium is initialized with a PersistEngine (if not, it uses the DefaultPersistEngine),
 * Then you can call the function 'executeChangelog' using a Path, a Reader or directly the script in String,
 * It will execute the changelog.
 */
class Updatarium(val configuration: UpdatariumConfiguration = UpdatariumConfiguration()) {
    private val logger = KotlinLogging.logger {}
    private val ktsLoader = KtsObjectLoader()

    fun executeChangeLog(reader: Reader, tags: List<String> = emptyList()) {
        executeScript(reader.readText(), tags)
    }

    fun executeChangeLog(script: String, tags: List<String> = emptyList()) {
        executeScript(script, tags)
    }

    fun executeChangeLog(reader: Reader, tag: String) {
        this.executeChangeLog(reader, listOf(tag))
    }

    fun executeChangeLog(path: Path, tags: List<String> = emptyList()) {
        executeChangeLog(Files.newBufferedReader(path), tags, path.fileName.toString())
    }

    fun executeChangeLog(path: Path, tag: String) {
        executeChangeLog(Files.newBufferedReader(path), listOf(tag), path.fileName.toString())
    }

    fun executeChangeLog(script: String, tag: String) {
        executeChangeLog(script, listOf(tag))
    }

    fun executeChangeLogs(path: Path, pattern: String, tag: String) {
        executeChangeLogs(path, pattern, listOf(tag))
    }

    private data class ExecutionState(val exceptions: List<UpdatariumError.ExitError> = emptyList()) {
        val hasError: Boolean = exceptions.isNotEmpty()

        fun execute(failFast: Boolean, block: () -> Unit): ExecutionState =
            if (hasError && failFast) this // fail fast and already failed => do nothing
            else try {
                block()
                this
            } catch (e: UpdatariumError.ExitError) {
                copy(exceptions = exceptions + e)
            }
    }

    fun executeChangeLogs(path: Path, pattern: String, tags: List<String> = emptyList()) {
        if (!Files.isDirectory(path)) {
            logger.error { "$path is not a directory." }
            throw UpdatariumError.ExitError
        } else {
            val state =
                path
                    .toFile()
                    .walk()
                    .maxDepth(generateMaxDepth())
                    .filter { it.name.matches(Regex(pattern)) }
                    .sorted()
                    .fold(ExecutionState()) { state, file ->
                        state.execute(configuration.failFast) {
                            this.executeChangeLog(file.toPath(), tags)
                        }
                    }
            if (state.hasError) throw UpdatariumError.ExitError
        }
    }

    internal fun generateMaxDepth(): Int =
        when {
            configuration.listFilesRecursively -> {
                Int.MAX_VALUE
            }
            else -> 1
        }

    fun executeChangeLog(changelog: ChangeLog, tags: List<String> = emptyList()) {
        val result = changelog.execute(configuration, tags)
        result.changeSetExceptions.forEach { logger.error { it } }
        if (result.changeSetExceptions.isNotEmpty()) {
            throw UpdatariumError.ExitError
        }
    }

    private fun executeChangeLog(reader: Reader, tags: List<String>, id: String) {
        val changeLog = ktsLoader.load<ChangeLog>(reader).let { loaded ->
            if (loaded.id.isEmpty()) loaded.copy(id = id) else loaded
        }
        executeChangeLog(changeLog, tags)
    }

    private fun executeScript(script: String, tags: List<String>) {
        val changeLog = ktsLoader.load<ChangeLog>(script)
        executeChangeLog(changeLog, tags)
    }
}
