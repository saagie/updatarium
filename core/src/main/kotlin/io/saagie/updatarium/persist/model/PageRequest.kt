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
package io.saagie.updatarium.persist.model

data class PageRequest(
    val size: Int = 10,
    val skip: Int = 0,
    val order: Sort = Sort.DESC
) {
    init {
        require(size > 0) {
            "size should be greater than 0, $size was found"
        }
        require(skip >= 0) {
            "skip should be greater or equal to 0, $skip was found"
        }
    }
}
