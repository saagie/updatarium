package persist

import com.mongodb.ConnectionString
import dsl.ChangeSet
import dsl.Status
import org.litote.kmongo.*
import persist.model.MongoDbChangeset
import persist.model.toMongoDbDocument

const val MONGODB_PERSIST_CONNECTIONSTRING = "MONGODB_PERSIST_CONNECTIONSTRING"
const val DATABASE = "MagicalUpdater"
const val COLLECTION = "changelog"


class MongodbPersistEngine : PersistEngine() {

    private val collection by lazy {
        with(KMongo.createClient(ConnectionString(getConnectionString()))) {
            this.getDatabase(DATABASE).getCollection<MongoDbChangeset>(COLLECTION)
        }
    }

    private fun getConnectionString(): String {
        if (System.getenv().containsKey(MONGODB_PERSIST_CONNECTIONSTRING)) {
            return System.getenv(MONGODB_PERSIST_CONNECTIONSTRING)
        }
        throw IllegalArgumentException("$MONGODB_PERSIST_CONNECTIONSTRING is missing (environment variable)")
    }

    override fun checkConnection() {
        logger.info { "CheckConnection ... " }
        collection.countDocuments()
        logger.info { "Connection to mongodb instance : successful" }

    }

    override fun notAlreadyExecuted(changeSetId: String): Boolean {
        val doc = collection.findOne(MongoDbChangeset::changesetId eq changeSetId)
        when (doc) {
            null -> {
                logger.info { "$changeSetId not exists" }
                return true
            }
            else -> {
                when (doc.status) {
                    Status.OK.name -> {
                        logger.info { "$changeSetId already executed : OK" }
                    }
                    Status.EXECUTE.name -> {
                        logger.info { "$changeSetId already in progress ?" }
                    }
                    else -> {
                        logger.warn { "$changeSetId was already executed in error" }
                    }
                }
                return false
            }
        }
    }

    override fun lock(changeSet: ChangeSet) {
        collection.insertOne(changeSet.toMongoDbDocument())
        logger.info { "${changeSet.id} marked as ${Status.EXECUTE}" }
    }

    override fun unlock(changeSet: ChangeSet, status: Status) {
        collection.updateOne(MongoDbChangeset::changesetId eq changeSet.id, setValue(MongoDbChangeset::status, status.name))
        logger.info { "${changeSet.id} marked as ${status}" }
    }
}