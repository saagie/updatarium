import dsl.action.BashScriptAction
import dsl.changeSet
import dsl.changelog

changelog {
    changesets {
        +changeSet {
            id = "ChangeSet-Http-1"
            author = "Postman"
            actions {
                +BashScriptAction(
                    script = """
curl -I https://httpbin.org/get | grep -i Server &&\
pwd &&\
export | grep " PWD"
""".trimIndent(),
                    workingDir = "/tmp"
                )
            }
        }
    }
}