package dsl.action

import com.autodsl.annotation.AutoDsl
import com.mongodb.client.MongoDatabase
import engine.mongo.MongoEngine

@AutoDsl
class MongoScriptAction(val f: (mongoScriptAction: MongoScriptAction) -> Unit) : Action() {

    val mongoEngine = MongoEngine()

    override fun execute() {
        f(this)
    }

    fun onDatabases(databaseNameRegex: String): List<MongoDatabase> = mongoEngine
        .listDatabase()
        .filter { databaseNameRegex.toRegex().matches(it) }
        .map { mongoEngine.mongoClient.getDatabase(it) }

}