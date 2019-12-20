package engine.mongo

import com.mongodb.ConnectionString
import com.mongodb.client.MongoIterable
import org.litote.kmongo.KMongo

const val MONGODB_CONNECTIONSTRING = "MONGODB_CONNECTIONSTRING"

class MongoEngine {

    val mongoClient by lazy { KMongo.createClient(ConnectionString(getConnectionString())) }

    private fun getConnectionString(): String {
        if (System.getenv().containsKey(MONGODB_CONNECTIONSTRING)) {
            return System.getenv(MONGODB_CONNECTIONSTRING)
        }
        throw IllegalArgumentException("$MONGODB_CONNECTIONSTRING is missing (environment variable)")
    }

    fun listDatabase(): MongoIterable<String> = mongoClient.listDatabaseNames()

}