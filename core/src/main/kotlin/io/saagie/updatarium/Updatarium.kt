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
class Updatarium(val engine: PersistEngine = DefaultPersistEngine()) {
    val ktsLoader = KtsObjectLoader()

    fun executeChangelog(path: Path) {
        executeChangelog(Files.newBufferedReader(path))
    }

    fun executeChangelog(reader: Reader) {
        with(ktsLoader.load<Changelog>(reader)) {
            this.execute(engine)
        }
    }

    fun executeChangelog(script: String) {
        with(ktsLoader.load<Changelog>(script)) {
            this.execute(engine)
        }
    }

    fun executeChangelogs(path: Path, pattern: String) {
        if (Files.isDirectory(path)) {
            path
                .toFile()
                .walk()
                .filter { it.name.matches(Regex(pattern)) }
                .sorted()
                .forEach {
                    this.executeChangelog(it.toPath())
                }

        }

    }
}
