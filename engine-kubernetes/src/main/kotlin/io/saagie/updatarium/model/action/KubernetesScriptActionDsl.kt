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
package io.saagie.updatarium.model.action

import io.saagie.updatarium.engine.kubernetes.KubernetesEngine
import io.saagie.updatarium.model.ChangeSetDsl
import mu.KotlinLogging

fun ChangeSetDsl.kubernetesAction(
    namespace: String? = null,
    block: KubernetesScriptActionDsl.() -> Unit
) {
    this.action {
        val actionDsl = KubernetesScriptActionDsl(namespace)
        try {
            actionDsl.block()
        } finally {
            actionDsl.client.close()
        }
    }
}

class KubernetesScriptActionDsl(namespace: String? = null) {
    val logger = KotlinLogging.logger("kubernetesAction")

    val client = KubernetesEngine.getClient(namespace)
}
