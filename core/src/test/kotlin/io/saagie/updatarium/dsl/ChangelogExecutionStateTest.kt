package io.saagie.updatarium.dsl

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

class ChangelogExecutionStateTest {

    @Nested
    inner class ExecuteTest {
        private val count = 10
        private val oops = UpdatariumError.ChangesetError(ChangeSet(id="plaf", author = "foo"))

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
        fun `should gather all issues throwed without fail fast`() {
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
