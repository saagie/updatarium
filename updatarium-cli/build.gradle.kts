import org.gradle.api.internal.plugins.DefaultTemplateBasedStartScriptGenerator

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
    application
}
config {
    bintray { enabled = false }
    publishing { enabled = false }
}

application {
    mainClassName = "io.saagie.updatarium.cli.StandaloneKt"
}

tasks.withType(CreateStartScripts::class.java){
    (unixStartScriptGenerator as DefaultTemplateBasedStartScriptGenerator).template = resources.text.fromFile("./template/customUnixStartScript.tpl")

}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.github.ajalt:clikt:2.6.0")
    // Updatarium deps
    implementation(project(":core"))
    implementation(project(":persist-mongodb"))
    implementation(project(":engine-bash"))
    implementation(project(":engine-mongodb"))
    implementation(project(":engine-mysql"))
    implementation(project(":engine-kubernetes"))
    implementation(project(":engine-httpclient"))
    // Logs
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.13.1")
    implementation("org.apache.logging.log4j:log4j-core:2.13.1")
}

