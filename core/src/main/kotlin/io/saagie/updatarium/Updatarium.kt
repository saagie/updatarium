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
import io.saagie.updatarium.dsl.Changelog
import io.saagie.updatarium.persist.DefaultPersistEngine
import io.saagie.updatarium.persist.PersistEngine
import java.io.Reader
import java.nio.file.Files
import java.nio.file.Path

/**
 * Main class of Updatarium.
 *
 * Updatarium is initialize with a PersistEngine (if not, use the DefaultPersistEngine),
 * then you can call the function 'executeChangelog' using a Path, a Reader or directly the script in String.
 * It will execute the changelog.
 */
class Updatarium(val persistEngine: PersistEngine = DefaultPersistEngine()) {
    val ktsLoader = KtsObjectLoader()

    fun executeChangelog(reader: Reader, tags: List<String> = emptyList()) {
        with(ktsLoader.load<Changelog>(reader)) {
            this.execute(persistEngine, tags)
        }
    }

    fun executeChangelog(script: String, tags: List<String> = emptyList()) {
        with(ktsLoader.load<Changelog>(script)) {
            this.execute(persistEngine, tags)
        }
    }

    fun executeChangelog(reader: Reader, tag: String) {
        this.executeChangelog(reader, listOf(tag))
    }

    fun executeChangelog(path: Path, tags: List<String> = emptyList()) {
        executeChangelog(Files.newBufferedReader(path), tags)
    }

    fun executeChangelog(path: Path, tag: String) {
        executeChangelog(Files.newBufferedReader(path), listOf(tag))
    }

    fun executeChangelog(script: String, tag: String) {
        executeChangelog(script, listOf(tag))
    }

    fun executeChangelogs(path: Path, pattern: String, tag: String) {
        executeChangelogs(path, pattern, listOf(tag))
    }

    fun executeChangelogs(path: Path, pattern: String, tags: List<String> = emptyList()) {
        if (Files.isDirectory(path)) {
            path
                .toFile()
                .walk()
                .filter { it.name.matches(Regex(pattern)) }
                .sorted()
                .forEach {
                    this.executeChangelog(it.toPath(), tags)
                }
        }
    }
}
