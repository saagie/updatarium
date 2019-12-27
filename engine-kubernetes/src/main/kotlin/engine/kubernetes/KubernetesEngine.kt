package engine.kubernetes

import io.fabric8.kubernetes.client.DefaultKubernetesClient

class KubernetesEngine {
    companion object {
        fun getClient() = DefaultKubernetesClient()
        fun getClient(namespace: String) = getClient().inNamespace(namespace)

    }
}
