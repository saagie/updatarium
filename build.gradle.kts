import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("jvm") version "1.3.61"
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
        maven(url = "https://dl.bintray.com/s1m0nw1/KtsRunner")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }


}
val autoImportDependencies = mapOf(
    "io.github.microutils:kotlin-logging" to "1.7.8",
    "org.slf4j:slf4j-api" to "1.7.29",
    "org.apache.logging.log4j:log4j-slf4j-impl" to "2.13.0"
)

subprojects {
    apply(plugin = "java")
    id("com.github.johnrengelman.shadow").version("2.0.2")

    group = "io.saagie"
    version = "1.0-SNAPSHOT"

    dependencies {
        autoImportDependencies.forEach {
            implementation("${it.key}:${it.value}")
        }
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}