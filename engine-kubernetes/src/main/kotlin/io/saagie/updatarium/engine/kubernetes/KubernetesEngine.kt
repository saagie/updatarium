/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2019-2023 Creative Data.
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
package io.saagie.updatarium.engine.kubernetes

import io.fabric8.kubernetes.client.ConfigBuilder
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import io.fabric8.kubernetes.client.NamespacedKubernetesClient

object KubernetesEngine {
    fun getClient(namespace: String? = null): NamespacedKubernetesClient =
        if (namespace == null) {
            KubernetesClientBuilder().build()
        } else {
            KubernetesClientBuilder().withConfig(ConfigBuilder().withNamespace(namespace).build()).build()
        }.adapt(NamespacedKubernetesClient::class.java)
}
