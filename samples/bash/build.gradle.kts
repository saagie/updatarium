plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":core"))
    implementation(project(":engine-bash"))

    implementation(kotlin("scripting-compiler-embeddable"))
    implementation(kotlin("script-util"))
}
