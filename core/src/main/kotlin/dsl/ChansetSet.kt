package dsl

import com.autodsl.annotation.AutoDsl
import dsl.action.Action
import mu.KLoggable
import persist.engine.DefaultEngine
import persist.engine.Engine

@AutoDsl
data class ChangeSet(val id: String, val author: String, val actions: List<Action> = mutableListOf()) : KLoggable {
    override val logger = logger()

    fun execute(engine: Engine = DefaultEngine()) {
        if (engine.notAlreadyExecuted(id)) {
            logger.info { "$id will be executed" }
            engine.lock(this)
            try {
                this.actions.forEach {
                    it.execute()
                }
                engine.unlock(this, Status.OK)
                logger.info { "$id marked as ${Status.OK}" }
            } catch (e: Exception) {
                logger.error(e) { "Error during apply update" }
                engine.unlock(this, Status.KO)
                logger.info { "$id marked as ${Status.KO}" }
            }
        } else {
            logger.info { "$id already executed" }
        }
    }

    private fun unlock() {
        logger.info { "UNLOCK" }
    }
}