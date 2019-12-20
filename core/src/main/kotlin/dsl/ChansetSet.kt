package dsl

import com.autodsl.annotation.AutoDsl
import dsl.action.Action
import mu.KLoggable
import kotlin.random.Random

@AutoDsl
data class ChangeSet(val id: String, val author: String, val actions: List<Action> = mutableListOf()) : KLoggable {
    override val logger = logger()

    fun execute() {
        if (notAlreadyExecuted()) {
            logger.info { "$id will be executed" }
            this.lock()
            try {
                this.actions.forEach {
                    it.execute()
                }
                logger.info { "$id marked as ${Status.OK}" }
            } catch (e: Exception) {
                logger.error(e) { "Error during apply update" }
                logger.info { "$id marked as ${Status.KO}" }
            } finally {
                this.unlock()
            }

        } else {
            logger.info { "$id already executed" }
        }
    }

    private fun lock() {
        logger.info { "LOCK" }
        logger.info { "$id marked as ${Status.EXECUTE}" }
    }

    private fun unlock() {
        logger.info { "UNLOCK" }
    }

    private fun notAlreadyExecuted(): Boolean = true//Random.nextBoolean()
}