plugins {
    kotlin("jvm")
    kotlin("kapt")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("compiler-embeddable"))


    api("com.juanchosaravia.autodsl:annotation:0.0.9")
    kapt("com.juanchosaravia.autodsl:processor:0.0.9")
}
