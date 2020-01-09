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


val bintrayUsername: String by project
val bintrayApiKey: String by project

plugins {
    kotlin("jvm") version "1.3.61"
    id("org.kordamp.gradle.kotlin-project") version "0.31.2"
    id("org.kordamp.gradle.bintray") version "0.31.2"
    id("org.kordamp.gradle.detekt") version "0.31.2"
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
            url = "https://github.com/PierreLeresteux/updatarium"
        }

        links {
            website = "https://www.saagie.com"
            scm = "https://github.com/PierreLeresteux/updatarium"
            issueTracker = "https://github.com/PierreLeresteux/updatarium/issues"
        }

        licensing {
            licenses {
                license {
                    id = "Apache-2.0"
                }
            }
        }

        people {
            person {
                id = "pierre"
                name = "Pierre Leresteux"
                email = "pierre@saagie.com"
                roles = listOf("author", "developer")
            }
        }

        bintray {
            credentials {
                username = bintrayUsername
                password = bintrayApiKey
            }
            userOrg = "pierresaagie"
            name = "updatarium"
            githubRepo = "PierreLeresteux/updatarium"
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

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }


}
val autoImportDependencies = mapOf(
    "io.github.microutils:kotlin-logging" to "1.7.8",
    "org.slf4j:slf4j-api" to "1.7.29"
)

val sampleAutoImportDependencies = mapOf(
    "org.apache.logging.log4j:log4j-slf4j-impl" to "2.13.0",
    "org.apache.logging.log4j:log4j-core" to "2.13.0"
)

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.kordamp.gradle.kotlin-project")

    group = "io.saagie.updatarium"
    version = "0.0.1"

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