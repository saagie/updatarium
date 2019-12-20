plugins {
    kotlin("jvm")
}

val fabric8K8SClientVersion = "4.6.4"
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":core"))
    implementation("io.fabric8:kubernetes-client:$fabric8K8SClientVersion")
}
