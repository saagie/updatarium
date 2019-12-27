import dsl.action.KubernetesScriptAction
import dsl.changeSet
import dsl.changelog

changelog {
    changesets {
        +changeSet {
            id = "ChangeSet-Kubernetes-1"
            author = "k8s"
            actions {
                +KubernetesScriptAction { kubernetesScriptAction ->
                    kubernetesScriptAction.logger.warn { "Need to be connected to a cluster" }
                    kubernetesScriptAction.client.version.data
                        .forEach { key, value -> kubernetesScriptAction.logger.info { "$key = $value" } }
                }
            }
        }
    }
}