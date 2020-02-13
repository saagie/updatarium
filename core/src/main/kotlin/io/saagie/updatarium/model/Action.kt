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

/**
 * This class represent an Action. All custom actions should use the abstract class.
 *
 * Only one function is available : `execute`, this function is called by the core io.saagie.updatarium.engine for a not already execute changeSet.
 */
abstract class Action {

    /**
     * The execute function.
     *
     * It will return an exception is something wrong happen.
     */
    abstract fun execute()

    companion object {

        operator fun invoke(block: () -> Unit): Action =
            object : Action() {
                override fun execute() {
                    block()
                }
            }
    }
}
