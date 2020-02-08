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
    private val ktsLoader = KtsObjectLoader()

    fun executeChangelog(reader: Reader, tags: List<String> = emptyList(), changelogId: String = "") {
        with(ktsLoader.load<Changelog>(reader)) {
            this.setId(changelogId)
            this.execute(configuration, tags)
        }
    }

    fun executeChangelog(script: String, tags: List<String> = emptyList(), changelogId: String = "") {
        with(ktsLoader.load<Changelog>(script)) {
            this.setId(changelogId)
            this.execute(configuration, tags)
        }
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
