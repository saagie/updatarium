package dsl.action

import com.autodsl.annotation.AutoDsl
import engine.kubernetes.KubernetesEngine

@AutoDsl
class KubernetesScriptAction(
    val namespace: String? = null,
    val f: (kubernetesScriptAction: KubernetesScriptAction) -> Unit
) : Action() {

    val client = when {
        namespace != null -> KubernetesEngine.getClient(namespace)
        else -> KubernetesEngine.getClient()
    }

    override fun execute() {
        f(this)
    }
}