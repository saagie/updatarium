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
import de.swirtz.ktsrunner.objectloader.KtsObjectLoader
import io.saagie.updatarium.config.UpdatariumConfiguration
import io.saagie.updatarium.dsl.Changelog
import io.saagie.updatarium.dsl.UpdatariumError
import mu.KotlinLogging
import java.io.Reader
import java.nio.file.Files
import java.nio.file.Path

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

    fun executeChangelog(reader: Reader, tags: List<String> = emptyList(), changelogId: String = "") {
        executeScript(reader.readText(), changelogId, tags)
    }

    fun executeChangelog(script: String, tags: List<String> = emptyList(), changelogId: String = "") {
        executeScript(script, changelogId, tags)
    }

    fun executeChangelog(reader: Reader, tag: String, changelogId: String = "") {
        this.executeChangelog(reader, listOf(tag), changelogId)
    }

    fun executeChangelog(path: Path, tags: List<String> = emptyList()) {
        executeChangelog(Files.newBufferedReader(path), tags, path.toAbsolutePath().toString())
    }

    fun executeChangelog(path: Path, tag: String) {
        executeChangelog(Files.newBufferedReader(path), listOf(tag), path.toAbsolutePath().toString())
    }

    fun executeChangelog(script: String, tag: String, changelogId: String = "") {
        executeChangelog(script, listOf(tag), changelogId)
    }

    fun executeChangelogs(path: Path, pattern: String, tag: String) {
        executeChangelogs(path, pattern, listOf(tag))
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

    fun executeChangelogs(path: Path, pattern: String, tags: List<String> = emptyList()) {
        if (!Files.isDirectory(path)) {
            logger.error { "$path is not a directory." }
            throw UpdatariumError.ExitError
        } else {
            val state = path
                .toFile()
                .walk()
                .filter { it.name.matches(Regex(pattern)) }
                .sorted()
                .fold(ExecutionState()) { state, file ->
                    state.execute(configuration.failfast) {
                        this.executeChangelog(file.toPath(), tags)
                    }
                }

            if (state.hasError) throw UpdatariumError.ExitError
        }
    }

    private fun executeScript(
        script: String,
        changelogId: String,
        tags: List<String>
    ) {
        with(ktsLoader.load<Changelog>(script)) {
            this.setId(changelogId)
            with(this.execute(configuration, tags).changeSetException) {
                if (this.isNotEmpty()) {
                    this.forEach { logger.error { it } }
                    throw UpdatariumError.ExitError
                }
            }
        }
    }
}

