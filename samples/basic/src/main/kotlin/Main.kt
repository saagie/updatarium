fun main() {
    MagicalUpdater().executeChangelog("""
        import dsl.action.BasicAction
        import dsl.changeSet
        import dsl.changelog

        changelog {
            changesets {
                +changeSet {
                    id = "ChangeSet-1"
                    author = "Hello World"
                    actions {
                        +BasicAction {basicAction ->
                            (1..5).forEach {
                                basicAction.logger.info {"Hello ${"$"}it!"}
                            }

                        }
                    }
                }
            }
        }
    """.trimIndent())
}