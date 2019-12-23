plugins {
    kotlin("jvm")
}

val fuelVersion = "2.2.1"
val kraphVersion = "0.6.0"
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":core"))
    implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")
    implementation("me.lazmaid.kraph:kraph:$kraphVersion")
}