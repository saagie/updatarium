package dsl.action

import com.autodsl.annotation.AutoDsl
import engine.mongo.MongoEngine

@AutoDsl
class MongoScriptAction(val f: (mongoScriptAction: MongoScriptAction) -> Unit) : Action() {

    val mongoEngine = MongoEngine()
    val mongoClient = mongoEngine.mongoClient


    override fun execute() {
        f(this)
    }

    fun onCollection(databaseName: String, collectionName: String) =
        mongoClient.getDatabase(databaseName).getCollection(collectionName)
}
