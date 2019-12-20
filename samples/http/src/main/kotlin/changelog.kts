package kotlin

import dsl.action.HttpScriptAction
import com.github.kittinunf.result.Result.Failure
import com.github.kittinunf.result.Result.Success
import dsl.changeSet
import dsl.changelog

changelog {
    changesets {
        +changeSet {
            id = "ChangeSet-1"
            author = "Postman"
            actions {
                +HttpScriptAction {
                    val (_, _, result) = it.restClient
                        .post("https://httpbin.org/post")
                        .body(
                            """
                        {
                            "menu": {
                                "id": "file",
                                "value": "File",
                                "popup": {
                                    "menuitem": [
                                    {"value": "New", "onclick": "CreateNewDoc()"},
                                    {"value": "Open", "onclick": "OpenDoc()"},
                                    {"value": "Close", "onclick": "CloseDoc()"}
                                    ]
                                }
                            }
                        }
                        """.trimIndent()
                        )
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
        +changeSet {
            id = "ChangeSet-2"
            author = "GraphMan"
            actions {
                +HttpScriptAction {

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

                    val (_, _, result) = it.restClient
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
    }
}