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
package io.saagie.updatarium.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import java.util.concurrent.atomic.AtomicInteger
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ChangelogExecutionStateTest {

    @Nested
    inner class ExecuteTest {
        private val count = 10
        private val oops = UpdatariumError.ChangeSetError(ChangeSet(id = "plaf", author = "foo"))

        @Test
        fun `should run without issue with fail fast`() {
            val counter = AtomicInteger(0)

            val result = (1..count).fold(ChangelogExecutionState()) { acc, _ ->
                acc.execute(failFast = true) {
                    counter.incrementAndGet()
                    emptyList()
                }
            }

            assertThat(result.hasError).isFalse()
            assertThat(counter.get()).isEqualTo(count)
        }

        @Test
        fun `should stop after throwing first issue with fail fast`() {
            val counter = AtomicInteger(0)

            val result = (1..count).fold(ChangelogExecutionState()) { acc, _ ->
                acc.execute(failFast = true) {
                    counter.incrementAndGet()
                    throw oops
                }
            }

            assertThat(result.hasError).isTrue()
            assertThat(counter.get()).isEqualTo(1)
        }

        @Test
        fun `should gather all issues thrown without fail fast`() {
            val counter = AtomicInteger(0)

            val result = (1..count).fold(ChangelogExecutionState()) { acc, _ ->
                acc.execute(failFast = false) {
                    counter.incrementAndGet()
                    throw oops
                }
            }

            assertThat(result.hasError).isTrue()
            assertThat(counter.get()).isEqualTo(count)
        }

        @Test
        fun `should stop after returning first issue with fail fast`() {
            val counter = AtomicInteger(0)

            val result = (1..count).fold(ChangelogExecutionState()) { acc, _ ->
                acc.execute(failFast = true) {
                    counter.incrementAndGet()
                    listOf(oops)
                }
            }

            assertThat(result.hasError).isTrue()
            assertThat(counter.get()).isEqualTo(1)
        }

        @Test
        fun `should gather all issues returned without fail fast`() {
            val counter = AtomicInteger(0)

            val result = (1..count).fold(ChangelogExecutionState()) { acc, _ ->
                acc.execute(failFast = false) {
                    counter.incrementAndGet()
                    listOf(oops)
                }
            }

            assertThat(result.hasError).isTrue()
            assertThat(counter.get()).isEqualTo(count)
        }
    }
}
