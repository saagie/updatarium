/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2019-2020 Pierre Leresteux.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

val bintrayUsername: String by project
val bintrayApiKey: String by project

plugins {
    kotlin("jvm") version "1.3.61"
    id("net.thauvin.erik.gradle.semver").version("1.0.4")
    id("org.kordamp.gradle.kotlin-project") version "0.32.0"
    id("org.kordamp.gradle.bintray") version "0.32.0"
    id("org.kordamp.gradle.coveralls") version "0.32.0"
    id("org.kordamp.gradle.jacoco") version "0.32.0"
    id("org.kordamp.gradle.detekt") version "0.32.0"
    id("com.adarshr.test-logger") version "2.0.0"
}

val props = Properties().apply {
    load(file("version.properties").inputStream())
}
config {

    release = false

    info {
        name = "Updatarium"
        description = "Automated update script management"
        inceptionYear = "2019"
        vendor = "Saagie"
        group = "io.saagie.updatarium"

        scm {
            url = "https://github.com/saagie/updatarium"
        }

        links {
            website = "https://www.saagie.com"
            scm = "https://github.com/saagie/updatarium"
            issueTracker = "https://github.com/saagie/updatarium/issues"
        }

        licensing {
            licenses {
                license {
                    id = "Apache-2.0"
                }
            }
        }

        coverage {
            jacoco {
                enabled = true
            }
            coveralls {
                enabled = true
            }
        }

        people {
            person {
                id = "pierre"
                name = "Pierre Leresteux"
                email = "pierre@saagie.com"
                roles = listOf("author", "developer")
            }
            person {
                id = "richard"
                name = "Richard Capraro"
                email = "richard.capraro@saagie.com"
                roles = listOf("developer")
            }
            person {
                id = "guillaume"
                name = "Guillaume Naimi"
                email = "guillaume.naimi@saagie.com"
                roles = listOf("developer")
            }
        }

        bintray {
            credentials {
                username = bintrayUsername
                password = bintrayApiKey
            }
            userOrg = "saagie"
            name = "updatarium"
            githubRepo = "saagie/updatarium"
            publish = true
        }
    }
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
        maven(url = "https://dl.bintray.com/s1m0nw1/KtsRunner")
    }
    group = "io.saagie.updatarium"
    version = props.get("version.semver")!!

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}
val autoImportDependencies = mapOf(
    "io.github.microutils:kotlin-logging" to "1.7.8",
    "org.slf4j:slf4j-api" to "1.7.30"
)

val sampleAutoImportDependencies = mapOf(
    "org.apache.logging.log4j:log4j-slf4j-impl" to "2.13.0",
    "org.apache.logging.log4j:log4j-core" to "2.13.0"
)

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.kordamp.gradle.kotlin-project")

    dependencies {
        autoImportDependencies.forEach {
            implementation("${it.key}:${it.value}")
        }
        if (this@subprojects.parent?.name == "samples") {
            sampleAutoImportDependencies.forEach {
                implementation("${it.key}:${it.value}")
            }
        }
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}
