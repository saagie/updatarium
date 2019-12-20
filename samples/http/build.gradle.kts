plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("scripting-compiler-embeddable"))
    implementation(kotlin("script-util"))
    implementation("de.swirtz:ktsRunner:0.0.7") { exclude(group = "ch.qos.logback", module = "logback-classic") }
    implementation(project(":core"))
    implementation(project(":engine-httpclient"))
}
