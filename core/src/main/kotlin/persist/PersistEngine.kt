package persist

import dsl.ChangeSet
import dsl.Status
import mu.KLoggable

abstract class PersistEngine : KLoggable {
    override val logger = logger()
    abstract fun checkConnection()
    abstract fun notAlreadyExecuted(changeSetId: String): Boolean
    abstract fun lock(changeSet: ChangeSet)
    abstract fun unlock(changeSet: ChangeSet,status: Status)
}