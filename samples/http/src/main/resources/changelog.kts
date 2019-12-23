import dsl.action.HttpScriptAction
import dsl.changeSet
import dsl.changelog

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
                    it.logger.info {result.get()}
                }
            }
        }
    }
}