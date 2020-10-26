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
package io.saagie.updatarium.model.action

import io.saagie.updatarium.engine.mysql.MysqlEngine
import io.saagie.updatarium.model.ChangeSetDsl
import mu.KotlinLogging

const val MYSQL_JDBC_CONNECTIONSTRING = "MYSQL_JDBC_CONNECTIONSTRING"
const val MYSQL_JDBC_USERNAME = "MYSQL_JDBC_USERNAME"
const val MYSQL_JDBC_PASSWORD = "MYSQL_JDBC_PASSWORD"

fun ChangeSetDsl.mysqlAction(
    connectionStringEnvVar: String = MYSQL_JDBC_CONNECTIONSTRING,
    username: String = MYSQL_JDBC_USERNAME,
    password: String = MYSQL_JDBC_PASSWORD,
    block: MysqlScriptActionDsl.() -> Unit
) {
    this.action { MysqlScriptActionDsl(connectionStringEnvVar, username, password).block() }
}

class MysqlScriptActionDsl(
    connectionStringEnvVar: String = MYSQL_JDBC_CONNECTIONSTRING,
    username: String = MYSQL_JDBC_USERNAME,
    password: String = MYSQL_JDBC_PASSWORD
) {
    val logger = KotlinLogging.logger("mysqlAction")
    val mysqlEngine = MysqlEngine(connectionStringEnvVar, username, password)
    val mysqlConnection = mysqlEngine.connection

}
