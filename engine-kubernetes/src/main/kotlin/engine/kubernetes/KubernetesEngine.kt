package engine.kubernetes

import crd.project.DoneableProject
import crd.project.Project
import crd.project.ProjectList
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource

class KubernetesEngine {
    companion object {
        fun getClient() = DefaultKubernetesClient()
        fun getClient(namespace: String) = getClient().inNamespace(namespace)

        val projectClient = (getClient().customResources(
            Project.getCRD(),
            Project::class.java,
            ProjectList::class.java,
            DoneableProject::class.java
        )) as (MixedOperation<Project?, ProjectList?, DoneableProject?, Resource<Project?, DoneableProject?>?>)
    }
}

fun KubernetesClient.projects() = KubernetesEngine.projectClient