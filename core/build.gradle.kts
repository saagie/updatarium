plugins {
    kotlin("jvm")
    kotlin("kapt")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("compiler-embeddable"))

    // ktsRunner
    implementation(kotlin("scripting-compiler-embeddable"))
    implementation(kotlin("script-util"))
    implementation("net.java.dev.jna:jna:5.5.0")
    implementation("de.swirtz:ktsRunner:0.0.7") { exclude(group = "ch.qos.logback", module = "logback-classic") }

    // AutoDsl Annotation
    api("com.juanchosaravia.autodsl:annotation:0.0.9")
    kapt("com.juanchosaravia.autodsl:processor:0.0.9")
}
