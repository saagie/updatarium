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
plugins {
    kotlin("jvm")
}
apply(plugin = "com.adarshr.test-logger")
config {
    publishing { enabled = true }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("compiler-embeddable"))

    // ktsRunner
    implementation(kotlin("scripting-compiler-embeddable"))
    implementation(kotlin("script-util"))
    implementation("net.java.dev.jna:jna:5.9.0")
    api("com.github.s1monw1:KtsRunner:v0.0.8") { exclude(group = "ch.qos.logback", module = "logback-classic") }

    // log4j2 appender plugin
    api("org.apache.logging.log4j:log4j-core:2.17.0")

    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.25")
    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:2.17.0")
    testImplementation(kotlin("reflect"))
    testImplementation("io.mockk:mockk:1.12.2")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
