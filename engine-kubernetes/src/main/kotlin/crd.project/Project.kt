package crd.project

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionBuilder
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionNames
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionSpec
import io.fabric8.kubernetes.client.CustomResource

data class Project(
    val platformId: String = "",
    val projectId: String = "",
    val customer: String = "",
    val minio: MinioInfo = MinioInfo()
) : CustomResource() {
    companion object {
        fun getCRD() = CustomResourceDefinitionBuilder()
            .withApiVersion("apiextensions.k8s.io/v1beta1")
            .withNewMetadata().withName("projects.saagie.io").endMetadata()
            .withSpec(CustomResourceDefinitionSpec().apply {
                group = "saagie.io"
                names = CustomResourceDefinitionNames().apply {
                    kind = "Project"
                    listKind = "ProjectList"
                    plural = "projects"
                    singular = "pj"
                }
                version = "v1alpha1"
                scope = "Cluster"
            })
            .build()
    }
}

data class MinioInfo(val endpoint: String = "", val secretName: String = "")