package persist

import dsl.ChangeSet
import dsl.Status
import persist.PersistEngine

class DefaultPersistEngine : PersistEngine() {
    override fun checkConnection() {
        logger.warn { "***********************" }
        logger.warn { "*NO PERSIST ENGINE !!!*" }
        logger.warn { "***********************" }
    }

    override fun notAlreadyExecuted(changeSetId: String): Boolean = true
    override fun lock(changeSet: ChangeSet) {
        logger.info { "${changeSet.id} marked as ${Status.EXECUTE}" }
    }

    override fun unlock(changeSet: ChangeSet, status: Status) {
        logger.info { "${changeSet.id} marked as ${status}" }
    }
}