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
import dsl.Changelog
import persist.DefaultPersistEngine
import persist.PersistEngine
import java.io.Reader
import java.nio.file.Files
import java.nio.file.Path

class MagicalUpdater(val engine: PersistEngine = DefaultPersistEngine()) {
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

}