package engine.mongo

import com.mongodb.ConnectionString
import com.mongodb.client.MongoIterable
import org.litote.kmongo.KMongo

class MongoEngine {

    val mongoClient =
        KMongo.createClient(ConnectionString("mongodb://${System.getenv("MONGO_USERNAME")}:${System.getenv("MONGO_PASSWORD")}@saagie-common-mongodb:27017"))

    fun listDatabase(): MongoIterable<String> = mongoClient.listDatabaseNames()

}