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
import io.saagie.updatarium.model.action.mysqlAction
import io.saagie.updatarium.model.changeLog
import java.sql.ResultSet


changeLog {
    changeSet(id = "ChangeSet-Mysql-1", author = "Mysql") {
        mysqlAction {
            logger.info { "Mysql" }

            val sqlcreateDatabase = "CREATE DATABASE IF NOT EXISTS starwars"

            val sqlCreate = """
             CREATE TABLE IF NOT EXISTS starwars.character (
                id INT AUTO_INCREMENT PRIMARY KEY,
                NAME VARCHAR(255),
                AGE INT)
            """.trimMargin()
            val sqlTrunc = "TRUNCATE TABLE starwars.character"
            with(mysqlConnection) {
                createStatement().execute(sqlcreateDatabase)
                createStatement().execute(sqlCreate)
                createStatement().execute(sqlTrunc)
            }

            val sqlInsert = """
             INSERT INTO starwars.character(name, age) VALUES ("Yoda",896),("Luke Skywalker",19)
            """.trimMargin()

            with(mysqlConnection) {
                createStatement().execute(sqlInsert)
            }

            with(mysqlConnection) {
                createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY).use { stmt ->
                    stmt.fetchSize = Int.MIN_VALUE
                    stmt.executeQuery("select name, age from starwars.character").use { rs ->
                        while (rs.next()) {
                            val name = rs.getString("name")
                            val age = rs.getString("age")
                            logger.info { " name : $name - age : $age" }
                        }
                    }

                }
            }
        }
    }
}
