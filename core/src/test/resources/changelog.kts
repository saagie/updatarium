import dsl.action.BasicAction
import dsl.changeSet
import dsl.changelog

changelog {
    changesets {
        +changeSet {
            id = "ChangeSet-1"
            author = "HelloWorld"
            actions {
                +BasicAction { basicAction ->
                    basicAction.logger.info { "Hello world" }
                }
            }
        }
    }
}