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
import io.saagie.updatarium.Updatarium
import java.nio.file.Paths

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
class Main

fun main() {
    val resourcesDirectory = Paths.get(Main::class.java.getResource("changelogs").path)
    val changelog1 = Paths.get(Main::class.java.getResource("changelogs/changelog1.kts").path)
    val changelog2 = Paths.get(Main::class.java.getResource("changelogs/changelog2.kts").path)
    Updatarium().executeChangeLogs(resourcesDirectory, "changelog(.*).kts", listOf("after", "before"))
    Updatarium().executeChangeLog(changelog1, listOf("never"))
    Updatarium().executeChangeLog(changelog2, listOf("before", "after"))
}
