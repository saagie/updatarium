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
import io.saagie.updatarium.model.action.kubernetesAction
import io.saagie.updatarium.model.changeLog

changeLog {
    changeSet(id = "ChangeSet-Kubernetes-1", author = "k8s") {
        kubernetesAction {
            logger.warn { "Need to be connected to a cluster" }
            client.version.data
                .forEach { (key, value) -> logger.info { "$key = $value" } }
        }
    }
}
