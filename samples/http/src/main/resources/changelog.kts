import com.github.kittinunf.result.Result.Failure
import com.github.kittinunf.result.Result.Success
import dsl.action.HttpScriptAction
import dsl.changeSet
import dsl.changelog
import me.lazmaid.kraph.Kraph

changelog {
    changesets {
        +changeSet {
            id = "ChangeSet-Http-1"
            author = "Postman"
            actions {
                +HttpScriptAction {
                    val (_, _, result) = it.restClient
                        .get("https://httpbin.org/get")
                        .responseString()
                    it.logger.info { result.get() }
                }
            }
        }
        +changeSet {
            id = "ChangeSet-Http-2"
            author = "GraphQL"
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