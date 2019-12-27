import dsl.action.MongoScriptAction
import dsl.changeSet
import dsl.changelog
import org.litote.kmongo.*

changelog {
    changesets {
        +changeSet {
            id = "ChangeSet-Mongodb-1"
            author = "MongoDb"
            actions {
                +MongoScriptAction {mongoScriptAction ->
                    mongoScriptAction.logger.info { "MongoDb" }
                    val database = "sample_mongodb"
                    val collection = "sample"

                    mongoScriptAction.mongoClient.dropDatabase(database)
                    mongoScriptAction.logger.info { "DB $database drop OK" }
                    with(mongoScriptAction.onCollection(database, collection)) {
                        mongoScriptAction.logger.info { "Collection OK" }
                        this.insertOne("{name:'Yoda',age:896}")
                        this.insertOne("{name:'Luke Skywalker',age:19}")
                        mongoScriptAction.logger.info { "2 docs inserted in collection $database.$collection" }
                        this.find()
                            .forEach {mongoScriptAction.logger.info { " name : ${it.get("name")} - age : ${it.get("age")}" }}

                    }
                }
            }
        }
    }
}
