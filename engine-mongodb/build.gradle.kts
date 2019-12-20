plugins {
    kotlin("jvm")
}

val kmongoVersion = "3.11.2"
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":core"))
    implementation("org.litote.kmongo:kmongo:$kmongoVersion")
}
