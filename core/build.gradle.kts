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
plugins {
    kotlin("jvm")
    kotlin("kapt")
}

config {
    bintray { enabled = true }
    publishing { enabled = true }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("compiler-embeddable"))

    // ktsRunner
    implementation(kotlin("scripting-compiler-embeddable"))
    implementation(kotlin("script-util"))
    implementation("net.java.dev.jna:jna:5.5.0")
    api("de.swirtz:ktsRunner:0.0.7") { exclude(group = "ch.qos.logback", module = "logback-classic") }

    // AutoDsl Annotation
    api("com.juanchosaravia.autodsl:annotation:0.0.9")
    kapt("com.juanchosaravia.autodsl:processor:0.0.9")

    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
    testImplementation("com.github.codemonkeyfactory.test.logging:logging-test:1.0.0")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.20")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
