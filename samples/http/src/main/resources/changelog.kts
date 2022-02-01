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
import com.github.kittinunf.result.Result.Failure
import com.github.kittinunf.result.Result.Success
import io.saagie.updatarium.model.action.httpAction
import io.saagie.updatarium.model.changeLog
import me.lazmaid.kraph.Kraph

changeLog {
    changeSet("ChangeSet-Http-1", "Postman") {
        httpAction {
            val (_, _, result) = restClient
                .get("https://httpbin.org/get")
                .responseString()
            logger.info { result.get() }
        }
    }

    changeSet("ChangeSet-Http-2", "GraphQL") {
        httpAction {
            val query = Kraph {
                query {
                    cursorConnection("allFilms", first = 10) {
                        edges {
                            node {
                                field("title")
                            }
                        }
                    }
                }
            }
            val (_, _, result) = restClient
                .post("https://swapi-graphql.netlify.com/.netlify/functions/index")
                .header("content-type" to "application/json", "Accept" to "application/json")
                .body(query.toRequestString())
                .responseString()

            when (result) {
                is Failure -> {
                    val ex = result.getException()
                    println(ex)
                }
                is Success -> {
                    val data = result.get()
                    println(data)
                }
            }
        }
    }
}
