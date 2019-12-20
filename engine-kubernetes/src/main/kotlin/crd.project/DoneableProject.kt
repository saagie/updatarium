package crd.project

import io.fabric8.kubernetes.api.builder.Function
import io.fabric8.kubernetes.client.CustomResourceDoneable

class DoneableProject(val resource: Project, val function: Function<Project, Project>) :
    CustomResourceDoneable<Project>(resource, function) {

}