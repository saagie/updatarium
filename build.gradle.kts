/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2019-2022 Creative Data.
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
import java.util.Properties

plugins {
    kotlin("jvm") version "1.7.21"
    id("net.thauvin.erik.gradle.semver").version("1.0.4")
//    id("org.kordamp.gradle.kotlin-project") version "0.47.0" apply false
    id("org.kordamp.gradle.project") version "0.48.0"
    id("org.kordamp.gradle.coveralls") version "0.48.0"
    id("org.kordamp.gradle.jacoco") version "0.48.0"
//    id("org.kordamp.gradle.detekt") version "0.47.0" enable when detekt support kotlin 1.6
    id("com.adarshr.test-logger") version "3.2.0"
}

//buildscript {
//    //override dokka version from kordamp
//    dependencies {
//        classpath("org.jetbrains.dokka:kotlin-as-java-plugin:1.6.10")
//        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.6.10")
//    }
//}

val props = Properties().apply {
    load(file("version.properties").inputStream())
}

config {

    release = false

//    quality {
//        detekt {
//            toolVersion = "1.19.0"
//        }
//    }

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
                    id = org.kordamp.gradle.plugin.base.model.LicenseId.APACHE_2_0.spdx()
                }
            }
        }

        people{
            person{
                id = "Creative Data"
                name = "Creative Data"
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
    }
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
    group = "io.saagie.updatarium"
    version = props["version.semver"]!!
    apply(plugin = "kotlin")

    kotlin {
        jvmToolchain {
            (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

}
val autoImportDependencies = mapOf(
    "io.github.microutils:kotlin-logging" to "3.0.4",
    "org.slf4j:slf4j-api" to "2.0.5"
)

val sampleAutoImportDependencies = mapOf(
    "org.apache.logging.log4j:log4j-slf4j2-impl" to "2.19.0",
    "org.apache.logging.log4j:log4j-core" to "2.19.0"
)

subprojects {
//    apply(plugin = "org.kordamp.gradle.kotlin-project")

    dependencies {
        autoImportDependencies.forEach {
            api("${it.key}:${it.value}")
        }
        if (this@subprojects.parent?.name == "samples") {
            sampleAutoImportDependencies.forEach {
                implementation("${it.key}:${it.value}")
            }
        }
    }
}
