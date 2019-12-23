package dsl

import com.autodsl.annotation.AutoDsl
import persist.engine.DefaultEngine
import persist.engine.Engine

@AutoDsl
data class Changelog(var changesets: List<ChangeSet> = mutableListOf()) {
    fun execute(engine: Engine = DefaultEngine()) {
        engine.checkConnection()
        changesets.forEach {
            it.execute(engine)
        }
    }
}



