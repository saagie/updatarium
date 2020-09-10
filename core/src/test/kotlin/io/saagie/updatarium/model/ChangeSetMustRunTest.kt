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

import io.mockk.every
import io.mockk.mockk
import io.saagie.updatarium.config.UpdatariumConfiguration
import io.saagie.updatarium.model.ExecutionStatus.FAIL
import io.saagie.updatarium.model.UpdatariumError.AlreadyExecutedAndInError
import io.saagie.updatarium.persist.PersistEngine
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class ChangeSetMustRunTest {
    companion object {
        @JvmStatic
        fun provideData(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(ExecutionStatus.NOT_EXECUTED, true),
                Arguments.of(ExecutionStatus.EXECUTE, false),
                Arguments.of(ExecutionStatus.OK, false),
                Arguments.of(ExecutionStatus.MANUAL_OK, false),
                Arguments.of(ExecutionStatus.RETRY, true)
            )
        }
    }

    val changeSet = ChangeSet(
        id = "changeSet1",
        author = "test",
        actions = listOf(
            Action { true }
        )
    )

    val persistEngine = mockk<PersistEngine>()

    val config = UpdatariumConfiguration(persistEngine = persistEngine, listFilesRecursively = true)


    @ParameterizedTest
    @MethodSource("provideData")
    fun should_test_correct_mustRun_return_for_all_ExecutionStatus(
        status: ExecutionStatus,
        expectedMustRun: Boolean
    ) {
        // GIVEN
        every { persistEngine.findLatestExecutionStatus("executionID") } returns status
        // WHEN
        val mustRun = changeSet.mustRun("executionID", configuration = config)
        // THEN
        assertEquals(expectedMustRun, mustRun)
    }

    @Test
    fun should_throw_an_error_if_latestExecutionStatus_is_FAIL() {
        // GIVEN
        every { persistEngine.findLatestExecutionStatus("executionID") } returns FAIL
        // WHEN
        try {
            changeSet.mustRun("executionID", configuration = config)
            fail("Should thrown an error")
        } catch (e: AlreadyExecutedAndInError) {
            // THEN
            assertEquals("executionID", e.executionId)
        }
    }
}
