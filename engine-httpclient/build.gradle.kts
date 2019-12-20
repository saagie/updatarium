plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":core"))
    implementation("com.github.kittinunf.fuel:fuel:2.2.1")
    implementation("me.lazmaid.kraph:kraph:0.6.0")
}